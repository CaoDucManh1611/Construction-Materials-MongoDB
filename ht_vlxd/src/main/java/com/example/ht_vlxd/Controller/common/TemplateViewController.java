package com.example.ht_vlxd.Controller.common;

import com.example.ht_vlxd.Model.product.DanhMuc;
import com.example.ht_vlxd.Model.product.HangHoa;
import com.example.ht_vlxd.Model.product.TrangThaiHangHoa;
import com.example.ht_vlxd.Service.product.DanhMucService;
import com.example.ht_vlxd.Service.product.HangHoaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class TemplateViewController {
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.product.CatalogApprovalService catalog;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.ht_vlxd.Service.auth.CurrentUser sessionUser;

    @ModelAttribute("currentAccount")
    public com.example.ht_vlxd.Model.auth.NguoiDung currentAccount(org.springframework.security.core.Authentication auth) {
        return auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName()) ? null : sessionUser.get();
    }

    @ModelAttribute("workspaceUrl")
    public String workspaceUrl(org.springframework.security.core.Authentication auth) {
        var account = currentAccount(auth);
        if (account == null) return "/login";
        return switch (account.getRole().getName()) {
            case "NV_KINH_DOANH" -> "/kinh_doanh/quan_ly_don_hang";
            case "NV_KHO" -> "/quan_ly_kho/quan_ly_kho";
            case "NV_KE_TOAN" -> "/ke_toan/quan_ly_tai_chinh_va_cong_no";
            case "BAN_QUAN_LY" -> "/ban_quan_ly/dashboard_tong_quan";
            case "QUAN_TRI_VIEN" -> "/quan_tri_vien/quan_ly_tai_khoan";
            default -> "/khach_hang/don_hang_cua_toi";
        };
    }

    private final HangHoaService hangHoaService;
    private final DanhMucService danhMucService;

    public TemplateViewController(HangHoaService hangHoaService, DanhMucService danhMucService) {
        this.hangHoaService = hangHoaService;
        this.danhMucService = danhMucService;
    }

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("hangHoas", hangHoaService.getAllProducts().stream().limit(4).toList());
        model.addAttribute("danhMucs", danhMucService.getAll());
        return "index";
    }

    @GetMapping("/san_pham")
    public String publicCatalog(Model model) {
        List<HangHoa> hangHoas = hangHoaService.getAllProducts();
        List<DanhMuc> danhMucs = danhMucService.getAll();
        model.addAttribute("hangHoas", hangHoas);
        model.addAttribute("danhMucs", danhMucs);
        return "san_pham";
    }

    @GetMapping("/san_pham/chi_tiet")
    public String productDetail(@RequestParam Long id, Model model) {
        HangHoa hangHoa = hangHoaService.findById(id);
        model.addAttribute("prod", hangHoa);
        return "san_pham_chi_tiet";
    }

    @GetMapping({"/login", "/register", "/auth"})
    public String loginRegister() {
        return "xac_thuc/dang_nhap_dang_ky";
    }

    @GetMapping("/dung_chung/thong_tin_ca_nhan")
    public String profile() {
        return "dung_chung/thong_tin_ca_nhan";
    }

    @GetMapping("/dung_chung/calculator")
    public String calculator(Model model) {
        model.addAttribute("danhMucs", danhMucService.getAll());
        return "dung_chung/calculator";
    }

    @GetMapping("/khach_hang/don_hang_cua_toi")
    public String customerOrders() {
        return "khach_hang/don_hang_cua_toi";
    }

    @GetMapping("/khach_hang/gio_hang")
    public String customerCart() {
        return "khach_hang/gio_hang";
    }

    @GetMapping("/kinh_doanh/quan_ly_don_hang")
    public String salesOrders() {
        return "kinh_doanh/quan_ly_don_hang";
    }

    @GetMapping("/kinh_doanh/quan_ly_hop_dong")
    public String salesContracts() {
        return "kinh_doanh/quan_ly_hop_dong";
    }

    @GetMapping("/kinh_doanh/danh_muc_hang_hoa")
    public String salesCatalog(Model model) {
        List<HangHoa> hangHoas = hangHoaService.getAllProducts();
        List<DanhMuc> danhMucs = danhMucService.getAll();
        model.addAttribute("hangHoas", hangHoas);
        model.addAttribute("danhMucs", danhMucs);
        return "kinh_doanh/danh_muc_hang_hoa";
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/kinh_doanh/danh_muc_hang_hoa/add")
    public String addProduct(
            @RequestParam(required = false) Long id,
            @RequestParam String maHang,
            @RequestParam String tenHang,
            @RequestParam Long danhMucId,
            @RequestParam String donViTinh,
            @RequestParam String quyCach,
            @RequestParam BigDecimal giaBanLe,
            @RequestParam(required = false) BigDecimal giaBanSi,
            @RequestParam(required = false) String anhUrl,
            @RequestParam(value = "anhFile", required = false) MultipartFile anhFile) {

        String finalAnhUrl = anhUrl;
        if (anhFile != null && !anhFile.isEmpty()) {
            try {
                String contentType = anhFile.getContentType();
                if (anhFile.getSize() > 5_000_000 || !java.util.Set.of("image/jpeg", "image/png", "image/webp").contains(contentType))
                    throw new IllegalArgumentException("Ảnh phải là JPEG/PNG/WebP và không vượt 5 MB.");
                String fileName = java.util.UUID.randomUUID() + ("image/jpeg".equals(contentType) ? ".jpg" : "image/png".equals(contentType) ? ".png" : ".webp");
                String userDir = System.getProperty("user.dir");

                // Paths: src/main/resources/static/images/uploads and target/classes/static/images/uploads
                java.nio.file.Path srcDir = java.nio.file.Paths.get(userDir, "src", "main", "resources", "static", "images", "uploads");
                java.nio.file.Path targetDir = java.nio.file.Paths.get(userDir, "target", "classes", "static", "images", "uploads");

                java.nio.file.Files.createDirectories(srcDir);
                java.nio.file.Files.createDirectories(targetDir);

                java.nio.file.Path srcFile = srcDir.resolve(fileName);
                java.nio.file.Path targetFile = targetDir.resolve(fileName);

                // Copy stream to both locations
                try (java.io.InputStream in1 = anhFile.getInputStream()) {
                    java.nio.file.Files.copy(in1, srcFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                try (java.io.InputStream in2 = anhFile.getInputStream()) {
                    java.nio.file.Files.copy(in2, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                finalAnhUrl = "/images/uploads/" + fileName;
            } catch (Exception e) {
                throw new IllegalArgumentException("Không lưu được ảnh hàng hóa.", e);
            }
        }

        HangHoa hangHoa = new HangHoa();
        hangHoa.setId(id);
        hangHoa.setMaHang(maHang);
        hangHoa.setTenHang(tenHang);
        if (danhMucId != null) {
            DanhMuc dm = new DanhMuc();
            dm.setId(danhMucId);
            hangHoa.setDanhMuc(dm);
        }
        hangHoa.setDonViTinh(donViTinh);
        hangHoa.setQuyCach(quyCach);
        hangHoa.setGiaBanLe(giaBanLe);
        if (giaBanSi != null) {
            hangHoa.setGiaBanSi(giaBanSi);
        } else {
            hangHoa.setGiaBanSi(giaBanLe);
        }
        if (finalAnhUrl != null && !finalAnhUrl.trim().isEmpty()) {
            hangHoa.setAnhUrl(finalAnhUrl);
        } else {
            hangHoa.setAnhUrl("📦");
        }
        hangHoa.setTrangThai(TrangThaiHangHoa.KINH_DOANH);
        catalog.proposeProduct(hangHoa);

        return "redirect:/kinh_doanh/danh_muc_hang_hoa";
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/kinh_doanh/danh_muc_hang_hoa/delete")
    public String deleteProduct(@RequestParam Long id) {
        catalog.proposeDisable("HANG_HOA", id);
        return "redirect:/kinh_doanh/danh_muc_hang_hoa";
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/kinh_doanh/danh_muc/add")
    public String addCategory(
            @RequestParam(required = false) Long id,
            @RequestParam String maDanhMuc,
            @RequestParam String ten,
            @RequestParam(required = false) String moTa) {
        DanhMuc dm = new DanhMuc();
        dm.setId(id);
        dm.setMaDanhMuc(maDanhMuc);
        dm.setTen(ten);
        dm.setMoTa(moTa);
        catalog.proposeCategory(dm);
        return "redirect:/kinh_doanh/danh_muc_hang_hoa";
    }

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/kinh_doanh/danh_muc/delete")
    public String deleteCategory(@RequestParam Long id) {
        catalog.proposeDisable("DANH_MUC", id);
        return "redirect:/kinh_doanh/danh_muc_hang_hoa";
    }

    @GetMapping("/ke_toan/quan_ly_tai_chinh_va_cong_no")
    public String accountingDebt() {
        return "ke_toan/quan_ly_tai_chinh_va_cong_no";
    }

    @GetMapping("/ke_toan/bao_cao")
    public String accountingReports() {
        return "ke_toan/bao_cao";
    }

    @GetMapping("/ke_toan/quan_ly_nha_cung_cap")
    public String accountingSuppliers() {
        return "ke_toan/quan_ly_nha_cung_cap";
    }

    @GetMapping("/ban_quan_ly/dashboard_tong_quan")
    public String managementDashboard() {
        return "ban_quan_ly/dashboard_tong_quan";
    }

    @GetMapping("/ban_quan_ly/bao_cao")
    public String managementReports() {
        return "ban_quan_ly/bao_cao";
    }

    @GetMapping("/quan_ly_kho/quan_ly_kho")
    public String warehouseInventory() {
        return "quan_ly_kho/quan_ly_kho";
    }

    @GetMapping("/quan_ly_kho/giao_nhan_va_ban_giao")
    public String warehouseDelivery() {
        return "quan_ly_kho/giao_nhan_va_ban_giao";
    }

    @GetMapping("/quan_ly_kho/quan_ly_don_hang")
    public String warehouseOrders() {
        return "quan_ly_kho/quan_ly_don_hang";
    }

    @GetMapping("/quan_tri_vien/quan_ly_tai_khoan")
    public String adminAccounts() {
        return "quan_tri_vien/quan_ly_tai_khoan";
    }
}
