package com.example.ht_vlxd.Controller.customer;
import com.example.ht_vlxd.Model.finance.CongNo;
import com.example.ht_vlxd.Model.sales.DoiTraHang;
import com.example.ht_vlxd.Model.sales.DonHang;
import com.example.ht_vlxd.Model.sales.DonHangChiTiet;
import com.example.ht_vlxd.Model.product.HangHoa;
import com.example.ht_vlxd.Model.customer.KhachHang;
import com.example.ht_vlxd.Model.auth.NguoiDung;
import com.example.ht_vlxd.Model.auth.Role;
import com.example.ht_vlxd.Repository.finance.CongNoRepository;
import com.example.ht_vlxd.Repository.sales.DoiTraHangRepository;
import com.example.ht_vlxd.Repository.sales.DonHangChiTietRepository;
import com.example.ht_vlxd.Repository.sales.DonHangRepository;
import com.example.ht_vlxd.Repository.customer.KhachHangRepository;
import com.example.ht_vlxd.Repository.auth.RoleRepository;

import com.example.ht_vlxd.Service.sales.DonHangService;
import com.example.ht_vlxd.Service.product.HangHoaService;
import com.example.ht_vlxd.Service.customer.KhachHangService;
import com.example.ht_vlxd.Service.auth.NguoiDungService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/khach_hang")
public class CustomerRestController {
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.sales.BusinessWorkflowService workflow;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.auth.CurrentUser currentUser;


    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository = new org.springframework.security.web.context.DelegatingSecurityContextRepository(
            new org.springframework.security.web.context.RequestAttributeSecurityContextRepository(),
            new org.springframework.security.web.context.HttpSessionSecurityContextRepository()
    );

    private final NguoiDungService nguoiDungService;
    private final KhachHangRepository khachHangRepository;
    private final HangHoaService hangHoaService;
    private final DonHangService donHangService;
    private final DonHangRepository donHangRepository;
    private final DonHangChiTietRepository donHangChiTietRepository;
    private final DoiTraHangRepository doiTraHangRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CongNoRepository congNoRepository;

    public CustomerRestController(NguoiDungService nguoiDungService,
                                  KhachHangRepository khachHangRepository,
                                  HangHoaService hangHoaService,
                                  DonHangService donHangService,
                                  DonHangRepository donHangRepository,
                                  DonHangChiTietRepository donHangChiTietRepository,
                                  DoiTraHangRepository doiTraHangRepository,
                                  RoleRepository roleRepository,
                                  PasswordEncoder passwordEncoder,
                                  CongNoRepository congNoRepository) {
        this.nguoiDungService = nguoiDungService;
        this.khachHangRepository = khachHangRepository;
        this.hangHoaService = hangHoaService;
        this.donHangService = donHangService;
        this.donHangRepository = donHangRepository;
        this.donHangChiTietRepository = donHangChiTietRepository;
        this.doiTraHangRepository = doiTraHangRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.congNoRepository = congNoRepository;
    }

