// Cấu trúc MongoDB đang dùng bởi ứng dụng. Xem Database.mongodb.md.
// Chạy: mongosh "mongodb://localhost:27017/vlxd_db?replicaSet=rs0" --file Database.mongodb.js
// Chỉ tạo collection/index; không drop, không sửa chứng từ, không reset sequences.
const collections = [
  "nguoi_dung",
  "role",
  "khach_hang",
  "dinh_muc_vat_lieu",
  "cong_no",
  "hoa_don",
  "thanh_toan",
  "kho",
  "lenh_xuat",
  "phieu_kho",
  "ton_kho",
  "bao_cao",
  "danh_muc",
  "de_xuat_danh_muc",
  "hang_hoa",
  "bang_bao_gia",
  "doi_tra_hang",
  "don_hang",
  "giao_nhan",
  "hop_dong",
  "nha_cung_cap",
  "sequences"
];
for (const name of collections) {
  if (!db.getCollectionNames().includes(name)) db.createCollection(name);
}

db.getCollection("nguoi_dung").createIndex({"username": 1}, {"name": "username", "unique": true, "sparse": true});
db.getCollection("nguoi_dung").createIndex({"email": 1}, {"name": "email", "unique": true, "sparse": true});
db.getCollection("role").createIndex({"name": 1}, {"name": "name", "unique": true, "sparse": true});
db.getCollection("khach_hang").createIndex({"maKhachHang": 1}, {"name": "maKhachHang", "unique": true, "sparse": true});
db.getCollection("dinh_muc_vat_lieu").createIndex({"loaiCongTrinh": 1, "loaiVatLieu": 1}, {"name": "DinhMucVatLieu_unique", "unique": true});
db.getCollection("cong_no").createIndex({"phieuNhapId": 1}, {"name": "phieuNhapId", "unique": true, "sparse": true});
db.getCollection("cong_no").createIndex({"donHang": 1}, {"name": "donHang", "unique": true, "sparse": true});
db.getCollection("hoa_don").createIndex({"maHoaDon": 1}, {"name": "maHoaDon", "unique": true});
db.getCollection("hoa_don").createIndex({"thanhToanId": 1}, {"name": "thanhToanId", "unique": true});
db.getCollection("thanh_toan").createIndex({"maThanhToan": 1}, {"name": "maThanhToan", "unique": true, "sparse": true});
db.getCollection("thanh_toan").createIndex({"maGiaoDich": 1}, {"name": "maGiaoDich", "unique": true, "sparse": true});
db.getCollection("kho").createIndex({"maKho": 1}, {"name": "maKho", "unique": true, "sparse": true});
db.getCollection("lenh_xuat").createIndex({"maLenhXuat": 1}, {"name": "maLenhXuat", "unique": true});
db.getCollection("lenh_xuat").createIndex({"donHangId": 1}, {"name": "donHangId", "unique": true});
db.getCollection("phieu_kho").createIndex({"maPhieu": 1}, {"name": "maPhieu", "unique": true, "sparse": true});
db.getCollection("ton_kho").createIndex({"hangHoa": 1, "kho": 1}, {"name": "TonKho_unique", "unique": true});
db.getCollection("danh_muc").createIndex({"maDanhMuc": 1}, {"name": "maDanhMuc", "unique": true, "sparse": true});
db.getCollection("hang_hoa").createIndex({"maHang": 1}, {"name": "maHang", "unique": true, "sparse": true});
db.getCollection("bang_bao_gia").createIndex({"maBaoGia": 1}, {"name": "maBaoGia", "unique": true});
db.getCollection("doi_tra_hang").createIndex({"maDoiTra": 1}, {"name": "maDoiTra", "unique": true, "sparse": true});
db.getCollection("don_hang").createIndex({"maDonHang": 1}, {"name": "maDonHang", "unique": true, "sparse": true});
db.getCollection("giao_nhan").createIndex({"maGiaoNhan": 1}, {"name": "maGiaoNhan", "unique": true, "sparse": true});
db.getCollection("hop_dong").createIndex({"maHopDong": 1}, {"name": "maHopDong", "unique": true, "sparse": true});
db.getCollection("nha_cung_cap").createIndex({"maNcc": 1}, {"name": "maNcc", "unique": true, "sparse": true});

print("Đã chuẩn bị " + collections.length + " collection trong database " + db.getName());
