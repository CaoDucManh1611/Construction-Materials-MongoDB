package com.example.ht_vlxd.Model.inventory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Document(collection = "lenh_xuat")
public class LenhXuat {
    @Id private Long id;
    @Version private Long version;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @Indexed(unique = true)
    private String maLenhXuat;
    public String getMaLenhXuat() { return maLenhXuat; }
    public void setMaLenhXuat(String value) { this.maLenhXuat = value; }
    @Indexed(unique = true)
    private Long donHangId;
    public Long getDonHangId() { return donHangId; }
    public void setDonHangId(Long value) { this.donHangId = value; }
    private Long hopDongId;
    public Long getHopDongId() { return hopDongId; }
    public void setHopDongId(Long value) { this.hopDongId = value; }
    private Long hoaDonId;
    public Long getHoaDonId() { return hoaDonId; }
    public void setHoaDonId(Long value) { this.hoaDonId = value; }
    private String trangThai = "CHO_XUAT";
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String value) { this.trangThai = value; }
    private List<com.example.ht_vlxd.Model.sales.DonHangChiTiet> chiTiet = new ArrayList<>();
    public List<com.example.ht_vlxd.Model.sales.DonHangChiTiet> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<com.example.ht_vlxd.Model.sales.DonHangChiTiet> value) { this.chiTiet = value; }
    private LocalDateTime ngayLap = LocalDateTime.now();
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime value) { this.ngayLap = value; }
}
