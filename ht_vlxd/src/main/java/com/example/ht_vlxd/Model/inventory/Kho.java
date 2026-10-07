package com.example.ht_vlxd.Model.inventory;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;

@Document(collection = "kho")
public class Kho {
    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maKho;


    private String tenKho;


    private String diaChi;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal dienTich;


    private String trangThai = "HOAT_DONG";

    public Kho() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaKho() { return maKho; }
    public void setMaKho(String maKho) { this.maKho = maKho; }
    public String getTenKho() { return tenKho; }
    public void setTenKho(String tenKho) { this.tenKho = tenKho; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public BigDecimal getDienTich() { return dienTich; }
    public void setDienTich(BigDecimal dienTich) { this.dienTich = dienTich; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
