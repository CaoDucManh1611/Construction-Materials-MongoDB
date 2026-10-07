package com.example.ht_vlxd.Model.sales;
import com.example.ht_vlxd.Model.customer.KhachHang;
import com.example.ht_vlxd.Model.auth.NguoiDung;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "don_hang")
public class DonHang {
    private Long doiTraId;
    public Long getDoiTraId() { return doiTraId; }
    public void setDoiTraId(Long value) { doiTraId = value; }
    private Long baoGiaId;
    public Long getBaoGiaId() { return baoGiaId; }
    public void setBaoGiaId(Long value) { baoGiaId = value; }
    private java.util.List<DonHangChiTiet> chiTiet = new java.util.ArrayList<>();
    public java.util.List<DonHangChiTiet> getChiTiet() { return chiTiet; }
    public void setChiTiet(java.util.List<DonHangChiTiet> chiTiet) { this.chiTiet = chiTiet; }

    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maDonHang;

    @DocumentReference
    private KhachHang khachHang;


    private String tenKhachVangLai;


    private String sdtKhachVangLai;

    @DocumentReference
    private NguoiDung nvKinhDoanh;


    private LocalDateTime ngayDat = LocalDateTime.now();


    private LocalDateTime ngayGiaoDuKien;


    private String diaChiGiao;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal tongTien = BigDecimal.ZERO;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal tienDatCoc = BigDecimal.ZERO;


    private String trangThai = "CHO_XAC_NHAN";


    private String ghiChu;


    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    public DonHang() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public String getTenKhachVangLai() { return tenKhachVangLai; }
    public void setTenKhachVangLai(String tenKhachVangLai) { this.tenKhachVangLai = tenKhachVangLai; }
    public String getSdtKhachVangLai() { return sdtKhachVangLai; }
    public void setSdtKhachVangLai(String sdtKhachVangLai) { this.sdtKhachVangLai = sdtKhachVangLai; }
    public NguoiDung getNvKinhDoanh() { return nvKinhDoanh; }
    public void setNvKinhDoanh(NguoiDung nvKinhDoanh) { this.nvKinhDoanh = nvKinhDoanh; }
    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }
    public String getDiaChiGiao() { return diaChiGiao; }
    public void setDiaChiGiao(String diaChiGiao) { this.diaChiGiao = diaChiGiao; }
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
    public BigDecimal getTienDatCoc() { return tienDatCoc; }
    public void setTienDatCoc(BigDecimal tienDatCoc) { this.tienDatCoc = tienDatCoc; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
