package com.example.ht_vlxd.Controller.management;
import com.example.ht_vlxd.Model.management.BaoCao;
import com.example.ht_vlxd.Model.finance.CongNo;
import com.example.ht_vlxd.Model.sales.DoiTraHang;
import com.example.ht_vlxd.Model.sales.DonHang;
import com.example.ht_vlxd.Model.product.HangHoa;
import com.example.ht_vlxd.Model.sales.HopDong;
import com.example.ht_vlxd.Model.inventory.Kho;
import com.example.ht_vlxd.Model.inventory.PhieuKho;
import com.example.ht_vlxd.Model.inventory.PhieuKhoChiTiet;
import com.example.ht_vlxd.Model.inventory.TonKho;
import com.example.ht_vlxd.Repository.management.BaoCaoRepository;
import com.example.ht_vlxd.Repository.finance.CongNoRepository;
import com.example.ht_vlxd.Repository.sales.DoiTraHangRepository;
import com.example.ht_vlxd.Repository.sales.DonHangChiTietRepository;
import com.example.ht_vlxd.Repository.sales.DonHangRepository;
import com.example.ht_vlxd.Repository.sales.HopDongRepository;
import com.example.ht_vlxd.Repository.inventory.KhoRepository;
import com.example.ht_vlxd.Repository.auth.NguoiDungRepository;
import com.example.ht_vlxd.Repository.inventory.PhieuKhoChiTietRepository;
import com.example.ht_vlxd.Repository.inventory.PhieuKhoRepository;
import com.example.ht_vlxd.Repository.inventory.TonKhoRepository;

import com.example.ht_vlxd.Service.auth.NguoiDungService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/management")
public class ManagementRestController {
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.sales.BusinessWorkflowService workflow;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.auth.CurrentUser currentUser;


    private final DonHangRepository donHangRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;
    private final CongNoRepository congNoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final HopDongRepository hopDongRepository;
    private final DoiTraHangRepository doiTraHangRepository;
    private final BaoCaoRepository baoCaoRepository;
    private final PhieuKhoRepository phieuKhoRepository;
    private final PhieuKhoChiTietRepository phieuKhoChiTietRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final KhoRepository khoRepository;

    private final NguoiDungService nguoiDungService;

    public ManagementRestController(DonHangRepository donHangRepository,
                                    DonHangChiTietRepository donHangChiTietRepository,
                                    CongNoRepository congNoRepository,
                                    TonKhoRepository tonKhoRepository,
                                    HopDongRepository hopDongRepository,
                                    DoiTraHangRepository doiTraHangRepository,
                                    BaoCaoRepository baoCaoRepository,
                                    PhieuKhoRepository phieuKhoRepository,
                                    PhieuKhoChiTietRepository phieuKhoChiTietRepository,
                                    NguoiDungRepository nguoiDungRepository,
                                    KhoRepository khoRepository,
                                    NguoiDungService nguoiDungService) {
        this.donHangRepository = donHangRepository;
        this.donHangChiTietRepository = donHangChiTietRepository;
        this.congNoRepository = congNoRepository;
        this.tonKhoRepository = tonKhoRepository;
        this.hopDongRepository = hopDongRepository;
        this.doiTraHangRepository = doiTraHangRepository;
        this.baoCaoRepository = baoCaoRepository;
        this.phieuKhoRepository = phieuKhoRepository;
        this.phieuKhoChiTietRepository = phieuKhoChiTietRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.khoRepository = khoRepository;
        this.nguoiDungService = nguoiDungService;
    }

