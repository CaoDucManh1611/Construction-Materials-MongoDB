/* Existing screens share these additional business actions. */
(() => {
  const path = location.pathname;
  const money = value => Number(value || 0).toLocaleString('vi-VN') + ' đ';
  const element = (tag, text, className) => { const node = document.createElement(tag); if (text) node.textContent = text; if (className) node.className = className; return node; };
  async function api(url, body) {
    const response = await fetch(url, body === undefined ? {} : {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)});
    const text = await response.text(); let data; try { data = JSON.parse(text); } catch { data = text; }
    if (!response.ok) throw new Error(data.message || (typeof data === 'string' ? data : 'Thao tác thất bại.'));
    return data;
  }
  function panel(title) {
    const box = element('section', '', 'card'); box.style.cssText = 'margin:24px 0;padding:20px;overflow:auto;'; box.append(element('h2', title));
    const main = document.querySelector('.main-content') || document.querySelector('main') || document.body;
    main.insertBefore(box, main.querySelector(':scope > .workspace-footer')); return box;
  }
  function button(text, action) {
    const btn = element('button', text, 'btn btn-primary btn-sm'); btn.type = 'button';
    btn.addEventListener('click', async () => { btn.disabled = true; try { await action(); } catch (error) { alert(error.message); } finally { btn.disabled = false; } }); return btn;
  }
  function field(form, title, name, type = 'text', options) {
    const group = element('div', '', 'form-group'); const id = 'business-' + name + '-' + crypto.randomUUID();
    const label = element('label', title, 'form-label'); label.htmlFor = id;
    const input = element(options ? 'select' : 'input', '', 'form-control'); input.id = id; input.name = name; input.required = true;
    if (options) for (const [value, text] of options) { const option = element('option', text); option.value = value; input.append(option); }
    else { input.type = type; if (type === 'number') { input.min = '0.001'; input.step = '0.001'; } }
    group.append(label, input); form.append(group); return input;
  }
  function table(box, headers, rows) {
    const grid = element('table', '', 'data-table'); const head = element('thead'); const header = element('tr');
    headers.forEach(text => header.append(element('th', text))); head.append(header); grid.append(head);
    const body = element('tbody'); rows.forEach(values => { const row = element('tr'); values.forEach(value => { const cell = element('td'); value instanceof Node ? cell.append(value) : cell.textContent = String(value ?? ''); row.append(cell); }); body.append(row); });
    if (!rows.length) { const cell = element('td', 'Chưa có dữ liệu.'); cell.colSpan = headers.length; const row = element('tr'); row.append(cell); body.append(row); }
    grid.append(body); const wrapper = element('div', '', 'table-scroll'); wrapper.style.overflowX = 'auto'; wrapper.append(grid); box.append(wrapper); return grid;
  }
  function submit(form, title, action) {
    const status = element('p'); status.setAttribute('role', 'status'); const btn = element('button', title, 'btn btn-primary'); btn.type = 'submit'; form.append(btn, status);
    form.addEventListener('submit', async event => { event.preventDefault(); btn.disabled = true; status.textContent = 'Đang xử lý…'; try { const result = await action(); status.textContent = result.message || 'Đã lưu thành công.'; } catch (error) { status.textContent = error.message; } finally { btn.disabled = false; } });
  }
  async function quotes() {
    const box = panel('Báo giá và tư vấn'); const customer = path.startsWith('/khach_hang/');
    if (!customer) {
      const [customers, products] = await Promise.all([api('/api/sales/customers'), api('/api/khach_hang/products')]);
      const form = element('form'); const target = field(form, 'Khách hàng', 'customer', 'text', customers.map(c => [c.id, c.hoTen])); const lines = element('div'); form.append(lines);
      const selections = [];
      const addLine = () => {
        const row = element('fieldset'); row.append(element('legend', 'Vật liệu ' + (selections.length + 1)));
        const product = field(row, 'Vật liệu', 'product', 'text', products.map(p => [p.id, p.tenHang])); const qty = field(row, 'Số lượng', 'qty', 'number');
        lines.append(row); selections.push({product, qty});
      };
      addLine(); form.append(button('Thêm vật liệu', addLine));
      submit(form, 'Lập và gửi báo giá', async () => { const result = await api('/api/business/quotes', {khachHangId: target.value, chiTiet: selections.map(s => ({hangHoaId: s.product.value, soLuong: s.qty.value}))}); await refresh(); return {message: 'Đã gửi báo giá ' + result.maBaoGia}; }); box.append(form);
    }
    const list = element('div'); box.append(list);
    async function refresh() {
      list.replaceChildren(); const data = await api('/api/business/quotes');
      table(list, ['Mã báo giá', 'Vật liệu', 'Giá trị', 'Trạng thái', 'Thao tác'], data.map(q => [q.maBaoGia, q.chiTiet.map(l => l.hangHoa.tenHang + ' × ' + l.soLuong).join(', '), money(q.tongTien), q.trangThai, customer && q.trangThai === 'DA_GUI' ? button('Chấp nhận báo giá', async () => { await api('/api/business/quotes/' + q.id + '/accept', {}); await refresh(); if (window.loadOrders) window.loadOrders(); }) : '—']));
    }
    await refresh();
  }
  async function recovery() {
    const box = panel('Kiểm tra và thu hồi hàng đổi trả'); const [requests, data] = await Promise.all([api('/api/business/returns-to-receive'), api('/api/warehouse/form-data')]);
    if (!requests.length) { box.append(element('p', 'Không có yêu cầu đã duyệt chờ thu hồi.')); return; }
    const form = element('form'); const request = field(form, 'Yêu cầu thu hồi', 'return', 'text', requests.map(r => [r.maDoiTra, r.maDoiTra + ' — ' + r.hangHoa.tenHang + ' × ' + r.soLuong]));
    const warehouse = field(form, 'Kho nhận', 'warehouse', 'text', data.khos.map(k => [k.id, k.tenKho]));
    const classification = field(form, 'Kết quả kiểm tra', 'classification', 'text', [['HONG', 'Hàng hỏng — cách ly, không đưa vào kho bán'], ['BAN_LAI', 'Hàng đạt kiểm tra — có thể bán lại']]);
    const report = field(form, 'Biên bản kiểm tra và thu hồi', 'report');
    submit(form, 'Xác nhận thu hồi', async () => { const result = await api('/api/business/returns/receive', {maDoiTra: request.value, khoId: warehouse.value, phanLoai: classification.value, bienBan: report.value}); form.remove(); return result; }); box.append(form);
  }
  async function catalog() {
    const box = panel('Đề xuất danh mục chờ duyệt');
    async function refresh() {
      box.querySelector('table')?.remove(); const data = await api('/api/business/catalog-proposals');
      table(box, ['Đề xuất', 'Nội dung', 'Thao tác'], data.map(item => {
        const actions = element('div');
        actions.append(button('Duyệt', async () => { await api('/api/business/catalog-proposals/' + item.id + '/decide', {duyet: true}); await refresh(); }), button('Từ chối', async () => { await api('/api/business/catalog-proposals/' + item.id + '/decide', {duyet: false}); await refresh(); }));
        const details = item.hangHoa ? `${item.hangHoa.maHang} — ${item.hangHoa.tenHang}; ${item.hangHoa.quyCach || ''}; giá lẻ ${money(item.hangHoa.giaBanLe)}, giá sỉ ${money(item.hangHoa.giaBanSi)}` : item.danhMuc ? `${item.danhMuc.maDanhMuc} — ${item.danhMuc.ten}; ${item.danhMuc.moTa || ''}` : 'Mã đối tượng: ' + item.doiTuongId;
        return [({THEM: 'Thêm danh mục/hàng hóa', SUA: 'Sửa thông tin', NGUNG: 'Ngừng sử dụng'})[item.thaoTac], details, actions];
      }));
    } await refresh();
  }
  async function finance() {
    const box = panel('Công nợ nhà cung cấp và hóa đơn'); const [debts, invoices, suppliers] = await Promise.all([api('/api/business/supplier-debts'), api('/api/business/invoices'), api('/api/accounting/suppliers')]);
    const supplierNames = new Map(suppliers.map(s => [s.id, s.tenNcc]));
    table(box, ['Nhà cung cấp', 'Phải trả', 'Đã trả', 'Trạng thái'], debts.map(d => [supplierNames.get(d.nhaCungCapId) || d.nhaCungCapId, money(d.soTienNo), money(d.soTienDaTt), d.trangThai]));
    const unpaid = debts.filter(d => Number(d.soTienNo) > Number(d.soTienDaTt));
    if (unpaid.length) {
      const form = element('form'); const debt = field(form, 'Công nợ cần chi trả', 'debt', 'text', unpaid.map(d => [d.id, (supplierNames.get(d.nhaCungCapId) || d.nhaCungCapId) + ' — còn ' + money(Number(d.soTienNo) - Number(d.soTienDaTt))]));
      const amount = field(form, 'Số tiền trả', 'amount', 'number'); const tx = field(form, 'Mã giao dịch', 'transaction'); tx.value = crypto.randomUUID();
      submit(form, 'Ghi nhận chi trả', () => api('/api/accounting/debts/collect', {congNoId: debt.value, soTien: amount.value, hinhThuc: 'CHUYEN_KHOAN', maGiaoDich: tx.value})); box.append(form);
    }
    table(box, ['Hóa đơn', 'Tiền thực thu', 'Ngày lập'], invoices.map(i => [i.maHoaDon, money(i.soTien), i.ngayLap]));
    const refunds = (await api('/api/accounting/debts/customers')).filter(d => Number(d.soTienHoanTra) > 0);
    if (refunds.length) {
      const form = element('form'); const debt = field(form, 'Khoản hoàn khách hàng', 'refund', 'text', refunds.map(d => [d.id, d.khachHangTen + ' — phải hoàn ' + money(d.soTienHoanTra)]));
      const amount = field(form, 'Số tiền hoàn', 'refund-amount', 'number'); const tx = field(form, 'Mã giao dịch hoàn', 'refund-tx'); tx.value = crypto.randomUUID();
      submit(form, 'Ghi nhận hoàn tiền', () => api('/api/business/debts/refund', {congNoId: debt.value, soTien: amount.value, maGiaoDich: tx.value})); box.append(form);
    }
  }
  async function releases() {
    const box = panel('Lệnh xuất đã được kế toán phát hành'); const data = await api('/api/business/release-orders');
    table(box, ['Lệnh xuất', 'Vật liệu', 'Trạng thái'], data.map(r => [r.maLenhXuat, r.chiTiet.map(l => l.hangHoa.tenHang + ' × ' + l.soLuong).join(', '), r.trangThai === 'CHO_XUAT' ? 'Chờ lập phiếu xuất và duyệt' : 'Đã xuất kho']));
  }
  async function reports() {
    const box = panel('Báo cáo phòng ban'); const form = element('form');
    const from = field(form, 'Từ ngày', 'report-from', 'date'); const to = field(form, 'Đến ngày', 'report-to', 'date');
    const today = new Date().toISOString().slice(0, 10); from.value = today.slice(0, 8) + '01'; to.value = today;
    submit(form, 'Lập báo cáo và gửi duyệt', async () => { const result = await api('/api/business/reports', {tuNgay: from.value, denNgay: to.value}); await refresh(); return result; }); box.append(form);
    const list = element('div'); box.append(list);
    async function refresh() {
      list.replaceChildren(); const data = await api('/api/business/reports');
      table(list, ['Báo cáo', 'Kỳ', 'Trạng thái', 'Xuất dữ liệu'], data.map(r => { const link = element('a', 'Tải báo cáo'); link.href = '/api/business/reports/' + r.id + '/export'; link.download = ''; return [r.tieuDe, r.tuNgay + ' — ' + r.denNgay, r.trangThai === 'CHO_DUYET' ? 'Chờ quản lý duyệt' : r.trangThai, link]; }));
    } await refresh();
  }
  async function init() {
    try {
      if (path === '/kinh_doanh/quan_ly_don_hang' || path === '/khach_hang/don_hang_cua_toi') await quotes();
      if (path === '/quan_ly_kho/quan_ly_kho') { await releases(); await recovery(); }
      if (path === '/ban_quan_ly/dashboard_tong_quan') await catalog();
      if (path === '/ke_toan/quan_ly_tai_chinh_va_cong_no') await finance();
      if (path === '/kinh_doanh/quan_ly_don_hang' || path === '/quan_ly_kho/quan_ly_kho') await reports();
    } catch (error) { const box = panel('Thông báo'); box.append(element('p', error.message)); }
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();
