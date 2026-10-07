package com.example.ht_vlxd.Controller.common;

import com.example.ht_vlxd.Model.sales.*;
import com.example.ht_vlxd.Model.finance.*;
import com.example.ht_vlxd.Model.inventory.*;
import com.example.ht_vlxd.Model.product.*;
import com.example.ht_vlxd.Repository.customer.KhachHangRepository;
import com.example.ht_vlxd.Repository.product.HangHoaRepository;
import com.example.ht_vlxd.Service.auth.CurrentUser;
import com.example.ht_vlxd.Service.sales.BusinessWorkflowService;
import com.example.ht_vlxd.Service.product.CatalogApprovalService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/business")
public class BusinessDocumentsController {
    private final MongoTemplate mongo;
    private final CurrentUser user;
    private final BusinessWorkflowService workflow;
    private final CatalogApprovalService catalog;
    private final HangHoaRepository products;
    private final KhachHangRepository customers;
    public BusinessDocumentsController(MongoTemplate mongo, CurrentUser user, BusinessWorkflowService workflow,
        CatalogApprovalService catalog, HangHoaRepository products, KhachHangRepository customers) {
        this.mongo = mongo; this.user = user; this.workflow = workflow; this.catalog = catalog; this.products = products; this.customers = customers;
    }
    @GetMapping("/quotes")
    @PreAuthorize("hasAnyRole('NV_KINH_DOANH','KHACH_HANG','BAN_QUAN_LY')")
    public List<BangBaoGia> quotes() {
        if ("KHACH_HANG".equals(user.get().getRole().getName())) {
            var customer = customers.findByNguoiDungUsername(user.username());
            BusinessWorkflowService.require(customer != null, "Không tìm thấy hồ sơ khách hàng.");
            return mongo.find(Query.query(Criteria.where("khachHangId").is(customer.getId())), BangBaoGia.class);
        }
        return mongo.findAll(BangBaoGia.class);
    }
    @PostMapping("/quotes") @Transactional
    @PreAuthorize("hasRole('NV_KINH_DOANH')")
    public BangBaoGia createQuote(@RequestBody Map<String, Object> body) {
        BusinessWorkflowService.require(body.get("khachHangId") != null && body.get("chiTiet") instanceof List<?>, "Cần khách hàng và danh sách vật liệu.");
        var customer = customers.findById(Long.valueOf(body.get("khachHangId").toString())).orElseThrow(() -> new IllegalArgumentException("Khách hàng không hợp lệ."));
        @SuppressWarnings("unchecked") var items = (List<Map<String, Object>>) body.get("chiTiet");
        BusinessWorkflowService.require(items != null && !items.isEmpty(), "Báo giá phải có vật liệu.");
        var quote = new BangBaoGia(); quote.setMaBaoGia(BusinessWorkflowService.code("BG")); quote.setKhachHangId(customer.getId()); quote.setNguoiLapId(user.get().getId());
        quote.setHieuLucDen(LocalDateTime.now().plusDays(7)); quote.setTrangThai("DA_GUI");
        var total = BigDecimal.ZERO;
        for (var item : items) {
            var product = products.findById(Long.valueOf(item.get("hangHoaId").toString())).orElseThrow(() -> new IllegalArgumentException("Hàng hóa không tồn tại."));
            BusinessWorkflowService.require(product.getTrangThai() == TrangThaiHangHoa.KINH_DOANH, "Hàng đã ngừng kinh doanh.");
            var qty = BusinessWorkflowService.positive(new BigDecimal(item.get("soLuong").toString()), "Số lượng");
            var price = "DOANH_NGHIEP".equals(customer.getLoaiKhach()) && product.getGiaBanSi() != null ? product.getGiaBanSi() : product.getGiaBanLe();
            var line = new DonHangChiTiet(); line.setHangHoa(product); line.setSoLuong(qty); line.setDonGia(price); line.setThanhTien(qty.multiply(price));
            quote.getChiTiet().add(line); total = total.add(line.getThanhTien());
        }
        quote.setTongTien(total); return mongo.save(quote);
    }
    @PostMapping("/quotes/{id}/accept") @Transactional
    @PreAuthorize("hasRole('KHACH_HANG')")
    public Map<String, Object> accept(@PathVariable Long id) {
        var quote = mongo.findById(id, BangBaoGia.class); var customer = customers.findByNguoiDungUsername(user.username());
        BusinessWorkflowService.require(quote != null && customer != null && customer.getId().equals(quote.getKhachHangId()), "Báo giá không thuộc khách hàng hiện tại.");
        BusinessWorkflowService.require("DA_GUI".equals(quote.getTrangThai()) && quote.getHieuLucDen().isAfter(LocalDateTime.now()), "Báo giá đã xử lý hoặc hết hạn.");
        var order = new DonHang(); order.setMaDonHang(BusinessWorkflowService.code("DH")); order.setKhachHang(customer); order.setBaoGiaId(id);
        order.setChiTiet(quote.getChiTiet()); order.setTongTien(quote.getTongTien()); order.setTienDatCoc(BigDecimal.ZERO); order.setDiaChiGiao(customer.getNguoiDung().getDiaChi()); mongo.save(order);
        quote.setTrangThai("DA_CHAP_NHAN"); mongo.save(quote); return Map.of("message", "Đã chấp nhận báo giá và tạo đơn chờ xác nhận.", "maDonHang", order.getMaDonHang());
    }
    @GetMapping("/returns-to-receive") @PreAuthorize("hasRole('NV_KHO')")
    public List<DoiTraHang> returns() { return mongo.find(Query.query(Criteria.where("trangThai").is("DA_DUYET")), DoiTraHang.class); }
    @PostMapping("/returns/receive") @PreAuthorize("hasRole('NV_KHO')")
    public Map<String, String> receive(@RequestBody Map<String, String> body) {
        workflow.receiveReturn(body.get("maDoiTra"), Long.valueOf(body.get("khoId")), body.get("phanLoai"), body.get("bienBan")); return Map.of("message", "Đã lưu biên bản thu hồi và quyết toán đổi trả.");
    }
    @GetMapping("/supplier-debts") @PreAuthorize("hasRole('NV_KE_TOAN')")
    public List<CongNo> supplierDebts() { return mongo.find(Query.query(Criteria.where("loaiCongNo").is("PHAI_TRA")), CongNo.class); }
    @GetMapping("/invoices") @PreAuthorize("hasAnyRole('NV_KE_TOAN','BAN_QUAN_LY')")
    public List<HoaDon> invoices() { return mongo.findAll(HoaDon.class); }
    @GetMapping("/release-orders") @PreAuthorize("hasAnyRole('NV_KHO','BAN_QUAN_LY')")
    public List<LenhXuat> releases() { return mongo.findAll(LenhXuat.class); }
    @GetMapping("/catalog-proposals") @PreAuthorize("hasRole('BAN_QUAN_LY')")
    public List<DeXuatDanhMuc> proposals() { return catalog.pending(); }
    @PostMapping("/catalog-proposals/{id}/decide") @PreAuthorize("hasRole('BAN_QUAN_LY')")
    public Map<String, String> decide(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        catalog.decide(id, Boolean.TRUE.equals(body.get("duyet"))); return Map.of("message", "Đã xử lý đề xuất danh mục.");
    }
    @PostMapping("/debts/refund") @PreAuthorize("hasRole('NV_KE_TOAN')")
    public Map<String, String> refund(@RequestBody Map<String, String> body) {
        workflow.refund(Long.valueOf(body.get("congNoId")), new BigDecimal(body.get("soTien")), body.get("maGiaoDich")); return Map.of("message", "Đã ghi nhận hoàn tiền khách hàng.");
    }
    private String department() {
        return switch (user.get().getRole().getName()) {
            case "NV_KINH_DOANH" -> "KINH_DOANH";
            case "NV_KHO" -> "KHO";
            case "NV_KE_TOAN" -> "TAI_CHINH";
            case "BAN_QUAN_LY" -> "QUAN_LY";
            default -> throw new org.springframework.security.access.AccessDeniedException("Không có quyền báo cáo.");
        };
    }
    @GetMapping("/reports") @PreAuthorize("hasAnyRole('NV_KINH_DOANH','NV_KHO','NV_KE_TOAN','BAN_QUAN_LY')")
    public List<com.example.ht_vlxd.Model.management.BaoCao> reports() {
        return "QUAN_LY".equals(department()) ? mongo.findAll(com.example.ht_vlxd.Model.management.BaoCao.class)
            : mongo.find(Query.query(Criteria.where("loai").is(department())), com.example.ht_vlxd.Model.management.BaoCao.class);
    }
    @PostMapping("/reports") @Transactional @PreAuthorize("hasAnyRole('NV_KINH_DOANH','NV_KHO','NV_KE_TOAN')")
    public Map<String, String> report(@RequestBody Map<String, String> body) {
        var from = java.time.LocalDate.parse(body.get("tuNgay")); var to = java.time.LocalDate.parse(body.get("denNgay"));
        BusinessWorkflowService.require(!to.isBefore(from), "Kỳ báo cáo không hợp lệ.");
        var role = department(); var content = new LinkedHashMap<String, Object>();
        var range = Criteria.where("ngayDat").gte(from.atStartOfDay()).lt(to.plusDays(1).atStartOfDay());
        if ("KINH_DOANH".equals(role)) {
            var orders = mongo.find(Query.query(range), DonHang.class);
            content.put("donHangTrongKy", orders.stream().map(o -> Map.of("maDonHang", o.getMaDonHang(), "trangThai", o.getTrangThai(), "giaTri", o.getTongTien())).toList());
            content.put("doanhThuDonHoanThanh", orders.stream().filter(o -> "HOAN_THANH".equals(o.getTrangThai())).map(DonHang::getTongTien).reduce(BigDecimal.ZERO, BigDecimal::add));
        } else if ("KHO".equals(role)) {
            content.put("tonKhoHienTai", mongo.findAll(TonKho.class).stream().map(s -> Map.of("hangHoa", s.getHangHoa().getTenHang(), "kho", s.getKho().getTenKho(), "soLuong", s.getSoLuong())).toList());
            content.put("phieuKhoTrongKy", mongo.find(Query.query(Criteria.where("ngayLap").gte(from.atStartOfDay()).lt(to.plusDays(1).atStartOfDay())), PhieuKho.class).stream().map(p -> Map.of("maPhieu", p.getMaPhieu(), "loai", p.getLoaiPhieu(), "trangThai", p.getTrangThai())).toList());
        } else {
            var payments = mongo.find(Query.query(Criteria.where("ngayThanhToan").gte(from.atStartOfDay()).lt(to.plusDays(1).atStartOfDay())), ThanhToan.class);
            content.put("giaoDichTrongKy", payments.stream().map(p -> Map.of("maPhieu", p.getMaThanhToan(), "loai", p.getLoaiPhieu(), "soTien", p.getSoTien())).toList());
            content.put("tongThu", payments.stream().filter(p -> "THU".equals(p.getLoaiPhieu())).map(ThanhToan::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add));
            content.put("tongChi", payments.stream().filter(p -> !"THU".equals(p.getLoaiPhieu())).map(ThanhToan::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        var report = new com.example.ht_vlxd.Model.management.BaoCao(); report.setLoai(role); report.setTieuDe("Báo cáo " + role + " từ " + from + " đến " + to);
        report.setTuNgay(from); report.setDenNgay(to); report.setNguoiLap(user.get()); report.setTrangThai("CHO_DUYET");
        report.setNoiDungJson(new tools.jackson.databind.ObjectMapper().writeValueAsString(content)); mongo.save(report);
        return Map.of("message", "Đã lập báo cáo và gửi ban quản lý duyệt.");
    }
    @GetMapping(value = "/reports/{id}/export", produces = "application/json")
    @PreAuthorize("hasAnyRole('NV_KINH_DOANH','NV_KHO','NV_KE_TOAN','BAN_QUAN_LY')")
    public org.springframework.http.ResponseEntity<String> export(@PathVariable Long id) {
        var report = mongo.findById(id, com.example.ht_vlxd.Model.management.BaoCao.class);
        BusinessWorkflowService.require(report != null, "Báo cáo không tồn tại.");
        if (!"QUAN_LY".equals(department()) && !department().equals(report.getLoai())) throw new org.springframework.security.access.AccessDeniedException("Báo cáo thuộc phòng ban khác.");
        return org.springframework.http.ResponseEntity.ok().header("Content-Disposition", "attachment; filename=report-" + id + ".json").body(report.getNoiDungJson());
    }
}