    @GetMapping("/products")
    public ResponseEntity<List<HangHoa>> getAllProducts() {
        return ResponseEntity.ok(hangHoaService.getAllProducts());
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam(required = false) String username) {
        username = currentUser.username();
        NguoiDung nd = nguoiDungService.findByUsername(username);
        if (nd == null) {
            // Also fall back to phone number search or name search
            nd = nguoiDungService.findBySoDienThoai(username);
        }
        if (nd == null) {
            List<NguoiDung> byName = nguoiDungService.findByHoTen(username);
            if (byName != null && !byName.isEmpty()) {
                nd = byName.get(0);
            }
        }
        if (nd == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản người dùng: " + username);
        }
        KhachHang kh = khachHangRepository.findByNguoiDungUsername(nd.getUsername());

        BigDecimal duNo = BigDecimal.ZERO;
        if (kh != null) {
            List<CongNo> congNos = congNoRepository.findByKhachHangId(kh.getId());
            for (CongNo cn : congNos) {
                if (!"DA_THANH_TOAN".equals(cn.getTrangThai())) {
                    BigDecimal outstanding = cn.getSoTienNo().subtract(cn.getSoTienDaTt() != null ? cn.getSoTienDaTt() : BigDecimal.ZERO);
                    duNo = duNo.add(outstanding);
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("username", nd.getUsername());
        data.put("hoTen", nd.getHoTen());
        data.put("email", nd.getEmail());
        data.put("soDienThoai", nd.getSoDienThoai());
        data.put("diaChi", nd.getDiaChi());
        data.put("tenCongTy", kh != null ? kh.getTenCongTy() : "");
        data.put("maSoThue", kh != null ? kh.getMaSoThue() : "");
        data.put("maKhachHang", kh != null ? kh.getMaKhachHang() : "");
        data.put("hanMucNo", kh != null ? kh.getHanMucNo() : BigDecimal.ZERO);
        data.put("duNo", duNo);
        data.put("trangThai", nd.getTrangThai());

        return ResponseEntity.ok(data);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String fullname = body.get("fullname");
        String phone = body.get("phone");
        String address = body.get("address");
        String password = body.get("password");

        if (nguoiDungService.findByUsername(username) != null) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại trong hệ thống.");
        }

        Role customerRole = roleRepository.findByName("KHACH_HANG");
        if (customerRole == null) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống: Không tìm thấy vai trò KHACH_HANG.");
        }

        workflow.require(username != null && !username.isBlank() && fullname != null && !fullname.isBlank(), "Tên đăng nhập và họ tên không được trống.");
        workflow.require(password != null && password.length() >= 8, "Mật khẩu cần ít nhất 8 ký tự.");
        NguoiDung nd = new NguoiDung();
        nd.setUsername(username);
        nd.setPasswordHash(passwordEncoder.encode(password));
        nd.setHoTen(fullname);
        nd.setSoDienThoai(phone);
        nd.setDiaChi(address);
        nd.setRole(customerRole);
        nd.setTrangThai("HOAT_DONG");
        NguoiDung savedNd = nguoiDungService.save(nd);

        KhachHang kh = new KhachHang();
        kh.setNguoiDung(savedNd);
        kh.setMaKhachHang(workflow.code("KH"));
        kh.setLoaiKhach("CA_NHAN");
        kh.setHanMucNo(BigDecimal.ZERO);
        kh.setNguoiDaiDien(fullname);
        kh.setTenCongTy("");
        kh.setMaSoThue("");
        khachHangRepository.save(kh);

        return ResponseEntity.ok("Đăng ký tài khoản khách hàng thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   jakarta.servlet.http.HttpServletRequest request,
                                   jakarta.servlet.http.HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");

        NguoiDung nd = nguoiDungService.findByUsername(username);
        if (nd == null) {
            nd = nguoiDungService.findBySoDienThoai(username);
        }
        if (nd == null) {
            List<NguoiDung> byName = nguoiDungService.findByHoTen(username);
            if (byName != null && !byName.isEmpty()) {
                nd = byName.get(0);
            }
        }

        if (nd == null) {
            return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        boolean matches = false;
        try {
            matches = passwordEncoder.matches(password, nd.getPasswordHash());
        } catch (Exception e) {
            // Ignore BCrypt check warnings
        }

        if (!matches) {
            nd.setSoLanDangNhapSai(nd.getSoLanDangNhapSai() + 1);
            if (nd.getSoLanDangNhapSai() >= 5) nd.setTrangThai("BI_KHOA");
            nguoiDungService.save(nd);
            return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu không chính xác.");
        }

        if ("BI_KHOA".equals(nd.getTrangThai())) {
            return ResponseEntity.badRequest().body("Tài khoản của bạn đã bị khóa bởi Quản trị viên.");
        }
        if ("CHO_DUYET".equals(nd.getTrangThai())) {
            return ResponseEntity.badRequest().body("Tài khoản của bạn đang chờ quản trị viên phê duyệt.");
        }

        nd.setSoLanDangNhapSai(0);
        nguoiDungService.save(nd);
        request.getSession(true);
        request.changeSessionId();

        // Establish Spring Security Context
        List<org.springframework.security.core.GrantedAuthority> authorities =
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_" + nd.getRole().getName());
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(nd.getUsername(), null, authorities);

        org.springframework.security.core.context.SecurityContext context =
                org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        Map<String, Object> respMap = new HashMap<>();
        respMap.put("username", nd.getUsername());
        respMap.put("hoTen", nd.getHoTen());
        respMap.put("role", nd.getRole().getName());
        respMap.put("trangThai", nd.getTrangThai());

        return ResponseEntity.ok(respMap);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        String username = currentUser.username();
        NguoiDung nd = nguoiDungService.findByUsername(username);
        if (nd == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản người dùng: " + username);
        }

        nd.setHoTen(body.get("hoTen"));
        nd.setEmail(body.get("email"));
        nd.setSoDienThoai(body.get("soDienThoai"));
        nd.setDiaChi(body.get("diaChi"));
        nguoiDungService.save(nd);

        KhachHang kh = khachHangRepository.findByNguoiDungUsername(username);
        if (kh == null) {
            kh = new KhachHang();
            kh.setNguoiDung(nd);
            kh.setMaKhachHang(workflow.code("KH"));
            kh.setLoaiKhach("CA_NHAN");
        }
        kh.setTenCongTy(body.get("tenCongTy"));
        kh.setMaSoThue(body.get("maSoThue"));
        kh.setNguoiDaiDien(body.get("hoTen"));
        khachHangRepository.save(kh);

        return ResponseEntity.ok("Cập nhật thông tin thành công!");
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String username = currentUser.username();
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        NguoiDung nd = nguoiDungService.findByUsername(username);
        if (nd == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản.");
        }

        boolean currentMatches = false;
        try {
            currentMatches = passwordEncoder.matches(currentPassword, nd.getPasswordHash());
        } catch (Exception e) {
            // Ignore BCrypt check warnings
        }

        if (!currentMatches) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không chính xác.");
        }

        workflow.require(newPassword != null && newPassword.length() >= 8, "Mật khẩu mới cần ít nhất 8 ký tự.");
        nd.setPasswordHash(passwordEncoder.encode(newPassword));
        nguoiDungService.save(nd);
        return ResponseEntity.ok("Đổi mật khẩu thành công!");
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@RequestParam(required = false) String username) {
        username = currentUser.username();
        KhachHang kh = khachHangRepository.findByNguoiDungUsername(username);
        if (kh == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<DonHang> donHangs = donHangRepository.findByKhachHangId(kh.getId());
        List<Map<String, Object>> response = new ArrayList<>();

        for (DonHang dh : donHangs) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", dh.getId());
            orderMap.put("maDonHang", dh.getMaDonHang());
            orderMap.put("diaChiGiao", dh.getDiaChiGiao());
            orderMap.put("tongTien", dh.getTongTien());
            orderMap.put("tienDatCoc", dh.getTienDatCoc());
            orderMap.put("trangThai", dh.getTrangThai());
            orderMap.put("ngayDat", dh.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
            orderMap.put("chiTiet", detailList);
            response.add(orderMap);
        }

        return ResponseEntity.ok(response);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/create")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        String username = currentUser.username();
        KhachHang kh = khachHangRepository.findByNguoiDungUsername(username);
        if (kh == null) {
            return ResponseEntity.badRequest().body("Chỉ tài khoản khách hàng mới được phép đặt hàng.");
        }

        Long productId = Long.valueOf(body.get("productId").toString());
        BigDecimal soLuong = new BigDecimal(body.get("soLuong").toString());
        workflow.positive(soLuong, "Số lượng");
        String diaChiGiao = (String) body.get("diaChiGiao");
        String ghiChu = (String) body.get("ghiChu");

        HangHoa hh = hangHoaService.findById(productId);
        if (hh == null || hh.getTrangThai() != com.example.ht_vlxd.Model.product.TrangThaiHangHoa.KINH_DOANH) {
            return ResponseEntity.badRequest().body("Không tìm thấy hàng hóa.");
        }

        BigDecimal donGia = hh.getGiaBanLe();
        BigDecimal tongTien = donGia.multiply(soLuong);

        DonHang dh = new DonHang();
        dh.setKhachHang(kh);
        dh.setMaDonHang("DH-" + System.currentTimeMillis());
        dh.setDiaChiGiao(diaChiGiao);
        dh.setTongTien(tongTien);
        dh.setTienDatCoc(BigDecimal.ZERO);
        dh.setTrangThai("CHO_XAC_NHAN");
        dh.setGhiChu(ghiChu);
        dh.setNgayDat(LocalDateTime.now());
        DonHang savedDh = donHangService.save(dh);

        DonHangChiTiet ct = new DonHangChiTiet();
        ct.setDonHang(savedDh);
        ct.setHangHoa(hh);
        ct.setSoLuong(soLuong);
        ct.setDonGia(donGia);
        ct.setThanhTien(tongTien);
        ct.setGhiChu(ghiChu);
        donHangChiTietRepository.save(ct);

        return ResponseEntity.ok("Đặt hàng thành công với mã đơn: " + savedDh.getMaDonHang());
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/cancel")
    public ResponseEntity<?> cancelOrder(@RequestBody Map<String, Object> body) {
        workflow.cancelOrder((String) body.get("maDonHang"), true);
        return ResponseEntity.ok(java.util.Map.of("message", "Đã hủy đơn hàng."));
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/return")
    public ResponseEntity<?> requestReturn(@RequestBody Map<String, Object> body) {
        String maDonHang = (String) body.get("maDonHang");
        String maHang = (String) body.get("maHang");
        BigDecimal soLuong = new BigDecimal(body.get("soLuong").toString());
        workflow.positive(soLuong, "Số lượng");
        String lyDo = (String) body.get("lyDo");
        String loai = (String) body.get("loai"); // DOI, TRA

        DonHang dh = donHangService.findByMaDonHang(maDonHang);
        if (dh == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng.");
        }

        HangHoa hh = hangHoaService.getAllProducts().stream()
                .filter(h -> h.getMaHang().equals(maHang))
                .findFirst()
                .orElse(null);

        if (hh == null || hh.getTrangThai() != com.example.ht_vlxd.Model.product.TrangThaiHangHoa.KINH_DOANH) {
            return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm trong hệ thống.");
        }

        workflow.validateReturn(dh, hh, soLuong, loai);
        workflow.require(lyDo != null && !lyDo.isBlank(), "Phải ghi lý do đổi trả.");
        DoiTraHang dth = new DoiTraHang();
        dth.setDonHang(dh);
        dth.setKhachHang(dh.getKhachHang());
        dth.setHangHoa(hh);
        dth.setSoLuong(soLuong);
        dth.setLyDo(lyDo);
        dth.setLoai(loai);
        dth.setTrangThai("CHO_DUYET");
        dth.setMaDoiTra("DT-" + System.currentTimeMillis());
        dth.setNgayYeuCau(LocalDateTime.now());
        doiTraHangRepository.save(dth);

        return ResponseEntity.ok("Yêu cầu đổi trả đã được gửi thành công!");
    }

    @GetMapping("/returns")
    public ResponseEntity<?> getReturns(@RequestParam(required = false) String username) {
        username = currentUser.username();
        NguoiDung nd = nguoiDungService.findByUsername(username);
        if (nd == null) {
            nd = nguoiDungService.findBySoDienThoai(username);
        }
        if (nd == null) {
            List<NguoiDung> byName = nguoiDungService.findByHoTen(username);
            if (byName != null && !byName.isEmpty()) {
                nd = byName.get(0);
            }
        }
        if (nd == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy người dùng.");
        }
        KhachHang kh = khachHangRepository.findByNguoiDungUsername(nd.getUsername());
        if (kh == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<DoiTraHang> returns = doiTraHangRepository.findByKhachHangId(kh.getId());
        List<Map<String, Object>> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DoiTraHang dth : returns) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dth.getId());
            map.put("maDoiTra", dth.getMaDoiTra());
            map.put("maDonHang", dth.getDonHang().getMaDonHang());
            map.put("tenHang", dth.getHangHoa().getTenHang());
            map.put("maHang", dth.getHangHoa().getMaHang());
            map.put("soLuong", dth.getSoLuong());
            map.put("lyDo", dth.getLyDo());
            map.put("loai", dth.getLoai());
            map.put("trangThai", dth.getTrangThai());
            map.put("ngayYeuCau", dth.getNgayYeuCau() != null ? dth.getNgayYeuCau().format(formatter) : "");
            map.put("ghiChuXuLy", dth.getGhiChuXuLy() != null ? dth.getGhiChuXuLy() : "");
            map.put("ngayXuLy", dth.getNgayXuLy() != null ? dth.getNgayXuLy().format(formatter) : "");
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/orders/create-cart")
    public ResponseEntity<?> createCartOrder(@RequestBody Map<String, Object> body) {
        String username = currentUser.username();
        NguoiDung nd = nguoiDungService.findByUsername(username);
        if (nd == null) {
            nd = nguoiDungService.findBySoDienThoai(username);
        }
        if (nd == null) {
            List<NguoiDung> byName = nguoiDungService.findByHoTen(username);
            if (byName != null && !byName.isEmpty()) {
                nd = byName.get(0);
            }
        }
        if (nd == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy tài khoản người dùng.");
        }
        KhachHang kh = khachHangRepository.findByNguoiDungUsername(nd.getUsername());
        if (kh == null) {
            return ResponseEntity.badRequest().body("Chỉ tài khoản khách hàng mới được phép đặt hàng.");
        }

        String diaChiGiao = (String) body.get("diaChiGiao");
        String ghiChu = (String) body.get("ghiChu");
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body("Giỏ hàng trống.");
        }

        BigDecimal tongTien = BigDecimal.ZERO;
        List<DonHangChiTiet> detailList = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("productId").toString());
            BigDecimal soLuong = new BigDecimal(item.get("soLuong").toString());
        workflow.positive(soLuong, "Số lượng");

            HangHoa hh = hangHoaService.findById(productId);
            if (hh == null || hh.getTrangThai() != com.example.ht_vlxd.Model.product.TrangThaiHangHoa.KINH_DOANH) {
                return ResponseEntity.badRequest().body("Không tìm thấy hàng hóa với ID: " + productId);
            }

            BigDecimal donGia = hh.getGiaBanLe();
            BigDecimal thanhTien = donGia.multiply(soLuong);
            tongTien = tongTien.add(thanhTien);

            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setHangHoa(hh);
            ct.setSoLuong(soLuong);
            ct.setDonGia(donGia);
            ct.setThanhTien(thanhTien);
            ct.setGhiChu(ghiChu);
            detailList.add(ct);
        }


        DonHang dh = new DonHang();
        dh.setKhachHang(kh);
        dh.setMaDonHang("DH-" + System.currentTimeMillis());
        dh.setDiaChiGiao(diaChiGiao);
        dh.setTongTien(tongTien);
        dh.setTienDatCoc(BigDecimal.ZERO);
        dh.setTrangThai("CHO_XAC_NHAN");
        dh.setGhiChu(ghiChu);
        dh.setNgayDat(LocalDateTime.now());

        DonHang savedDh = donHangService.save(dh);

        for (DonHangChiTiet ct : detailList) {
            ct.setDonHang(savedDh);
            donHangChiTietRepository.save(ct);
        }

        return ResponseEntity.ok("Đặt hàng thành công với mã đơn: " + savedDh.getMaDonHang());
    }
}