    // ====================================================
    // SUMMARY STATS FOR EXECUTIVE KPIS
    // ====================================================

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary() {
        // 1. Tổng doanh thu lũy kế (các đơn hàng HOAN_THANH, DA_XAC_NHAN, DANG_GIAO)
        List<DonHang> donHangs = donHangRepository.findAll();
        BigDecimal tongDoanhThu = BigDecimal.ZERO;
        for (DonHang dh : donHangs) {
            String status = dh.getTrangThai();
            if ("HOAN_THANH".equals(status)) {
                tongDoanhThu = tongDoanhThu.add(dh.getTongTien() != null ? dh.getTongTien() : BigDecimal.ZERO);
            }
        }

        // 2. Số dư nợ khách hàng (Phải thu): sum of (soTienNo - soTienDaTt) where status != DA_THANH_TOAN
        List<CongNo> congNos = congNoRepository.findAll();
        BigDecimal tongDuNo = BigDecimal.ZERO;
        for (CongNo cn : congNos) {
            if (!"PHAI_THU".equals(cn.getLoaiCongNo())) continue;
            if (!"DA_THANH_TOAN".equals(cn.getTrangThai())) {
                BigDecimal no = cn.getSoTienNo() != null ? cn.getSoTienNo() : BigDecimal.ZERO;
                BigDecimal daTt = cn.getSoTienDaTt() != null ? cn.getSoTienDaTt() : BigDecimal.ZERO;
                tongDuNo = tongDuNo.add(no.subtract(daTt).max(BigDecimal.ZERO));
            }
        }

        // 3. Giá trị tồn kho quy đổi (sum of soLuong * giaBanLe)
        List<TonKho> tonKhos = tonKhoRepository.findAll();
        BigDecimal giaTriTonKho = BigDecimal.ZERO;
        for (TonKho tk : tonKhos) {
            if (tk.getHangHoa() != null && tk.getSoLuong() != null) {
                BigDecimal rate = tk.getHangHoa().getGiaBanLe() != null ? tk.getHangHoa().getGiaBanLe() : BigDecimal.ZERO;
                giaTriTonKho = giaTriTonKho.add(tk.getSoLuong().multiply(rate));
            }
        }

        // 4. Tỉ lệ hoàn thành kế hoạch (mục tiêu tháng: 25,000,000 VND)
        BigDecimal mucTieu = new BigDecimal("25000000");
        BigDecimal phanTramDat = BigDecimal.ZERO;
        if (mucTieu.compareTo(BigDecimal.ZERO) > 0) {
            phanTramDat = tongDoanhThu.multiply(new BigDecimal("100")).divide(mucTieu, 2, BigDecimal.ROUND_HALF_UP);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tongDoanhThu", tongDoanhThu);
        result.put("tongDuNo", tongDuNo);
        result.put("giaTriTonKho", giaTriTonKho);
        result.put("phanTramDat", phanTramDat);
        result.put("mucTieu", mucTieu);

        return ResponseEntity.ok(result);
    }

    // ====================================================
    // NEW ORDERS AND DELIVERY ROUTINGS
    // ====================================================

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders() {
        List<DonHang> donHangs = donHangRepository.findAll();
        // Sort newest first
        donHangs.sort((o1, o2) -> {
            if (o1.getNgayDat() == null) return 1;
            if (o2.getNgayDat() == null) return -1;
            return o2.getNgayDat().compareTo(o1.getNgayDat());
        });

        List<Map<String, Object>> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DonHang dh : donHangs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dh.getId());
            map.put("maDonHang", dh.getMaDonHang());
            map.put("khachHangTen", dh.getKhachHang() != null && dh.getKhachHang().getNguoiDung() != null ?
                    dh.getKhachHang().getNguoiDung().getHoTen() : "Khách vãng lai");
            map.put("ngayDat", dh.getNgayDat() != null ? dh.getNgayDat().format(formatter) : "");
            map.put("tongTien", dh.getTongTien());
            map.put("trangThai", dh.getTrangThai());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    // ====================================================
    // PENDING APPROVALS LIST
    // ====================================================

    @GetMapping("/pending-approvals")
    public ResponseEntity<?> getPendingApprovals() {
        List<Map<String, Object>> approvals = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 1. Contracts in NHAP/CHO_DUYET status
        List<HopDong> contracts = hopDongRepository.findAll();
        for (HopDong hd : contracts) {
            if ("NHAP".equals(hd.getTrangThai()) || "CHO_DUYET".equals(hd.getTrangThai())) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", hd.getId());
                item.put("type", "CONTRACT");
                item.put("title", "Duyệt chiết khấu hợp đồng " + hd.getMaHopDong());
                String khTen = (hd.getKhachHang() != null && hd.getKhachHang().getNguoiDung() != null) ?
                        hd.getKhachHang().getNguoiDung().getHoTen() : "Không rõ";
                item.put("description", "KH: " + khTen + " - Giá trị: " + hd.getGiaTri() + "đ; cọc thỏa thuận: " + hd.getTienDatCoc() + "; chi tiết: " + hd.getChiTiet().stream().map(l -> l.getHangHoa().getTenHang() + " × " + l.getSoLuong() + " @ " + l.getDonGia()).collect(java.util.stream.Collectors.joining(", ")) + "; điều khoản: " + hd.getDieuKhoanTt());
                item.put("targetKey", hd.getMaHopDong());
                approvals.add(item);
            }
        }

        // 2. Returns/Exchanges in CHO_DUYET status
        List<DoiTraHang> returns = doiTraHangRepository.findAll();
        for (DoiTraHang dth : returns) {
            if ("CHO_DUYET".equals(dth.getTrangThai())) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", dth.getId());
                item.put("type", "RETURN");
                String loai = "DOI".equals(dth.getLoai()) ? "Đổi hàng" : "Trả hàng";
                item.put("title", "Duyệt yêu cầu " + loai + " " + dth.getMaDoiTra());
                String khTen = (dth.getKhachHang() != null && dth.getKhachHang().getNguoiDung() != null) ?
                        dth.getKhachHang().getNguoiDung().getHoTen() : "Không rõ";
                String hangTen = dth.getHangHoa() != null ? dth.getHangHoa().getTenHang() : "hàng hóa";
                item.put("description", "KH: " + khTen + " yêu cầu trả " + dth.getSoLuong() + " " + (dth.getHangHoa() != null ? dth.getHangHoa().getDonViTinh() : "") + " " + hangTen + ". Lý do: " + dth.getLyDo());
                item.put("targetKey", dth.getMaDoiTra());
                approvals.add(item);
            }
        }

