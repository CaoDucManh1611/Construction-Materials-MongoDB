package com.example.ht_vlxd.Controller.sales;
import com.example.ht_vlxd.Model.finance.CongNo;
import com.example.ht_vlxd.Model.sales.DoiTraHang;
import com.example.ht_vlxd.Model.sales.DonHang;
import com.example.ht_vlxd.Model.sales.DonHangChiTiet;
import com.example.ht_vlxd.Model.product.HangHoa;
import com.example.ht_vlxd.Model.sales.HopDong;
import com.example.ht_vlxd.Model.customer.KhachHang;
import com.example.ht_vlxd.Model.inventory.Kho;
import com.example.ht_vlxd.Model.auth.NguoiDung;
import com.example.ht_vlxd.Model.inventory.PhieuKho;
import com.example.ht_vlxd.Model.inventory.TonKho;
import com.example.ht_vlxd.Repository.finance.CongNoRepository;
import com.example.ht_vlxd.Repository.sales.DoiTraHangRepository;
import com.example.ht_vlxd.Repository.sales.DonHangChiTietRepository;
import com.example.ht_vlxd.Repository.sales.DonHangRepository;
import com.example.ht_vlxd.Repository.product.HangHoaRepository;
import com.example.ht_vlxd.Repository.sales.HopDongRepository;
import com.example.ht_vlxd.Repository.customer.KhachHangRepository;
import com.example.ht_vlxd.Repository.inventory.KhoRepository;
import com.example.ht_vlxd.Repository.auth.NguoiDungRepository;
import com.example.ht_vlxd.Repository.inventory.PhieuKhoRepository;
import com.example.ht_vlxd.Repository.inventory.TonKhoRepository;

import com.example.ht_vlxd.Service.sales.DonHangService;
import com.example.ht_vlxd.Service.product.HangHoaService;
import com.example.ht_vlxd.Service.auth.NguoiDungService;
import com.example.ht_vlxd.Service.sales.HopDongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/sales")
public class SalesRestController {
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.sales.BusinessWorkflowService workflow;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.auth.CurrentUser currentUser;


    private final DonHangRepository donHangRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final HangHoaRepository hangHoaRepository;
    private final DoiTraHangRepository doiTraHangRepository;
    private final HopDongRepository hopDongRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final CongNoRepository congNoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final KhoRepository khoRepository;

    private final NguoiDungService nguoiDungService;
    private final DonHangService donHangService;
    private final HopDongService hopDongService;
    private final HangHoaService hangHoaService;

    public SalesRestController(DonHangRepository donHangRepository,
                               DonHangChiTietRepository donHangChiTietRepository,
                               KhachHangRepository khachHangRepository,
                               HangHoaRepository hangHoaRepository,
                               DoiTraHangRepository doiTraHangRepository,
                               HopDongRepository hopDongRepository,
                               NguoiDungRepository nguoiDungRepository,
                               CongNoRepository congNoRepository,
                               TonKhoRepository tonKhoRepository,
                               PhieuKhoRepository phieuKhoRepository,
                               KhoRepository khoRepository,
                               NguoiDungService nguoiDungService,
                               DonHangService donHangService,
                               HopDongService hopDongService,
                               HangHoaService hangHoaService) {
        this.donHangRepository = donHangRepository;
        this.donHangChiTietRepository = donHangChiTietRepository;
        this.khachHangRepository = khachHangRepository;
        this.hangHoaRepository = hangHoaRepository;
        this.doiTraHangRepository = doiTraHangRepository;
        this.hopDongRepository = hopDongRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.congNoRepository = congNoRepository;
        this.tonKhoRepository = tonKhoRepository;
        this.phieuKhoRepository = phieuKhoRepository;
        this.khoRepository = khoRepository;
        this.nguoiDungService = nguoiDungService;
        this.donHangService = donHangService;
        this.hopDongService = hopDongService;
        this.hangHoaService = hangHoaService;
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        List<DonHang> donHangs = donHangRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DonHang dh : donHangs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dh.getId());
            map.put("maDonHang", dh.getMaDonHang());
            map.put("khachHangTen", dh.getKhachHang() != null && dh.getKhachHang().getNguoiDung() != null ?
                    dh.getKhachHang().getNguoiDung().getHoTen() : "Khách vãng lai");
            map.put("khachHangLoai", dh.getKhachHang() != null ? dh.getKhachHang().getLoaiKhach() : "");
            map.put("ngayDat", dh.getNgayDat() != null ? dh.getNgayDat().format(formatter) : "");
            map.put("tongTien", dh.getTongTien());
            map.put("tienDatCoc", dh.getTienDatCoc());
            map.put("trangThai", dh.getTrangThai());
            map.put("diaChiGiao", dh.getDiaChiGiao());
            map.put("ghiChu", dh.getGhiChu());

