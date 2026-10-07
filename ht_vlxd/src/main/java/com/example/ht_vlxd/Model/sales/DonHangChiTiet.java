package com.example.ht_vlxd.Model.sales;
import com.example.ht_vlxd.Model.product.HangHoa;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;


public class DonHangChiTiet {
    @Id
    private Long id;


    @org.springframework.data.annotation.Transient
    private DonHang donHang;

    @DocumentReference
    private HangHoa hangHoa;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soLuong;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal donGia;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal thanhTien;


    private String ghiChu;

    public DonHangChiTiet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public HangHoa getHangHoa() { return hangHoa; }
    public void setHangHoa(HangHoa hangHoa) { this.hangHoa = hangHoa; }
    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