        // 3. Reports in CHO_DUYET status
        List<BaoCao> reports = baoCaoRepository.findAll();
        for (BaoCao bc : reports) {
            if ("CHO_DUYET".equals(bc.getTrangThai())) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", bc.getId());
                item.put("type", "REPORT");
                item.put("title", "Phê duyệt báo cáo: " + bc.getTieuDe());
                String nguoiLap = bc.getNguoiLap() != null ? bc.getNguoiLap().getHoTen() : "Nhân viên";
                item.put("description", "Người lập: " + nguoiLap + " - Ngày lập: " + bc.getNgayLap().format(dateFormatter) + ". Ghi chú: " + bc.getGhiChu());
                item.put("targetKey", bc.getId().toString());
                approvals.add(item);
            }
        }

        // 4. Warehouse/Inventory Slips in NHAP status (Draft waiting for approval)
        List<PhieuKho> slips = phieuKhoRepository.findAll();
        for (PhieuKho pk : slips) {
            if ("NHAP".equals(pk.getTrangThai()) || "CHO_DUYET".equals(pk.getTrangThai())) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", pk.getId());
                item.put("type", "INVENTORY_SLIP");
                String loai = "NHẬP KHO".equals(pk.getLoaiPhieu()) || "NHAP".equals(pk.getLoaiPhieu()) ? "nhập kho" : "xuất kho";
                item.put("title", "Duyệt phiếu " + loai + " " + pk.getMaPhieu());
                String nguoiLap = pk.getNguoiTao() != null ? pk.getNguoiTao().getHoTen() : "Thủ kho";
                String doiTac = "";
                if ("NHAP".equals(pk.getLoaiPhieu()) && pk.getNhaCungCap() != null) {
                    doiTac = " từ NCC: " + pk.getNhaCungCap().getTenNcc();
                } else if (pk.getDonHang() != null) {
                    doiTac = " cho đơn hàng: " + pk.getDonHang().getMaDonHang();
                }
                item.put("description", "Người lập: " + nguoiLap + doiTac + "; chi tiết: " + pk.getChiTiet().stream().map(l -> l.getHangHoa().getTenHang() + " × " + l.getSoLuong() + " @ " + l.getDonGia()).collect(java.util.stream.Collectors.joining(", ")) + "; biên bản nhập: " + pk.getBienBanNhapHang() + "; ghi chú: " + pk.getGhiChu());
                item.put("targetKey", pk.getMaPhieu());
                approvals.add(item);
            }
        }

        return ResponseEntity.ok(approvals);
    }

    // ====================================================
    // DYNAMIC INVENTORY REPORT
    // ====================================================

    @GetMapping("/inventory-report")
    public ResponseEntity<?> getInventoryReport() {
        List<TonKho> tonKhos = tonKhoRepository.findAll();
        List<PhieuKhoChiTiet> slipDetails = phieuKhoChiTietRepository.findAll();

        Map<Long, BigDecimal> currentStocks = new HashMap<>();
        for (TonKho tk : tonKhos) {
            if (tk.getHangHoa() != null) {
                Long hhId = tk.getHangHoa().getId();
                BigDecimal current = currentStocks.getOrDefault(hhId, BigDecimal.ZERO);
                currentStocks.put(hhId, current.add(tk.getSoLuong() != null ? tk.getSoLuong() : BigDecimal.ZERO));
            }
        }

        Map<Long, BigDecimal> importedMap = new HashMap<>();
        Map<Long, BigDecimal> exportedMap = new HashMap<>();

        for (PhieuKhoChiTiet ct : slipDetails) {
            if (ct.getHangHoa() != null && ct.getPhieuKho() != null && "DA_DUYET".equals(ct.getPhieuKho().getTrangThai())) {
                Long hhId = ct.getHangHoa().getId();
                String type = ct.getPhieuKho().getLoaiPhieu();
                BigDecimal qty = ct.getSoLuong() != null ? ct.getSoLuong() : BigDecimal.ZERO;
                if ("NHAP".equals(type)) {
                    BigDecimal current = importedMap.getOrDefault(hhId, BigDecimal.ZERO);
                    importedMap.put(hhId, current.add(qty));
                } else if ("XUAT".equals(type)) {
                    BigDecimal current = exportedMap.getOrDefault(hhId, BigDecimal.ZERO);
                    exportedMap.put(hhId, current.add(qty));
                }
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        Set<Long> addedIds = new HashSet<>();
        List<TonKho> allTks = tonKhoRepository.findAll();
        for (TonKho tk : allTks) {
            HangHoa hh = tk.getHangHoa();
            if (hh == null || addedIds.contains(hh.getId())) continue;
            addedIds.add(hh.getId());

            BigDecimal cur = currentStocks.getOrDefault(hh.getId(), BigDecimal.ZERO);
            BigDecimal imp = importedMap.getOrDefault(hh.getId(), BigDecimal.ZERO);
            BigDecimal exp = exportedMap.getOrDefault(hh.getId(), BigDecimal.ZERO);
            BigDecimal start = cur.subtract(imp).add(exp);

            Map<String, Object> map = new HashMap<>();
            map.put("maHang", hh.getMaHang());
            map.put("tenHang", hh.getTenHang());
            map.put("donViTinh", hh.getDonViTinh());
            map.put("tonDauKy", start);
            map.put("nhapTrongKy", imp);
            map.put("xuatTrongKy", exp);
            map.put("tonCuoiKy", cur);
            rows.add(map);
        }

        return ResponseEntity.ok(rows);
    }

    // ====================================================
    // ACTION ENDPOINTS
    // ====================================================

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/contracts/approve")
    public ResponseEntity<?> approveContract(@RequestBody Map<String, String> body) {
        workflow.approveContract(body.get("maHopDong"));
        return ResponseEntity.ok(java.util.Map.of("message", "Đã duyệt hợp đồng; kinh doanh tiếp tục ghi nhận ký."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/returns/approve")
    public ResponseEntity<?> approveReturn(@RequestBody Map<String, String> body) {
        workflow.approveReturn(body.get("maDoiTra"), true);
        return ResponseEntity.ok(java.util.Map.of("message", "Đã duyệt yêu cầu thu hồi."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/returns/reject")
    public ResponseEntity<?> rejectReturn(@RequestBody Map<String, String> body) {
        workflow.approveReturn(body.get("maDoiTra"), false);
        return ResponseEntity.ok(java.util.Map.of("message", "Đã từ chối yêu cầu."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/reports/approve")
    public ResponseEntity<?> approveReport(@RequestBody Map<String, String> body) {
        Long reportId = Long.valueOf(body.get("reportId"));
        BaoCao bc = baoCaoRepository.findById(reportId).orElse(null);
        if (bc == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy báo cáo.");
        }
        workflow.require("CHO_DUYET".equals(bc.getTrangThai()), "Báo cáo không ở trạng thái chờ duyệt.");
        bc.setNguoiDuyet(currentUser.get());
        bc.setTrangThai("DA_DUYET");
        bc.setNgayDuyet(LocalDateTime.now());
        baoCaoRepository.save(bc);
        return ResponseEntity.ok(Map.of("message", "Đã phê duyệt báo cáo thành công!"));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/inventory-slips/approve")
    public ResponseEntity<?> approveInventorySlip(@RequestBody Map<String, String> body) {
        workflow.approveSlip(body.get("maPhieu"));
        return ResponseEntity.ok(java.util.Map.of("message", "Đã duyệt phiếu và cập nhật kho/công nợ."));
    }
}
