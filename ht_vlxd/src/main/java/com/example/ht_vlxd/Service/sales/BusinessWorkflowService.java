package com.example.ht_vlxd.Service.sales;

import com.example.ht_vlxd.Model.sales.*;
import com.example.ht_vlxd.Model.inventory.*;
import com.example.ht_vlxd.Model.finance.*;
import com.example.ht_vlxd.Model.product.*;
import com.example.ht_vlxd.Repository.sales.*;
import com.example.ht_vlxd.Repository.inventory.*;
import com.example.ht_vlxd.Repository.finance.*;
import com.example.ht_vlxd.Service.auth.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** All money and stock transitions run in a MongoDB transaction (replica set). */
@Service
@Transactional
public class BusinessWorkflowService {
    private final DonHangRepository orders;
    private final HopDongRepository contracts;
    private final CongNoRepository debts;
    private final ThanhToanRepository payments;
    private final PhieuKhoRepository slips;
    private final TonKhoRepository stock;
    private final GiaoNhanRepository deliveries;
    private final DoiTraHangRepository returns;
    private final CurrentUser user;
    private final MongoTemplate mongo;

    public BusinessWorkflowService(DonHangRepository orders, HopDongRepository contracts,
        CongNoRepository debts, ThanhToanRepository payments, PhieuKhoRepository slips,
        TonKhoRepository stock, GiaoNhanRepository deliveries, DoiTraHangRepository returns,
        CurrentUser user, MongoTemplate mongo) {
        this.orders = orders; this.contracts = contracts; this.debts = debts; this.payments = payments;
        this.slips = slips; this.stock = stock; this.deliveries = deliveries; this.returns = returns;
        this.user = user; this.mongo = mongo;
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    public static BigDecimal positive(BigDecimal value, String label) {
        require(value != null && value.signum() > 0, label + " phải lớn hơn 0."); return value;
    }
    public static String code(String prefix) { return prefix + "-" + UUID.randomUUID(); }
    private static BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private <T> T document(Class<T> type, String field, Object value) {
        return mongo.findOne(Query.query(Criteria.where(field).is(value)), type);
    }
    private DonHang order(String code) {
        var order = orders.findByMaDonHang(code); require(order != null, "Không tìm thấy đơn hàng."); return order;
    }
    private HopDong contract(String code) {
        var contract = contracts.findByMaHopDong(code); require(contract != null, "Không tìm thấy hợp đồng."); return contract;
    }
    public void approveOrder(String code) {
        var order = order(code);
        require("CHO_XAC_NHAN".equals(order.getTrangThai()), "Chỉ xác nhận đơn đang chờ.");
        require(!order.getChiTiet().isEmpty(), "Đơn hàng chưa có chi tiết.");
        order.setTrangThai("DA_XAC_NHAN"); order.setNgayCapNhat(LocalDateTime.now()); orders.save(order);
    }
    public void cancelOrder(String code, boolean customer) {
        var order = order(code); if (customer) user.owns(order);
        require(Set.of("CHO_XAC_NHAN", "DA_XAC_NHAN").contains(order.getTrangThai()), "Không thể hủy đơn đã xuất/giao hàng.");
        var contract = contracts.findByDonHangId(order.getId());
        require(contract == null || "DA_HUY".equals(contract.getTrangThai()), "Phải xử lý hợp đồng và hoàn tiền trước khi hủy đơn.");
        order.setTrangThai("DA_HUY"); orders.save(order);
    }
    public void approveContract(String code) {
        var contract = contract(code);
        require("NHAP".equals(contract.getTrangThai()) || "CHO_DUYET".equals(contract.getTrangThai()), "Hợp đồng đã được xử lý.");
        require(contract.getDonHang() != null && "DA_XAC_NHAN".equals(contract.getDonHang().getTrangThai()), "Đơn hàng chưa được xác nhận.");
        require(!contract.getChiTiet().isEmpty(), "Hợp đồng phải có chi tiết giá và số lượng.");
        positive(contract.getGiaTri(), "Giá trị hợp đồng");
        var detailTotal = contract.getChiTiet().stream().map(l -> positive(l.getSoLuong(), "Số lượng").multiply(positive(l.getDonGia(), "Đơn giá"))).reduce(BigDecimal.ZERO, BigDecimal::add);
        require(detailTotal.compareTo(contract.getGiaTri()) == 0, "Giá trị hợp đồng không khớp chi tiết.");
        require(zero(contract.getTienDatCoc()).signum() >= 0 && zero(contract.getTienDatCoc()).compareTo(contract.getGiaTri()) <= 0, "Cọc thỏa thuận không hợp lệ.");
        require(contract.getNgayHieuLuc() != null && (contract.getNgayHetHan() == null || !contract.getNgayHetHan().isBefore(contract.getNgayHieuLuc())), "Ngày hiệu lực/hết hạn không hợp lệ.");
        contract.setTrangThai("DA_DUYET"); contract.setNguoiDuyetId(user.get().getId());
        contract.setNgayDuyet(LocalDateTime.now()); contracts.save(contract);
    }
    public void signContract(String code) {
        var contract = contract(code); require("DA_DUYET".equals(contract.getTrangThai()), "Ban quản lý phải duyệt trước khi ký.");
        require(contract.getNgayHieuLuc() != null, "Chưa có ngày hiệu lực.");
        contract.setNgayKy(LocalDate.now()); contract.setTrangThai("HIEU_LUC"); contracts.save(contract);
        var order = contract.getDonHang();
        require(order.getKhachHang() != null, "Hợp đồng cần hồ sơ khách hàng.");
        require(debts.findByDonHangId(order.getId()) == null, "Đơn đã có công nợ.");
        var debt = new CongNo(); debt.setKhachHang(order.getKhachHang()); debt.setDonHang(order); debt.setHopDong(contract);
        debt.setSoTienNo(contract.getGiaTri()); debt.setSoTienDaTt(BigDecimal.ZERO);
        debt.setHanThanhToan(contract.getNgayHetHan() != null ? contract.getNgayHetHan() : LocalDate.now().plusDays(30));
        debt.setTrangThai("CHUA_THANH_TOAN"); debts.save(debt);
        order.setTongTien(contract.getGiaTri()); orders.save(order);
    }
    public void cancelContract(String code) {
        var contract = contract(code);
        require(!"DA_HUY".equals(contract.getTrangThai()), "Hợp đồng đã hủy.");
        require(zero(contract.getTienDaThu()).signum() == 0, "Hợp đồng đã thu tiền; cần hoàn tiền và quyết toán trước khi hủy.");
        require(document(LenhXuat.class, "donHangId", contract.getDonHang().getId()) == null, "Hợp đồng đã phát sinh lệnh xuất.");
        var debt = debts.findByDonHangId(contract.getDonHang().getId());
        require(debt == null || zero(debt.getSoTienDaTt()).signum() == 0, "Công nợ đã có chứng từ thu; cần quyết toán trước khi hủy.");
        contract.setTrangThai("DA_HUY"); contracts.save(contract);
        if (debt != null) { debt.setSoTienNo(BigDecimal.ZERO); debt.setTrangThai("DA_HUY"); debts.save(debt); }
    }
    public Map<String, Object> collect(Long id, BigDecimal amount, String method, String transactionCode, String note) {
        positive(amount, "Số tiền");
        require(transactionCode != null && !transactionCode.isBlank(), "Cần mã giao dịch để chống ghi nhận trùng.");
        require(method != null && Set.of("TIEN_MAT", "CHUYEN_KHOAN", "THE").contains(method), "Hình thức thanh toán không hợp lệ.");
        var debt = debts.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công nợ."));
        require(!"DA_HUY".equals(debt.getTrangThai()), "Công nợ đã hủy.");
        if (transactionCode != null && !transactionCode.isBlank()) {
            var previous = payments.findByMaGiaoDich(transactionCode);
            if (previous != null) {
                require(previous.getCongNo().getId().equals(id) && previous.getSoTien().compareTo(amount) == 0
                    && method.equals(previous.getHinhThuc()) && ("PHAI_THU".equals(debt.getLoaiCongNo()) ? "THU" : "CHI").equals(previous.getLoaiPhieu()), "Mã giao dịch đã dùng cho khoản khác.");
                return Map.of("message", "Giao dịch đã ghi nhận.", "maThanhToan", previous.getMaThanhToan(), "soTienThu", previous.getSoTien(), "conLai", zero(debt.getSoTienNo()).subtract(zero(debt.getSoTienDaTt())).max(BigDecimal.ZERO));
            }
        }
        require(amount.compareTo(zero(debt.getSoTienNo()).subtract(zero(debt.getSoTienDaTt()))) <= 0, "Số tiền vượt công nợ còn lại.");
        boolean receivable = "PHAI_THU".equals(debt.getLoaiCongNo());
        var contract = debt.getHopDong();
        if (receivable) {
            require(contract != null && "HIEU_LUC".equals(contract.getTrangThai()), "Cần hợp đồng đã ký trước khi thu tiền.");
            require(!contract.getNgayHieuLuc().isAfter(LocalDate.now()) && (contract.getNgayHetHan() == null || !contract.getNgayHetHan().isBefore(LocalDate.now())), "Hợp đồng chưa có hiệu lực hoặc đã hết hạn.");
        }
        var payment = new ThanhToan(); payment.setMaThanhToan(code(receivable ? "PT" : "PC")); payment.setCongNo(debt);
        payment.setNguoiThu(user.get()); payment.setSoTien(amount); payment.setHinhThuc(method);
        payment.setMaGiaoDich(transactionCode == null || transactionCode.isBlank() ? null : transactionCode);
        payment.setGhiChu(note); payment.setLoaiPhieu(receivable ? "THU" : "CHI"); payments.save(payment);
        debt.setSoTienDaTt(zero(debt.getSoTienDaTt()).add(amount));
        debt.setTrangThai(debt.getSoTienNo().compareTo(debt.getSoTienDaTt()) == 0 ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN"); debts.save(debt);
        if (receivable) {
            contract.setTienDaThu(zero(contract.getTienDaThu()).add(amount)); contracts.save(contract);
            var invoice = new HoaDon(); invoice.setMaHoaDon(code("HDON")); invoice.setHopDongId(contract.getId());
            invoice.setDonHangId(debt.getDonHang().getId()); invoice.setThanhToanId(payment.getId()); invoice.setSoTien(amount); mongo.save(invoice);
            if (contract.getTienDaThu().compareTo(zero(contract.getTienDatCoc())) >= 0
                && document(LenhXuat.class, "donHangId", debt.getDonHang().getId()) == null) {
                var release = new LenhXuat(); release.setMaLenhXuat(code("LX")); release.setDonHangId(debt.getDonHang().getId());
                release.setHopDongId(contract.getId()); release.setHoaDonId(invoice.getId()); release.setChiTiet(contract.getChiTiet()); mongo.save(release);
            }
            var order = debt.getDonHang(); order.setTienDatCoc(contract.getTienDaThu().min(zero(contract.getTienDatCoc()))); orders.save(order);
        }
        return Map.of("message", receivable ? "Đã thu tiền và lập hóa đơn." : "Đã chi trả nhà cung cấp.", "maThanhToan", payment.getMaThanhToan(), "soTienThu", amount, "conLai", debt.getSoTienNo().subtract(debt.getSoTienDaTt()));
    }
    private void changeStock(Kho warehouse, HangHoa product, BigDecimal difference) {
        var current = stock.findByHangHoaIdAndKhoId(product.getId(), warehouse.getId());
        if (current == null) { current = new TonKho(); current.setHangHoa(product); current.setKho(warehouse); current.setSoLuong(BigDecimal.ZERO); }
        var next = zero(current.getSoLuong()).add(difference); require(next.signum() >= 0, "Không đủ tồn kho: " + product.getTenHang());
        current.setSoLuong(next); current.setNgayCapNhat(LocalDateTime.now()); stock.save(current);
    }
    public void approveSlip(String code) {
        var slip = slips.findByMaPhieu(code); require(slip != null, "Không tìm thấy phiếu kho.");
        require("NHAP".equals(slip.getTrangThai()) || "CHO_DUYET".equals(slip.getTrangThai()), "Phiếu đã được xử lý.");
        require(slip.getKho() != null && !slip.getChiTiet().isEmpty(), "Phiếu cần kho và chi tiết.");
        boolean adjustment = slip.getTonKhoKiemKeId() != null;
        boolean export = "XUAT".equals(slip.getLoaiPhieu());
        require(export || "NHAP".equals(slip.getLoaiPhieu()), "Loại phiếu không hợp lệ.");
        LenhXuat release = null;
        if (adjustment) {
            var inventory = stock.findById(slip.getTonKhoKiemKeId()).orElseThrow();
            require(inventory.getSoLuong().compareTo(slip.getSoLuongSoSach()) == 0, "Tồn kho đã thay đổi từ lúc kiểm kê; cần lập lại biên bản.");
        } else if (export) {
            require(slip.getDonHang() != null, "Phiếu xuất cần đơn hàng.");
            release = document(LenhXuat.class, "donHangId", slip.getDonHang().getId());
            require(release != null && "CHO_XUAT".equals(release.getTrangThai()), "Chưa có lệnh xuất hợp lệ hoặc đã xuất.");
            var requested = new HashMap<Long, BigDecimal>();
            for (var line : slip.getChiTiet()) requested.merge(line.getHangHoa().getId(), positive(line.getSoLuong(), "Số lượng"), BigDecimal::add);
            var expected = new HashMap<Long, BigDecimal>();
            for (var line : release.getChiTiet()) expected.merge(line.getHangHoa().getId(), line.getSoLuong(), BigDecimal::add);
            require(requested.keySet().equals(expected.keySet()) && requested.entrySet().stream().allMatch(e -> e.getValue().compareTo(expected.get(e.getKey())) == 0), "Phiếu xuất phải khớp chi tiết lệnh xuất.");
        } else {
            require(slip.getNhaCungCap() != null, "Phiếu nhập cần nhà cung cấp.");
            require(slip.getBienBanNhapHang() != null && !slip.getBienBanNhapHang().isBlank(), "Phiếu nhập cần biên bản kiểm tra số lượng và chất lượng.");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (var line : slip.getChiTiet()) {
            var qty = positive(line.getSoLuong(), "Số lượng");
            require(line.getHangHoa() != null, "Chi tiết thiếu hàng hóa.");
            if (!export && !adjustment) positive(line.getDonGia(), "Đơn giá nhập");
            changeStock(slip.getKho(), line.getHangHoa(), export ? qty.negate() : qty);
            total = total.add(qty.multiply(zero(line.getDonGia())));
        }
        if (!export && !adjustment) {
            var debt = new CongNo(); debt.setNhaCungCapId(slip.getNhaCungCap().getId()); debt.setPhieuNhapId(slip.getId());
            debt.setLoaiCongNo("PHAI_TRA"); debt.setSoTienNo(total); debt.setSoTienDaTt(BigDecimal.ZERO);
            debt.setHanThanhToan(LocalDate.now().plusDays(30)); debt.setTrangThai("CHUA_THANH_TOAN"); debts.save(debt);
        }
        if (release != null) { release.setTrangThai("DA_XUAT"); mongo.save(release); var order = slip.getDonHang(); order.setTrangThai("DA_XUAT_KHO"); orders.save(order); }
        slip.setTrangThai("DA_DUYET"); slip.setNguoiDuyetId(user.get().getId()); slip.setNgayDuyet(LocalDateTime.now()); slips.save(slip);
    }
    public void requireDelivery(DonHang order) {
        require(order != null && "DA_XUAT_KHO".equals(order.getTrangThai()), "Đơn phải xuất kho trước khi lập giao nhận.");
        require(deliveries.findByDonHangId(order.getId()).stream().noneMatch(d -> !"THAT_BAI".equals(d.getTrangThai())), "Đơn đã có giao nhận.");
    }
    public void handover(String code, String condition, String note) {
        require(note != null && !note.isBlank(), "Cần biên bản bàn giao hoặc sự vụ.");
        require(condition != null && Set.of("OK", "LOI").contains(condition), "Tình trạng bàn giao không hợp lệ.");
        var delivery = deliveries.findByMaGiaoNhan(code); require(delivery != null, "Không tìm thấy giao nhận.");
        require("DANG_GIAO".equals(delivery.getTrangThai()), "Giao nhận đã được xử lý.");
        var order = delivery.getDonHang(); require("DANG_GIAO".equals(order.getTrangThai()), "Đơn không ở trạng thái đang giao.");
        if ("OK".equals(condition)) {
            delivery.setDaBanGiao(true); delivery.setNgayBanGiao(LocalDateTime.now()); delivery.setNgayGiaoThuc(LocalDateTime.now());
            delivery.setTrangThai("DA_GIAO"); delivery.setBienBanBanGiao(note); order.setTrangThai("HOAN_THANH");
            if (order.getDoiTraId() != null) {
                var exchange = returns.findById(order.getDoiTraId()).orElseThrow();
                require("CHO_GIAO_DOI".equals(exchange.getTrangThai()), "Yêu cầu đổi hàng không chờ bàn giao.");
                exchange.setTrangThai("HOAN_THANH"); exchange.setNgayXuLy(LocalDateTime.now()); returns.save(exchange);
            }
        } else {
            require(note != null && !note.isBlank(), "Phải ghi rõ sự vụ bàn giao.");
            delivery.setDaBanGiao(false); delivery.setTrangThai("THAT_BAI"); delivery.getBienBanSuVu().add(note);
            order.setTrangThai("DA_XUAT_KHO");
        }
        deliveries.save(delivery); orders.save(order);
    }
    public void completeOrder(String code) {
        var order = order(code);
        require("HOAN_THANH".equals(order.getTrangThai()) && deliveries.findByDonHangId(order.getId()).stream().anyMatch(d -> "DA_GIAO".equals(d.getTrangThai())), "Phải có biên bản bàn giao thành công; không được hoàn thành đơn thủ công.");
    }
    public void validateReturn(DonHang order, HangHoa product, BigDecimal qty, String type) {
        user.owns(order); positive(qty, "Số lượng trả");
        require(type != null && Set.of("TRA", "DOI").contains(type), "Loại đổi trả không hợp lệ.");
        require(Set.of("HOAN_THANH", "DA_XUAT_KHO", "DANG_GIAO").contains(order.getTrangThai()), "Đơn chưa xuất/giao hàng.");
        var ordered = order.getChiTiet().stream().filter(l -> l.getHangHoa().getId().equals(product.getId())).map(DonHangChiTiet::getSoLuong).reduce(BigDecimal.ZERO, BigDecimal::add);
        var reserved = returns.findByDonHangId(order.getId()).stream().filter(r -> r.getHangHoa().getId().equals(product.getId()) && !"TU_CHOI".equals(r.getTrangThai())).map(DoiTraHang::getSoLuong).reduce(BigDecimal.ZERO, BigDecimal::add);
        require(qty.add(reserved).compareTo(ordered) <= 0, "Số lượng đổi trả vượt lượng đã mua hoặc đã yêu cầu.");
        order.setNgayCapNhat(LocalDateTime.now()); orders.save(order);
    }
    public void approveReturn(String code, boolean approved) {
        var request = returns.findByMaDoiTra(code); require(request != null, "Không tìm thấy yêu cầu đổi trả.");
        require("CHO_DUYET".equals(request.getTrangThai()), "Yêu cầu đã được xử lý.");
        request.setTrangThai(approved ? "DA_DUYET" : "TU_CHOI"); request.setNguoiXuLy(user.get()); request.setNgayXuLy(LocalDateTime.now()); returns.save(request);
    }
    public void receiveReturn(String code, Long warehouseId, String classification, String report) {
        var request = returns.findByMaDoiTra(code); require(request != null && "DA_DUYET".equals(request.getTrangThai()), "Yêu cầu phải được duyệt và chưa thu hồi.");
        require(classification != null && Set.of("BAN_LAI", "HONG").contains(classification), "Phải phân loại hàng thu hồi.");
        require(report != null && !report.isBlank(), "Cần biên bản kiểm tra thu hồi.");
        var warehouse = mongo.findById(warehouseId, Kho.class); require(warehouse != null, "Kho không hợp lệ.");
        var order = request.getDonHang();
        var original = order.getChiTiet().stream().filter(l -> l.getHangHoa().getId().equals(request.getHangHoa().getId())).findFirst().orElseThrow();
        var contract = contracts.findByDonHangId(order.getId());
        BigDecimal rate = original.getDonGia();
        if (contract != null) rate = contract.getChiTiet().stream().filter(l -> l.getHangHoa().getId().equals(request.getHangHoa().getId())).findFirst().orElseThrow().getDonGia();
        request.setDonGiaGoc(rate); request.setKhoNhanId(warehouseId); request.setPhanLoai(classification); request.setGhiChuXuLy(report);
        if ("BAN_LAI".equals(classification)) changeStock(warehouse, request.getHangHoa(), request.getSoLuong());
        if ("TRA".equals(request.getLoai())) {
            var debt = debts.findByDonHangId(order.getId()); require(debt != null, "Không tìm thấy công nợ liên quan.");
            debt.setSoTienNo(debt.getSoTienNo().subtract(rate.multiply(request.getSoLuong())).max(BigDecimal.ZERO));
            debt.setSoTienHoanTra(zero(debt.getSoTienDaTt()).subtract(debt.getSoTienNo()).subtract(zero(debt.getSoTienDaHoan())).max(BigDecimal.ZERO));
            debt.setTrangThai(debt.getSoTienHoanTra().signum() > 0 ? "CHO_HOAN_TIEN" : debt.getSoTienNo().compareTo(debt.getSoTienDaTt()) <= 0 ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN"); debts.save(debt);
        } else {
            var replacement = new DonHang(); replacement.setMaDonHang(code("DH-DOI")); replacement.setKhachHang(order.getKhachHang());
            replacement.setDoiTraId(request.getId()); replacement.setTongTien(BigDecimal.ZERO); replacement.setTrangThai("DA_XAC_NHAN");
            replacement.setDiaChiGiao(order.getDiaChiGiao()); replacement.setGhiChu("Giao hàng thay thế cho yêu cầu " + request.getMaDoiTra());
            var line = new DonHangChiTiet(); line.setHangHoa(request.getHangHoa()); line.setSoLuong(request.getSoLuong());
            line.setDonGia(BigDecimal.ZERO); line.setThanhTien(BigDecimal.ZERO); replacement.setChiTiet(List.of(line)); orders.save(replacement);
            var originalInvoice = document(HoaDon.class, "donHangId", order.getId()); require(originalInvoice != null, "Thiếu hóa đơn gốc để lập lệnh giao hàng thay thế.");
            var release = new LenhXuat(); release.setMaLenhXuat(code("LX-DOI")); release.setDonHangId(replacement.getId());
            release.setHopDongId(contract.getId()); release.setHoaDonId(originalInvoice.getId()); release.setChiTiet(replacement.getChiTiet()); mongo.save(release);
        }
        request.setTrangThai("TRA".equals(request.getLoai()) ? "HOAN_THANH" : "CHO_GIAO_DOI"); request.setNgayXuLy(LocalDateTime.now()); returns.save(request);
    }
    public void refund(Long id, BigDecimal amount, String transactionCode) {
        positive(amount, "Số tiền hoàn"); require(transactionCode != null && !transactionCode.isBlank(), "Cần mã giao dịch hoàn tiền.");
        var debt = debts.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy công nợ."));
        var existing = payments.findByMaGiaoDich(transactionCode);
        if (existing != null) {
            require("CHI_HOAN".equals(existing.getLoaiPhieu()) && existing.getCongNo().getId().equals(id) && existing.getSoTien().compareTo(amount) == 0, "Mã giao dịch đã sử dụng."); return;
        }
        require("PHAI_THU".equals(debt.getLoaiCongNo()) && amount.compareTo(zero(debt.getSoTienHoanTra())) <= 0, "Số tiền vượt khoản phải hoàn.");
        var payment = new ThanhToan(); payment.setMaThanhToan(code("PC-HOAN")); payment.setCongNo(debt); payment.setNguoiThu(user.get());
        payment.setSoTien(amount); payment.setLoaiPhieu("CHI_HOAN"); payment.setHinhThuc("CHUYEN_KHOAN"); payment.setMaGiaoDich(transactionCode); payments.save(payment);
        debt.setSoTienDaHoan(zero(debt.getSoTienDaHoan()).add(amount)); debt.setSoTienHoanTra(debt.getSoTienHoanTra().subtract(amount));
        debt.setTrangThai(debt.getSoTienHoanTra().signum() == 0 ? "DA_THANH_TOAN" : "CHO_HOAN_TIEN"); debts.save(debt);
    }
}
