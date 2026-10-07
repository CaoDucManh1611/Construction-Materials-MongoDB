"""Convert a JSON export of MySQL tables into MongoDB Extended JSON, offline.

Input: {"don_hang": [{"id": 1, ...}], "don_hang_chi_tiet": [...], ...}.
This prepares files only; it never connects to or overwrites a database.
"""
import argparse
import json
import re
from datetime import datetime, timezone, timedelta
from decimal import Decimal
from pathlib import Path

TABLES = {
    'role', 'nguoi_dung', 'khach_hang', 'nha_cung_cap', 'danh_muc', 'hang_hoa',
    'kho', 'ton_kho', 'phieu_kho', 'don_hang', 'hop_dong', 'giao_nhan',
    'doi_tra_hang', 'cong_no', 'thanh_toan', 'bao_cao', 'dinh_muc_vat_lieu',
    'don_hang_chi_tiet', 'phieu_kho_chi_tiet'
}
MONEY = {'so_luong', 'don_gia', 'thanh_tien', 'tong_tien', 'tien_dat_coc', 'gia_tri',
         'so_tien_no', 'so_tien_da_tt', 'so_tien', 'gia_ban_le', 'gia_ban_si',
         'han_muc_no', 'dien_tich', 'he_so_m2'}
def camel(name):
    return re.sub(r'_([a-z])', lambda m: m[1].upper(), name)
def decimal(value):
    return {'$numberDecimal': str(Decimal(str(value)))}
def record(table, row):
    result = {'_id': int(row['id']), 'version': 0}
    for key, value in row.items():
        if key == 'id' or value is None:
            continue
        name = camel(key.removesuffix('_id')) if key.endswith('_id') else camel(key)
        if key.endswith('_id'):
            value = int(value)
        elif key in MONEY:
            value = decimal(value)
        elif key.startswith('ngay_') or key == 'han_thanh_toan' or key in ('tu_ngay', 'den_ngay'):
            date = datetime.fromisoformat(str(value).replace(' ', 'T'))
            if date.tzinfo is None: date = date.replace(tzinfo=timezone(timedelta(hours=7)))
            value = {'$date': date.astimezone(timezone.utc).isoformat().replace('+00:00', 'Z')}
        elif key == 'da_ban_giao':
            value = bool(int(value))
        elif isinstance(value, Decimal):
            value = decimal(value)
        result[name] = value
    if table == 'thanh_toan':
        result['loaiPhieu'] = 'THU'
        if not result.get('maGiaoDich'): result.pop('maGiaoDich', None)
    if table == 'cong_no':
        result.update(loaiCongNo='PHAI_THU', soTienDaHoan=decimal(0), soTienHoanTra=decimal(0))
    if table == 'danh_muc': result['hoatDong'] = True
    if table == 'nguoi_dung': result['soLanDangNhapSai'] = 0
    if result.get('trangThai') == 'HUY': result['trangThai'] = 'DA_HUY'
    return result

def convert(tables):
    unknown = set(tables) - TABLES
    if unknown: raise ValueError('Unsupported tables: ' + ', '.join(sorted(unknown)))
    output = {table: [record(table, row) for row in rows] for table, rows in tables.items()}
    for table, rows in output.items():
        if len({r['_id'] for r in rows}) != len(rows): raise ValueError('Duplicate ids in ' + table)
    for child, parent, parent_field in [('don_hang_chi_tiet', 'don_hang', 'donHang'), ('phieu_kho_chi_tiet', 'phieu_kho', 'phieuKho')]:
        parents = {row['_id']: row for row in output.get(parent, [])}
        for row in parents.values(): row['chiTiet'] = []
        for row in output.get(child, []):
            parent_id = row.pop(parent_field)
            if parent_id not in parents: raise ValueError('Orphan detail in ' + child)
            row.pop('version', None); parents[parent_id]['chiTiet'].append(row)
    orders = {r['_id']: r for r in output.get('don_hang', [])}
    debts = {r.get('donHang'): r for r in output.get('cong_no', [])}
    transactions = [r['maGiaoDich'] for r in output.get('thanh_toan', []) if r.get('maGiaoDich')]
    if len(transactions) != len(set(transactions)): raise ValueError('Duplicate payment transaction codes')
    for contract in output.get('hop_dong', []):
        order = orders.get(contract.get('donHang'))
        if not order: raise ValueError('Contract has no order')
        # Preserve order price snapshots; flag any contract-level discount for review.
        contract['chiTiet'] = order.get('chiTiet', [])
        contract['tienDaThu'] = debts.get(order['_id'], {}).get('soTienDaTt', decimal(0))
    output['sequences'] = [{'_id': table, 'value': max((int(row['id']) for row in rows), default=0)} for table, rows in tables.items() if table not in ('don_hang_chi_tiet', 'phieu_kho_chi_tiet')]
    for table, sequence in [('don_hang_chi_tiet', 'DonHangChiTiet'), ('phieu_kho_chi_tiet', 'PhieuKhoChiTiet')]:
        output['sequences'].append({'_id': sequence, 'value': max((int(row['id']) for row in tables.get(table, [])), default=0)})
    output.pop('don_hang_chi_tiet', None); output.pop('phieu_kho_chi_tiet', None)
    return output

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('input', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    tables = json.loads(args.input.read_text(encoding='utf-8-sig'), parse_float=Decimal)
    output = convert(tables)
    targets = [args.output / (table + '.json') for table in output] + [args.output / 'RECONCILE.md']
    for target in targets:
        if target.exists(): raise FileExistsError(target)
    args.output.mkdir(parents=True, exist_ok=True)
    for table, rows in output.items():
        target = args.output / (table + '.json')
        if target.exists(): raise FileExistsError(target)
        target.write_text(json.dumps(rows, ensure_ascii=False, indent=2), encoding='utf-8')
    (args.output / 'RECONCILE.md').write_text('''# Đối soát dữ liệu cũ

Các ID, số dư và trạng thái lịch sử được giữ nguyên. Đây là chuyển định dạng,
chưa phải xác nhận tính đúng của nghiệp vụ cũ. Trước khi dùng dữ liệu thật:

- Đối soát tiền cọc với chứng từ thu: source cũ có thể tự ghi cọc 30%.
- Đối soát công nợ theo tổng giá trị hợp đồng và các khoản thực thu.
- Đối soát giá chi tiết hợp đồng, đặc biệt hợp đồng có chiết khấu.
- Đối soát tồn kho thực tế vì source cũ có thể trừ kho hai lần.
- Đối soát người nhận/biên bản giao hàng và các giao dịch hoàn tiền.
- Tài khoản có mật khẩu dạng rõ cần đặt lại mật khẩu BCrypt trước khi đăng nhập.

Không tự tạo hóa đơn hay lệnh xuất cho chứng từ cũ khi chưa đối soát.
Giữ bản sao MySQL gốc. Nhập vào database MongoDB mới, không ghi đè database đang dùng.
''', encoding='utf-8')
    print(f'Prepared {len(output)} collections at {args.output.resolve()} (not imported).')

if __name__ == '__main__': main()
