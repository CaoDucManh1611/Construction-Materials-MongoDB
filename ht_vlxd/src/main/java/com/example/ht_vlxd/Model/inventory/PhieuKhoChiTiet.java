package com.example.ht_vlxd.Model.inventory;
import com.example.ht_vlxd.Model.product.HangHoa;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;


public class PhieuKhoChiTiet {
    @Id
    private Long id;


    @org.springframework.data.annotation.Transient
    private PhieuKho phieuKho;

    @DocumentReference
    private HangHoa hangHoa;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soLuong;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal donGia;


    private String ghiChu;

    public PhieuKhoChiTiet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public PhieuKho getPhieuKho() { return phieuKho; }
    public void setPhieuKho(PhieuKho phieuKho) { this.phieuKho = phieuKho; }
    public HangHoa getHangHoa() { return hangHoa; }
    public void setHangHoa(HangHoa hangHoa) { this.hangHoa = hangHoa; }
    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
