# 🏗️ Construction Materials Management System (Hệ Thống Quản Lý Cửa Hàng Vật Liệu Xây Dựng)

Hệ thống **Construction Materials Management System** là giải pháp phần mềm quản lý quy trình hoạt động (mini ERP) dành riêng cho các cửa hàng, đại lý phân phối vật liệu xây dựng. Hệ thống hỗ trợ tối ưu hóa việc quản lý xuất - nhập kho, theo dõi đơn hàng, quản lý hợp đồng đại lý, đối soát công nợ khách hàng và nhà cung cấp.

---

## 🛠️ CÔNG NGHỆ SỬ DỤNG (TECH STACK)

*   **Backend Framework:** Spring Boot 4.0.6
*   **Ngôn ngữ lập trình:** Java 26 (Amazon Corretto)
*   **Bảo mật:** Spring Security (Mã hóa mật khẩu bằng BCrypt, thiết lập bộ lọc phân quyền API)
*   **Database ORM:** Spring Data JPA & Hibernate 6
*   **Hệ quản trị cơ sở dữ liệu:** MySQL 8.0+
*   **Frontend Template Engine:** Thymeleaf (HTML5 / CSS3 / JavaScript)

---

## 📋 CÁC TÍNH NĂNG ĐÃ HOÀN THÀNH (COMPLETED FEATURES)

Hệ thống đã xây dựng hoàn chỉnh cấu trúc dữ liệu và các luồng xử lý nghiệp vụ chính sau:

### 1. Quản Lý Danh Mục & Hàng Hóa (Catalog & Inventory)
*   Phân loại vật liệu xây dựng theo danh mục (Ví dụ: Cát, đá, gạch, xi măng, sắt thép...).
*   Quản lý danh sách sản phẩm kèm theo đơn giá bán, đơn vị tính và thông số kỹ thuật.
*   Theo dõi số lượng hàng tồn kho thực tế khả dụng.

### 2. Quản Lý Nhập - Xuất Kho (Warehouse Management)
*   Lập phiếu nhập kho khi nhập hàng từ các Nhà cung cấp.
*   Lập phiếu xuất kho tương ứng với các đơn hàng bán ra.
*   Quản lý thông tin chi tiết từng đợt nhập/xuất vật tư để kiểm soát hao hụt.

### 3. Quy Trình Bán Hàng & Đơn Hàng (Sales & Order Processing)
*   Giỏ hàng trực tuyến dành cho khách hàng tự chọn vật tư.
*   Tạo và quản lý đơn đặt hàng của khách hàng với các trạng thái xử lý rõ ràng.
*   Ghi nhận nhật ký lịch sử thanh toán hóa đơn.

### 4. Quản Lý Hợp Đồng & Theo Dõi Công Nợ (Contracts & Receivables/Payables)
*   Quản lý hợp đồng hợp tác dài hạn với các đại lý hoặc nhà thầu xây dựng.
*   Tự động ghi nhận công nợ phải thu (đối với khách hàng mua trả chậm) và công nợ phải trả (đối với nhà cung cấp vật liệu).
*   Thống kê báo cáo công nợ chi tiết theo từng đối tác.

### 5. Quản Lý Hậu Cần & Giao Nhận (Logistics)
*   Lập lịch trình giao nhận vật tư đến tận công trình.
*   Theo dõi trạng thái giao hàng thực tế của từng chuyến xe vận chuyển.

### 6. Bảo Mật & Phân Quyền Nhân Viên (Security & Access Control)
Thiết lập bộ lọc phân quyền chi tiết cho các bộ phận nhân sự trong cửa hàng:
*   **Admin (Quản trị viên):** Quản lý tài khoản nhân viên và thiết lập vai trò (`ROLE_ADMIN`).
*   **Kế toán (Accounting):** Quản lý dòng tiền, công nợ và nhà cung cấp.
*   **Thủ kho (Warehouse):** Xử lý nhập/xuất vật tư và kiểm kê kho hàng.
*   **Nhân viên Kinh doanh (Sales):** Quản lý đơn hàng, danh mục sản phẩm và ký kết hợp đồng.
*   **Ban quản lý (Management):** Theo dõi báo cáo thống kê doanh số tổng quan.
*   **Khách hàng (Customer):** Tra cứu đơn hàng và xem thông tin sản phẩm.

### 7. Khởi Tạo Dữ Liệu Mẫu (Database Seeding)
*   Tự động khởi tạo dữ liệu mẫu về các mặt hàng vật liệu xây dựng phổ biến và các tài khoản demo tương ứng với từng phòng ban để phục vụ quá trình chạy thử nghiệm và kiểm thử hệ thống.
