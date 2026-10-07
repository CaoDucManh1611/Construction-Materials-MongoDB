package com.example.ht_vlxd.Model.finance;
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

@Document(collection = "thanh_toan")
public class ThanhToan {
    private String loaiPhieu = "THU";
    public String getLoaiPhieu() { return loaiPhieu; }
    public void setLoaiPhieu(String value) { this.loaiPhieu = value; }

    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maThanhToan;

    @DocumentReference
    private CongNo congNo;

    @DocumentReference
    private NguoiDung nguoiThu;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soTien;


    private String hinhThuc; // TIEN_MAT, CHUYEN_KHOAN, THE


    private LocalDateTime ngayThanhToan = LocalDateTime.now();


    @Indexed(unique = true, sparse = true)
    private String maGiaoDich;


    private String ghiChu;

    public ThanhToan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaThanhToan() { return maThanhToan; }
    public void setMaThanhToan(String maThanhToan) { this.maThanhToan = maThanhToan; }
    public CongNo getCongNo() { return congNo; }
    public void setCongNo(CongNo congNo) { this.congNo = congNo; }
    public NguoiDung getNguoiThu() { return nguoiThu; }
    public void setNguoiThu(NguoiDung nguoiThu) { this.nguoiThu = nguoiThu; }
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }
    public String getHinhThuc() { return hinhThuc; }
    public void setHinhThuc(String hinhThuc) { this.hinhThuc = hinhThuc; }
    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(LocalDateTime ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }
    public String getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(String maGiaoDich) { this.maGiaoDich = maGiaoDich; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
