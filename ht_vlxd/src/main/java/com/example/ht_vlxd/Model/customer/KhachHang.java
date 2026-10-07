package com.example.ht_vlxd.Model.customer;
import com.example.ht_vlxd.Model.auth.NguoiDung;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;

@Document(collection = "khach_hang")
public class KhachHang {
    @Id
    private Long id;

    @Version
    private Long version;

    @DocumentReference
    private NguoiDung nguoiDung;

    @Indexed(unique = true, sparse = true)
    private String maKhachHang;


    private String tenCongTy;


    private String maSoThue;


    private String nguoiDaiDien;


    private String loaiKhach = "CA_NHAN";


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal hanMucNo = BigDecimal.ZERO;


    private String ghiChu;

    public KhachHang() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public NguoiDung getNguoiDung() { return nguoiDung; }
    public void setNguoiDung(NguoiDung nguoiDung) { this.nguoiDung = nguoiDung; }
    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }
    public String getTenCongTy() { return tenCongTy; }
    public void setTenCongTy(String tenCongTy) { this.tenCongTy = tenCongTy; }
    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }
    public String getNguoiDaiDien() { return nguoiDaiDien; }
    public void setNguoiDaiDien(String nguoiDaiDien) { this.nguoiDaiDien = nguoiDaiDien; }
    public String getLoaiKhach() { return loaiKhach; }
    public void setLoaiKhach(String loaiKhach) { this.loaiKhach = loaiKhach; }
    public BigDecimal getHanMucNo() { return hanMucNo; }
    public void setHanMucNo(BigDecimal hanMucNo) { this.hanMucNo = hanMucNo; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