            List<DonHangChiTiet> details = donHangChiTietRepository.findByDonHangId(dh.getId());
            List<Map<String, Object>> detailList = new ArrayList<>();
            for (DonHangChiTiet ct : details) {
                Map<String, Object> ctMap = new HashMap<>();
                ctMap.put("id", ct.getId());
                ctMap.put("tenHang", ct.getHangHoa().getTenHang());
                ctMap.put("maHang", ct.getHangHoa().getMaHang());
                ctMap.put("soLuong", ct.getSoLuong());
                ctMap.put("donGia", ct.getDonGia());
                ctMap.put("thanhTien", ct.getThanhTien());
                detailList.add(ctMap);
            }
            map.put("chiTiet", detailList);
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/approve")
    public ResponseEntity<?> approveOrder(@RequestBody Map<String, String> body) {
        workflow.approveOrder(body.get("maDonHang"));
        return ResponseEntity.ok(java.util.Map.of("message", "Đã xác nhận đơn hàng."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/cancel")
    public ResponseEntity<?> cancelOrder(@RequestBody Map<String, String> body) {
        workflow.cancelOrder(body.get("maDonHang"), false);
        return ResponseEntity.ok(java.util.Map.of("message", "Đã hủy đơn hàng."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/complete")
    public ResponseEntity<?> completeOrder(@RequestBody Map<String, String> body) {
        workflow.completeOrder(body.get("maDonHang"));
        return ResponseEntity.ok(java.util.Map.of("message", "Đơn đã có bàn giao thành công."));
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers() {
        List<KhachHang> khachHangs = khachHangRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        for (KhachHang kh : khachHangs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", kh.getId());
            map.put("maKhachHang", kh.getMaKhachHang());
            map.put("hoTen", kh.getNguoiDung() != null ? kh.getNguoiDung().getHoTen() : "Không tên");
            map.put("soDienThoai", kh.getNguoiDung() != null ? kh.getNguoiDung().getSoDienThoai() : "");
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/create")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        BigDecimal soLuong = new BigDecimal(body.get("soLuong").toString());
        String diaChiGiao = (String) body.get("diaChiGiao");
        String ghiChu = (String) body.get("ghiChu");
        String username = currentUser.username(); // salesperson username

        KhachHang kh = khachHangRepository.findById(customerId).orElse(null);
        if (kh == null) {
            return ResponseEntity.badRequest().body("Khách hàng không hợp lệ.");
        }

        HangHoa hh = hangHoaService.findById(productId);
        if (hh == null || hh.getTrangThai() != com.example.ht_vlxd.Model.product.TrangThaiHangHoa.KINH_DOANH) {
            return ResponseEntity.badRequest().body("Sản phẩm không hợp lệ.");
        }

        NguoiDung nvKinhDoanh = nguoiDungService.findByUsername(username);

        BigDecimal donGia = hh.getGiaBanLe();
        com.example.ht_vlxd.Service.sales.BusinessWorkflowService.positive(soLuong, "Số lượng");
        BigDecimal tongTien = donGia.multiply(soLuong);

        DonHang dh = new DonHang();
        dh.setKhachHang(kh);
        dh.setNvKinhDoanh(nvKinhDoanh);
        dh.setMaDonHang("DH-" + System.currentTimeMillis());
        dh.setDiaChiGiao(diaChiGiao);
        dh.setTongTien(tongTien);
        dh.setTienDatCoc(BigDecimal.ZERO);
        dh.setTrangThai("DA_XAC_NHAN"); // salesperson orders are auto-approved
        dh.setGhiChu(ghiChu);
        dh.setNgayDat(LocalDateTime.now());

        DonHang savedDh = donHangRepository.save(dh);

        DonHangChiTiet ct = new DonHangChiTiet();
        ct.setDonHang(savedDh);
        ct.setHangHoa(hh);
        ct.setSoLuong(soLuong);
        ct.setDonGia(donGia);
        ct.setThanhTien(tongTien);
        ct.setGhiChu(ghiChu);
        donHangChiTietRepository.save(ct);

        return ResponseEntity.ok("Khởi tạo và phê duyệt đơn hàng thành công! Mã đơn: " + savedDh.getMaDonHang());
    }

    @GetMapping("/returns")
    public ResponseEntity<?> getReturns() {
        List<DoiTraHang> returns = doiTraHangRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DoiTraHang dth : returns) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dth.getId());
            map.put("maDoiTra", dth.getMaDoiTra());
            map.put("maDonHang", dth.getDonHang() != null ? dth.getDonHang().getMaDonHang() : "");
            map.put("khachHangTen", dth.getKhachHang() != null && dth.getKhachHang().getNguoiDung() != null ?
                    dth.getKhachHang().getNguoiDung().getHoTen() : "");
            map.put("tenHang", dth.getHangHoa() != null ? dth.getHangHoa().getTenHang() : "");
            map.put("maHang", dth.getHangHoa() != null ? dth.getHangHoa().getMaHang() : "");
            map.put("soLuong", dth.getSoLuong());
            map.put("loai", dth.getLoai());
            map.put("lyDo", dth.getLyDo());
            map.put("trangThai", dth.getTrangThai());
            map.put("ngayYeuCau", dth.getNgayYeuCau() != null ? dth.getNgayYeuCau().format(formatter) : "");
            map.put("ghiChuXuLy", dth.getGhiChuXuLy() != null ? dth.getGhiChuXuLy() : "");
            map.put("ngayXuLy", dth.getNgayXuLy() != null ? dth.getNgayXuLy().format(formatter) : "");
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/returns/approve")
    public ResponseEntity<?> approveReturn(@RequestBody Map<String, String> body) {
        workflow.approveReturn(body.get("maDoiTra"), true);
        return ResponseEntity.ok(java.util.Map.of("message", "Đã duyệt; kho cần kiểm tra và thu hồi trước khi quyết toán."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/returns/reject")
    public ResponseEntity<?> rejectReturn(@RequestBody Map<String, String> body) {
        workflow.approveReturn(body.get("maDoiTra"), false);
        return ResponseEntity.ok(java.util.Map.of("message", "Đã từ chối yêu cầu."));
    }

    @GetMapping("/contracts")
    public ResponseEntity<?> getAllContracts() {
        List<HopDong> contracts = hopDongRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (HopDong hd : contracts) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", hd.getId());
            map.put("maHopDong", hd.getMaHopDong());
            map.put("maDonHang", hd.getDonHang() != null ? hd.getDonHang().getMaDonHang() : "Không có");
            map.put("khachHangTen", hd.getKhachHang() != null && hd.getKhachHang().getNguoiDung() != null ?
                    hd.getKhachHang().getNguoiDung().getHoTen() : "");
            map.put("ngayKy", hd.getNgayKy() != null ? hd.getNgayKy().format(dateFormatter) : "");
            map.put("ngayHieuLuc", hd.getNgayHieuLuc() != null ? hd.getNgayHieuLuc().format(dateFormatter) : "");
            map.put("giaTri", hd.getGiaTri());
            map.put("tienDatCoc", hd.getTienDatCoc() != null ? hd.getTienDatCoc() : BigDecimal.ZERO);
            map.put("trangThai", hd.getTrangThai());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/approved-no-contract")
    public ResponseEntity<?> getApprovedOrdersNoContract() {
        // Lấy tất cả đơn hàng đã duyệt
        List<DonHang> approvedOrders = donHangRepository.findAll().stream()
                .filter(dh -> "DA_XAC_NHAN".equals(dh.getTrangThai()) && dh.getDoiTraId() == null)
                .toList();

        List<Map<String, Object>> response = new ArrayList<>();
        for (DonHang dh : approvedOrders) {
            // Kiểm tra xem đơn hàng đã có hợp đồng chưa
            HopDong hd = hopDongRepository.findByDonHangId(dh.getId());
            if (hd == null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", dh.getId());
                map.put("maDonHang", dh.getMaDonHang());
                map.put("khachHangId", dh.getKhachHang().getId());
                map.put("khachHangTen", dh.getKhachHang().getNguoiDung().getHoTen());
                map.put("tongTien", dh.getTongTien());
                response.add(map);
            }
        }
        return ResponseEntity.ok(response);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/contracts/create")
    public ResponseEntity<?> createContract(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String ngayKyStr = (String) body.get("ngayKy");
        String ngayHieuLucStr = (String) body.get("ngayHieuLuc");
        BigDecimal chietKhauPercent = new BigDecimal(body.get("chietKhau").toString());
        BigDecimal inputTienCoc = new BigDecimal(body.get("tienDatCoc").toString());
        String dieuKhoan = (String) body.get("dieuKhoan");
        String username = currentUser.username(); // salesperson username

        DonHang dh = donHangRepository.findById(orderId).orElse(null);
        if (dh == null) {
            return ResponseEntity.badRequest().body("Đơn hàng liên kết không tồn tại.");
        }

        workflow.require("DA_XAC_NHAN".equals(dh.getTrangThai()) && dh.getKhachHang() != null && dh.getDoiTraId() == null, "Cần đơn mua đã xác nhận và hồ sơ khách hàng.");
        workflow.require(hopDongRepository.findByDonHangId(dh.getId()) == null, "Đơn đã có hợp đồng.");
        workflow.require(chietKhauPercent.signum() >= 0 && chietKhauPercent.compareTo(new BigDecimal("100")) < 0, "Chiết khấu phải từ 0 đến dưới 100%.");
        NguoiDung nvLap = nguoiDungService.findByUsername(username);

        HopDong hd = new HopDong();
        hd.setMaHopDong("HD-" + System.currentTimeMillis());
        hd.setDonHang(dh);
        hd.setKhachHang(dh.getKhachHang());
        hd.setNvLap(nvLap);
        hd.setNgayKy(null);
        hd.setNgayHieuLuc(LocalDate.parse(ngayHieuLucStr));

        // Giá trị hợp đồng tính toán bao gồm chiết khấu
        BigDecimal discountFactor = BigDecimal.ONE.subtract(chietKhauPercent.divide(new BigDecimal("100")));
        BigDecimal contractValue = dh.getTongTien().multiply(discountFactor);
        hd.setGiaTri(contractValue);

        workflow.require(inputTienCoc.signum() >= 0 && inputTienCoc.compareTo(contractValue) <= 0, "Tiền cọc yêu cầu phải nằm trong giá trị hợp đồng.");
        java.util.List<DonHangChiTiet> agreed = new java.util.ArrayList<>();
        for (var line : donHangChiTietRepository.findByDonHangId(dh.getId())) {
            var copy = new DonHangChiTiet(); copy.setHangHoa(line.getHangHoa()); copy.setSoLuong(line.getSoLuong());
            copy.setDonGia(line.getDonGia().multiply(discountFactor)); copy.setThanhTien(copy.getDonGia().multiply(copy.getSoLuong())); agreed.add(copy);
        }
        hd.setChiTiet(agreed);
        hd.setTienDatCoc(inputTienCoc);
        hd.setDieuKhoanTt(dieuKhoan);
        hd.setTrangThai("NHAP"); // Initial status draft

        hopDongRepository.save(hd);
        return ResponseEntity.ok("Soạn thảo hợp đồng thành công với mã: " + hd.getMaHopDong());
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/contracts/activate")
    public ResponseEntity<?> activateContract(@RequestBody Map<String, String> body) {
        workflow.signContract(body.get("maHopDong"));
        return ResponseEntity.ok(java.util.Map.of("message", "Đã ghi nhận ký hợp đồng."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/contracts/cancel")
    public ResponseEntity<?> cancelContract(@RequestBody Map<String, String> body) {
        workflow.cancelContract(body.get("maHopDong"));
        return ResponseEntity.ok(java.util.Map.of("message", "Đã hủy hợp đồng và giữ lịch sử."));
    }
}
