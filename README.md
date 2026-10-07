# Quản lý cửa hàng vật liệu xây dựng — MongoDB

Ứng dụng Java 21+, Spring Boot 4.0.6, Spring Data MongoDB, Spring Security và Thymeleaf. Thiết kế nghiệp vụ đối chiếu tài liệu phase3; MongoDB là cơ sở dữ liệu chính thức. Không còn cấu hình kết nối MySQL/JPA.

## Chạy dự án

Cần JDK 21 trở lên, Maven hoặc Maven Wrapper và MongoDB replica set. Transaction của các nghiệp vụ tiền và kho **bắt buộc replica set**, kể cả chạy trên một máy.

Nếu có Docker, tại thư mục gốc:

```powershell
docker compose up -d --wait
cd ht_vlxd
$env:MONGODB_URI='mongodb://localhost:27017/vlxd_db?replicaSet=rs0'
$env:SEED_DEMO='true'
.\mvnw.cmd spring-boot:run
```

Mở http://localhost:8080/login. Nếu không có Docker, chạy MongoDB cục bộ với `mongod --replSet rs0 --bind_ip 127.0.0.1 --dbpath <thư-mục-dữ-liệu>` rồi dùng `mongosh` khởi tạo `rs.initiate({_id:'rs0',members:[{_id:0,host:'localhost:27017'}]})`. Không khởi tạo lại replica set đã có.

`SEED_DEMO` mặc định `false`; bật khi tạo database demo mới. Seeder không đặt lại mật khẩu khi khởi động. Tài khoản mẫu dùng mật khẩu `Admin@123`:

| Vai trò | Tài khoản |
| --- | --- |
| Quản trị viên | admin |
| Ban quản lý | giamdoc |
| Kinh doanh | nvkd01 |
| Kho | nvkho01 |
| Kế toán | nvkt01 |
| Khách hàng | khachhang01 |

Gemini là tùy chọn qua biến `GEMINI_API_KEY`. Các luồng quản lý không cần API key.

## Luồng nghiệp vụ đã triển khai

1. Kinh doanh lập báo giá có chi tiết và thời hạn. Khách hàng chấp nhận báo giá của mình để tạo đơn chờ xác nhận; hoặc gửi đơn từ giỏ hàng.
2. Kinh doanh xác nhận đơn và lập hợp đồng với giá/số lượng lưu tại thời điểm thỏa thuận. Ban quản lý duyệt, sau đó kinh doanh ghi nhận ký hợp đồng. Giá trị hợp đồng tạo công nợ; tiền cọc đã thu ban đầu bằng 0.
3. Kế toán thu tiền thực tế với mã giao dịch chống ghi trùng, tạo hóa đơn. Khi đạt cọc đã thỏa thuận mới có lệnh xuất. Cọc 30% trên form là giá trị gợi ý, có thể chỉnh khi lập hợp đồng.
4. Kho lập phiếu xuất khớp lệnh xuất; quản lý duyệt mới trừ tồn kho. Chỉ lập giao nhận sau khi xuất kho; bàn giao lưu biên bản và hoàn thành đơn, không trừ kho lần nữa.
5. Nhập kho cần nhà cung cấp, đơn giá và biên bản kiểm đếm/chất lượng. Duyệt phiếu tăng tồn và tạo công nợ phải trả; kế toán chi tiền để giảm công nợ.
6. Kiểm kê tạo đề xuất điều chỉnh, giữ số lượng sổ sách để phát hiện tồn kho đã thay đổi. Quản lý duyệt trước khi cập nhật tồn.
7. Đổi/trả phải thuộc đơn của khách, số lượng cộng dồn không vượt lượng mua. Kinh doanh duyệt yêu cầu, kho nhận và phân loại. Hàng hỏng không vào tồn bán. Trả hàng điều chỉnh công nợ theo giá hợp đồng gốc; kế toán ghi phiếu hoàn tiền nếu thực thu vượt giá trị còn lại. Đổi hàng tạo đơn/lệnh giao thay thế, xuất kho và bàn giao riêng.
8. Thêm/sửa/ngừng hàng hóa và danh mục đi qua đề xuất quản lý duyệt. Báo cáo phòng ban lưu số liệu do server tính, gửi quản lý duyệt và xuất JSON.

Hủy/ngừng giữ lịch sử và liên kết chứng từ. Hợp đồng đã thu tiền hoặc có lệnh xuất bị chặn hủy trực tiếp. API phân quyền ở server, không tin vai trò/username do trình duyệt gửi; CSRF và khóa sau 5 lần đăng nhập sai được áp dụng. Session đang đăng nhập được kiểm tra lại khi tài khoản bị khóa hoặc đổi quyền.

## Thiết kế dữ liệu

Xem **[Database.mongodb.md](Database.mongodb.md)** để đọc đầy đủ 22 collection, các trường BSON, tham chiếu, chi tiết nhúng, index và sơ đồ. **[Database.mongodb.js](Database.mongodb.js)** là script mongosh tạo collection/index cho database mới; không chứa dữ liệu thật.

