package com.example.ht_vlxd.Model.finance;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
@Document(collection = "hoa_don")
public class HoaDon {
    @Id private Long id;
    @Version private Long version;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @Indexed(unique = true)
    private String maHoaDon;
    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String value) { this.maHoaDon = value; }
    private Long hopDongId;
    public Long getHopDongId() { return hopDongId; }
    public void setHopDongId(Long value) { this.hopDongId = value; }
    private Long donHangId;
    public Long getDonHangId() { return donHangId; }
    public void setDonHangId(Long value) { this.donHangId = value; }
    @Indexed(unique = true)
    private Long thanhToanId;
    public Long getThanhToanId() { return thanhToanId; }
    public void setThanhToanId(Long value) { this.thanhToanId = value; }
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soTien;
    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal value) { this.soTien = value; }
    private LocalDateTime ngayLap = LocalDateTime.now();
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime value) { this.ngayLap = value; }
    private String trangThai = "DA_LAP";
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String value) { this.trangThai = value; }
}
