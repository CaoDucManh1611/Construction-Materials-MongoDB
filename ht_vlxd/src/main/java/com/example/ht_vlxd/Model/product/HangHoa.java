package com.example.ht_vlxd.Model.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "hang_hoa")
public class HangHoa {
    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maHang;


    private String tenHang;

    @DocumentReference
    private DanhMuc danhMuc;


    private String donViTinh;


    private String quyCach;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal giaBanLe = BigDecimal.ZERO;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal giaBanSi;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal trongLuongKg;


    private String anhUrl;

    private TrangThaiHangHoa trangThai = TrangThaiHangHoa.KINH_DOANH;


    private String ghiChu;


    private LocalDateTime ngayTao = LocalDateTime.now();

    public HangHoa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaHang() { return maHang; }
    public void setMaHang(String maHang) { this.maHang = maHang; }
    public String getTenHang() { return tenHang; }
    public void setTenHang(String tenHang) { this.tenHang = tenHang; }
    public DanhMuc getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public String getQuyCach() { return quyCach; }
    public void setQuyCach(String quyCach) { this.quyCach = quyCach; }
    public BigDecimal getGiaBanLe() { return giaBanLe; }
    public void setGiaBanLe(BigDecimal giaBanLe) { this.giaBanLe = giaBanLe; }
    public BigDecimal getGiaBanSi() { return giaBanSi; }
    public void setGiaBanSi(BigDecimal giaBanSi) { this.giaBanSi = giaBanSi; }
    public TrangThaiHangHoa getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiHangHoa trangThai) { this.trangThai = trangThai; }
    public String getAnhUrl() { return anhUrl; }
    public void setAnhUrl(String anhUrl) { this.anhUrl = anhUrl; }
}
