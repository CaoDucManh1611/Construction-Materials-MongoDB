package com.example.ht_vlxd.Model.product;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "danh_muc")
public class DanhMuc {
    private boolean hoatDong = true;
    public boolean getHoatDong() { return hoatDong; }
    public void setHoatDong(boolean value) { hoatDong = value; }
    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maDanhMuc;


    private String ten;


    private String moTa;

    @DocumentReference
    private DanhMuc parent;

    public DanhMuc() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public DanhMuc getParent() { return parent; }
    public void setParent(DanhMuc parent) { this.parent = parent; }
}
