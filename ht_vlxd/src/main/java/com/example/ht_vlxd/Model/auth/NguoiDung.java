package com.example.ht_vlxd.Model.auth;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "nguoi_dung")
public class NguoiDung {
    private int soLanDangNhapSai = 0;
    public int getSoLanDangNhapSai() { return soLanDangNhapSai; }
    public void setSoLanDangNhapSai(int value) { this.soLanDangNhapSai = value; }

    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String username;


    @com.fasterxml.jackson.annotation.JsonIgnore
    private String passwordHash;


    private String hoTen;

    @Indexed(unique = true, sparse = true)
    private String email;


    private String soDienThoai;


    private String diaChi;


    private String avatarUrl;

    @DocumentReference
    private Role role;


    private String trangThai = "HOAT_DONG";


    private LocalDateTime ngayTao = LocalDateTime.now();


    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    public NguoiDung() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
