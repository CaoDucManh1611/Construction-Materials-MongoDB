package com.example.ht_vlxd.Model.sales;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Document(collection = "bang_bao_gia")
public class BangBaoGia {
    @Id private Long id;
    @Version private Long version;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @Indexed(unique = true)
    private String maBaoGia;
    public String getMaBaoGia() { return maBaoGia; }
    public void setMaBaoGia(String value) { this.maBaoGia = value; }
    private Long khachHangId;
    public Long getKhachHangId() { return khachHangId; }
    public void setKhachHangId(Long value) { this.khachHangId = value; }
    private Long nguoiLapId;
    public Long getNguoiLapId() { return nguoiLapId; }
    public void setNguoiLapId(Long value) { this.nguoiLapId = value; }
    private List<DonHangChiTiet> chiTiet = new ArrayList<>();
    public List<DonHangChiTiet> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<DonHangChiTiet> value) { this.chiTiet = value; }
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal tongTien = BigDecimal.ZERO;
    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal value) { this.tongTien = value; }
    private LocalDateTime ngayLap = LocalDateTime.now();
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime value) { this.ngayLap = value; }
    private LocalDateTime hieuLucDen;
    public LocalDateTime getHieuLucDen() { return hieuLucDen; }
    public void setHieuLucDen(LocalDateTime value) { this.hieuLucDen = value; }
    private String trangThai = "NHAP";
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String value) { this.trangThai = value; }
}