- Collection độc lập: `nguoi_dung`, `role`, `khach_hang`, `nha_cung_cap`, `danh_muc`, `hang_hoa`, `kho`, `ton_kho`, `don_hang`, `hop_dong`, `phieu_kho`, `giao_nhan`, `doi_tra_hang`, `cong_no`, `thanh_toan`, `bao_cao`, `dinh_muc_vat_lieu`.
- Collection bổ sung: `bang_bao_gia`, `hoa_don`, `lenh_xuat`, `de_xuat_danh_muc`, `sequences`.
- Chi tiết đơn hàng/phiếu kho nhúng trong chứng từ cha. Hợp đồng, báo giá và lệnh xuất lưu bản chi tiết giá/số lượng; thay đổi giá danh mục không sửa giá chứng từ cũ.
- Tham chiếu giữa collection dùng ID số nguyên giữ tương thích API. `sequences` cấp ID nguyên tử; `@Version` kiểm soát ghi đồng thời. Mã chứng từ, mã giao dịch và cặp hàng–kho có unique index.
- Tiền/số lượng dùng `BigDecimal` và MongoDB `Decimal128`. Các thay đổi tiền/kho/chứng từ liên quan nằm trong cùng transaction.

## Dữ liệu MySQL cũ

`Database.sql` là tài liệu lịch sử của source cũ, không dùng để khởi tạo bản MongoDB. Công cụ offline chỉ chuẩn bị file nhập, không kết nối hay ghi đè database:

```powershell
python scripts/convert_mysql_export.py mysql-export.json --output mongo-import
python scripts/test_mysql_conversion.py
```

Đầu vào là JSON dạng `{ "ten_bang": [ { "id": 1, ... } ] }`, cột tên snake_case như source cũ. Đầu ra là MongoDB Extended JSON, giữ ID, Decimal128, tham chiếu, nhúng chi tiết và tạo sequence. Công cụ không đọc trực tiếp file SQL. Đọc `RECONCILE.md` sinh ra và đối soát tiền cọc, công nợ, tồn kho, giá hợp đồng trước khi nhập vào **database mới**. Source cũ có sai nghiệp vụ nên đổi định dạng không tự xác nhận số dư đúng. Chưa thực hiện nhập dữ liệu MySQL thực tế của người dùng.

## Kiểm tra

Các test tích hợp dùng MongoDB riêng; tuyệt đối không trỏ `TEST_MONGODB_URI` vào database sử dụng thật. Ví dụ có replica set test `rs-test` tại cổng 27028:

```powershell
cd ht_vlxd
$env:TEST_MONGODB_URI='mongodb://127.0.0.1:27028/vlxd_test_workflows?replicaSet=rs-test'
.\mvnw.cmd test
.\mvnw.cmd package -DskipTests
cd ..
python scripts/check_javascript.py
python scripts/test_mysql_conversion.py
```

Kiểm thử bao gồm phân quyền/CSRF, khóa đăng nhập, thực thu cọc, chống ghi trùng, xuất kho lặp/thiếu hàng, đổi/trả, hoàn tiền, công nợ NCC và duyệt danh mục. Kiểm tra giao diện thực tế bằng Playwright trên database demo riêng.

Với ứng dụng demo đang chạy ở cổng 8088, kiểm tra sáu vai trò và các màn hình quản lý ở kích thước máy tính/điện thoại bằng `playwright-cli run-code --filename=scripts/browser_smoke.js` sau khi mở browser tới trang đăng nhập. Script dùng các tài khoản mẫu nêu trên.

## Phạm vi hiện tại

Một lệnh xuất tương ứng một lần xuất đủ chi tiết; chưa chia giao nhiều đợt từ cùng lệnh. Biên bản sự vụ được lưu trong hệ thống, chưa có dịch vụ gửi thông báo email/SMS. Báo cáo xuất JSON, chưa xuất biểu mẫu PDF. Phần giao diện 3.3 trong tài liệu do chủ dự án chủ động bỏ để bổ sung ảnh sau khi hoàn thiện.

## Giao diện và luồng truy cập

Trang `/` giới thiệu doanh nghiệp trước. Khách xem danh mục, tìm kiếm/lọc/sắp xếp và xem chi tiết vật liệu tại `/san_pham` mà không cần đăng nhập. Giỏ lựa chọn được lưu trên trình duyệt; khi mở giỏ để gửi đơn, khách đăng nhập tại `/login?next=cart`. Đăng nhập thành công chuyển tới khu vực theo vai trò do máy chủ trả về. Giao diện dùng CSS/JavaScript hiện có, phông hệ thống và hình SVG minh họa lưu trong dự án.

Kiểm tra luồng công khai và giỏ hàng trên database demo riêng:

```powershell
playwright-cli open http://127.0.0.1:8088/ --browser=msedge
playwright-cli run-code --filename=scripts/storefront_smoke.js
playwright-cli run-code --filename=scripts/browser_smoke.js
```

Trang giới thiệu có chuyển động mở đầu, vật liệu nổi, dải chữ chạy, xuất hiện khi cuộn và thẻ nghiêng trên thiết bị có chuột. Nút tạm dừng lưu lựa chọn trên trình duyệt; hệ thống tôn trọng `prefers-reduced-motion`. Các cảnh nhà ở, công trình và xe giao vật liệu là SVG minh họa, không phải ảnh dự án thực tế. Chạy `playwright-cli run-code --filename=scripts/motion_smoke.js` để kiểm tra animation, ảnh, FAQ và bố cục ở 375/768/1440 px.
