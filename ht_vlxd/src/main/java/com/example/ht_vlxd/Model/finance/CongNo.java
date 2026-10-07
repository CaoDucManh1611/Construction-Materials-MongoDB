package com.example.ht_vlxd.Model.finance;
import com.example.ht_vlxd.Model.sales.DonHang;
import com.example.ht_vlxd.Model.sales.HopDong;
import com.example.ht_vlxd.Model.customer.KhachHang;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "cong_no")
public class CongNo {
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soTienDaHoan = BigDecimal.ZERO;
    public BigDecimal getSoTienDaHoan() { return soTienDaHoan; }
    public void setSoTienDaHoan(BigDecimal value) { soTienDaHoan = value; }
    private Long nhaCungCapId;
    public Long getNhaCungCapId() { return nhaCungCapId; }
    public void setNhaCungCapId(Long value) { this.nhaCungCapId = value; }
    @Indexed(unique = true, sparse = true)
    private Long phieuNhapId;
    public Long getPhieuNhapId() { return phieuNhapId; }
    public void setPhieuNhapId(Long value) { this.phieuNhapId = value; }
    private String loaiCongNo = "PHAI_THU";
    public String getLoaiCongNo() { return loaiCongNo; }
    public void setLoaiCongNo(String value) { this.loaiCongNo = value; }
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soTienHoanTra = BigDecimal.ZERO;
    public BigDecimal getSoTienHoanTra() { return soTienHoanTra; }
    public void setSoTienHoanTra(BigDecimal value) { this.soTienHoanTra = value; }

    @Id
    private Long id;

    @Version
    private Long version;

    @DocumentReference
    private KhachHang khachHang;

    @DocumentReference
    @Indexed(unique = true, sparse = true)
    private DonHang donHang;

    @DocumentReference
    private HopDong hopDong;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soTienNo;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soTienDaTt = BigDecimal.ZERO;


    private LocalDateTime ngayPhatSinh = LocalDateTime.now();


    private LocalDate hanThanhToan;


    private String trangThai = "CON_NO";


    private String ghiChu;

    public CongNo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public HopDong getHopDong() { return hopDong; }
    public void setHopDong(HopDong hopDong) { this.hopDong = hopDong; }
    public BigDecimal getSoTienNo() { return soTienNo; }
    public void setSoTienNo(BigDecimal soTienNo) { this.soTienNo = soTienNo; }
    public BigDecimal getSoTienDaTt() { return soTienDaTt; }
    public void setSoTienDaTt(BigDecimal soTienDaTt) { this.soTienDaTt = soTienDaTt; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayPhatSinh() { return ngayPhatSinh; }
    public void setNgayPhatSinh(LocalDateTime ngayPhatSinh) { this.ngayPhatSinh = ngayPhatSinh; }
    public LocalDate getHanThanhToan() { return hanThanhToan; }
    public void setHanThanhToan(LocalDate hanThanhToan) { this.hanThanhToan = hanThanhToan; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
