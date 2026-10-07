package com.example.ht_vlxd.Service.product;
import com.example.ht_vlxd.Model.product.*;
import com.example.ht_vlxd.Repository.product.*;
import com.example.ht_vlxd.Service.auth.CurrentUser;
import com.example.ht_vlxd.Service.sales.BusinessWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import java.util.*;

@Service
@Transactional
public class CatalogApprovalService {
    private final MongoTemplate mongo;
    private final HangHoaRepository products;
    private final DanhMucRepository categories;
    private final CurrentUser user;
    public CatalogApprovalService(MongoTemplate mongo, HangHoaRepository products, DanhMucRepository categories, CurrentUser user) {
        this.mongo = mongo; this.products = products; this.categories = categories; this.user = user;
    }
    public void proposeProduct(HangHoa product) {
        BusinessWorkflowService.require(product.getMaHang() != null && !product.getMaHang().isBlank()
            && product.getTenHang() != null && !product.getTenHang().isBlank(), "Mã và tên hàng không được trống.");
        BusinessWorkflowService.positive(product.getGiaBanLe(), "Giá bán");
        BusinessWorkflowService.require(product.getDanhMuc() != null && categories.findById(product.getDanhMuc().getId()).isPresent(), "Danh mục không hợp lệ.");
        product.setDanhMuc(categories.findById(product.getDanhMuc().getId()).orElseThrow());
        BusinessWorkflowService.require(product.getDanhMuc().getHoatDong(), "Danh mục đã ngừng sử dụng.");
        BusinessWorkflowService.require(product.getGiaBanSi() == null || product.getGiaBanSi().signum() > 0, "Giá bán sỉ phải lớn hơn 0.");
        if (product.getId() != null) BusinessWorkflowService.require(products.existsById(product.getId()), "Không tìm thấy hàng hóa cần sửa.");
        var request = new DeXuatDanhMuc(); request.setLoai("HANG_HOA"); request.setThaoTac(product.getId() == null ? "THEM" : "SUA"); request.setDoiTuongId(product.getId()); request.setHangHoa(product); submit(request);
    }
    public void proposeCategory(DanhMuc category) {
        BusinessWorkflowService.require(category.getMaDanhMuc() != null && !category.getMaDanhMuc().isBlank() && category.getTen() != null && !category.getTen().isBlank(), "Mã và tên danh mục không được trống.");
        if (category.getId() != null) BusinessWorkflowService.require(categories.existsById(category.getId()), "Không tìm thấy danh mục cần sửa.");
        var request = new DeXuatDanhMuc(); request.setLoai("DANH_MUC"); request.setThaoTac(category.getId() == null ? "THEM" : "SUA"); request.setDoiTuongId(category.getId()); request.setDanhMuc(category); submit(request);
    }
    public void proposeDisable(String type, Long id) {
        BusinessWorkflowService.require("HANG_HOA".equals(type) ? products.existsById(id) : categories.existsById(id), "Đối tượng không tồn tại.");
        var request = new DeXuatDanhMuc(); request.setLoai(type); request.setThaoTac("NGUNG"); request.setDoiTuongId(id); submit(request);
    }
    private void submit(DeXuatDanhMuc request) { request.setNguoiLapId(user.get().getId()); mongo.save(request); }
    public List<DeXuatDanhMuc> pending() { return mongo.find(Query.query(Criteria.where("trangThai").is("CHO_DUYET")), DeXuatDanhMuc.class); }
    public void decide(Long id, boolean approve) {
        var request = mongo.findById(id, DeXuatDanhMuc.class);
        BusinessWorkflowService.require(request != null && "CHO_DUYET".equals(request.getTrangThai()), "Đề xuất đã xử lý hoặc không tồn tại.");
        if (approve) {
            if ("HANG_HOA".equals(request.getLoai())) {
                if ("THEM".equals(request.getThaoTac())) products.save(request.getHangHoa());
                else {
                    var product = products.findById(request.getDoiTuongId()).orElseThrow();
                    if ("SUA".equals(request.getThaoTac())) {
                        var proposed = request.getHangHoa(); product.setMaHang(proposed.getMaHang()); product.setTenHang(proposed.getTenHang()); product.setDanhMuc(proposed.getDanhMuc());
                        product.setDonViTinh(proposed.getDonViTinh()); product.setQuyCach(proposed.getQuyCach()); product.setGiaBanLe(proposed.getGiaBanLe()); product.setGiaBanSi(proposed.getGiaBanSi()); product.setAnhUrl(proposed.getAnhUrl());
                    } else product.setTrangThai(TrangThaiHangHoa.NGUNG_KINH_DOANH);
                    products.save(product);
                }
            } else {
                if ("THEM".equals(request.getThaoTac())) categories.save(request.getDanhMuc());
                else {
                    var category = categories.findById(request.getDoiTuongId()).orElseThrow();
                    if ("SUA".equals(request.getThaoTac())) { category.setMaDanhMuc(request.getDanhMuc().getMaDanhMuc()); category.setTen(request.getDanhMuc().getTen()); category.setMoTa(request.getDanhMuc().getMoTa()); }
                    else category.setHoatDong(false);
                    categories.save(category);
                    if ("NGUNG".equals(request.getThaoTac())) for (var product : products.findAll()) if (product.getDanhMuc() != null && product.getDanhMuc().getId().equals(category.getId())) { product.setTrangThai(TrangThaiHangHoa.NGUNG_KINH_DOANH); products.save(product); }
                }
            }
        }
        request.setTrangThai(approve ? "DA_DUYET" : "TU_CHOI"); request.setNguoiDuyetId(user.get().getId()); mongo.save(request);
    }
}
