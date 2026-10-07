package com.example.ht_vlxd.Model.sales;
import com.example.ht_vlxd.Model.auth.NguoiDung;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;

@Document(collection = "giao_nhan")
public class GiaoNhan {
    private String bienBanBanGiao;
    public String getBienBanBanGiao() { return bienBanBanGiao; }
    public void setBienBanBanGiao(String value) { this.bienBanBanGiao = value; }
    private java.util.List<String> bienBanSuVu = new java.util.ArrayList<>();
    public java.util.List<String> getBienBanSuVu() { return bienBanSuVu; }
    public void setBienBanSuVu(java.util.List<String> value) { this.bienBanSuVu = value; }

    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maGiaoNhan;

    @DocumentReference
    private DonHang donHang;

    @DocumentReference
    private NguoiDung nvGiao;


    private LocalDateTime ngayGiaoDuKien;


    private LocalDateTime ngayGiaoThuc;


    private String diaChiGiao;


    private String loTrinh;


    private String trangThai = "CHO_GIAO"; // CHO_GIAO, DANG_GIAO, DA_GIAO, THAT_BAI


    private String ghiChuGiao;


    private String nguoiNhan;


    private String soDienThoaiNhan;


    private Boolean daBanGiao = false;


    private LocalDateTime ngayBanGiao;

    public GiaoNhan() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaGiaoNhan() { return maGiaoNhan; }
    public void setMaGiaoNhan(String maGiaoNhan) { this.maGiaoNhan = maGiaoNhan; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public NguoiDung getNvGiao() { return nvGiao; }
    public void setNvGiao(NguoiDung nvGiao) { this.nvGiao = nvGiao; }
    public LocalDateTime getNgayGiaoDuKien() { return ngayGiaoDuKien; }
    public void setNgayGiaoDuKien(LocalDateTime ngayGiaoDuKien) { this.ngayGiaoDuKien = ngayGiaoDuKien; }
    public LocalDateTime getNgayGiaoThuc() { return ngayGiaoThuc; }
    public void setNgayGiaoThuc(LocalDateTime ngayGiaoThuc) { this.ngayGiaoThuc = ngayGiaoThuc; }
    public String getDiaChiGiao() { return diaChiGiao; }
    public void setDiaChiGiao(String diaChiGiao) { this.diaChiGiao = diaChiGiao; }
    public String getLoTrinh() { return loTrinh; }
    public void setLoTrinh(String loTrinh) { this.loTrinh = loTrinh; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChuGiao() { return ghiChuGiao; }
    public void setGhiChuGiao(String ghiChuGiao) { this.ghiChuGiao = ghiChuGiao; }
    public String getNguoiNhan() { return nguoiNhan; }
    public void setNguoiNhan(String nguoiNhan) { this.nguoiNhan = nguoiNhan; }
    public String getSoDienThoaiNhan() { return soDienThoaiNhan; }
    public void setSoDienThoaiNhan(String soDienThoaiNhan) { this.soDienThoaiNhan = soDienThoaiNhan; }
    public Boolean getDaBanGiao() { return daBanGiao; }
    public void setDaBanGiao(Boolean daBanGiao) { this.daBanGiao = daBanGiao; }
    public LocalDateTime getNgayBanGiao() { return ngayBanGiao; }
    public void setNgayBanGiao(LocalDateTime ngayBanGiao) { this.ngayBanGiao = ngayBanGiao; }
}
