package com.example.ht_vlxd.Model.inventory;
import com.example.ht_vlxd.Model.sales.DonHang;
import com.example.ht_vlxd.Model.auth.NguoiDung;
import com.example.ht_vlxd.Model.supplier.NhaCungCap;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Document(collection = "phieu_kho")
public class PhieuKho {
    private Long nguoiDuyetId;
    public Long getNguoiDuyetId() { return nguoiDuyetId; }
    public void setNguoiDuyetId(Long value) { this.nguoiDuyetId = value; }
    private LocalDateTime ngayDuyet;
    public LocalDateTime getNgayDuyet() { return ngayDuyet; }
    public void setNgayDuyet(LocalDateTime value) { this.ngayDuyet = value; }
    private String bienBanNhapHang;
    public String getBienBanNhapHang() { return bienBanNhapHang; }
    public void setBienBanNhapHang(String value) { this.bienBanNhapHang = value; }
    private Long tonKhoKiemKeId;
    public Long getTonKhoKiemKeId() { return tonKhoKiemKeId; }
    public void setTonKhoKiemKeId(Long value) { this.tonKhoKiemKeId = value; }
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soLuongSoSach;
    public BigDecimal getSoLuongSoSach() { return soLuongSoSach; }
    public void setSoLuongSoSach(BigDecimal value) { this.soLuongSoSach = value; }
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soLuongThucTe;
    public BigDecimal getSoLuongThucTe() { return soLuongThucTe; }
    public void setSoLuongThucTe(BigDecimal value) { this.soLuongThucTe = value; }

    private java.util.List<PhieuKhoChiTiet> chiTiet = new java.util.ArrayList<>();
    public java.util.List<PhieuKhoChiTiet> getChiTiet() { return chiTiet; }
    public void setChiTiet(java.util.List<PhieuKhoChiTiet> chiTiet) { this.chiTiet = chiTiet; }

    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maPhieu;


    private String loaiPhieu; // NHAP, XUAT

    @DocumentReference
    private Kho kho;

    @DocumentReference
    private NguoiDung nguoiTao;

    @DocumentReference
    private DonHang donHang;

    @DocumentReference
    private NhaCungCap nhaCungCap;


    private LocalDateTime ngayLap = LocalDateTime.now();


    private String ghiChu;


    private String trangThai = "NHAP"; // NHAP, DA_DUYET, HUY

    public PhieuKho() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public String getLoaiPhieu() { return loaiPhieu; }
    public void setLoaiPhieu(String loaiPhieu) { this.loaiPhieu = loaiPhieu; }
    public Kho getKho() { return kho; }
    public void setKho(Kho kho) { this.kho = kho; }
    public NguoiDung getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(NguoiDung nguoiTao) { this.nguoiTao = nguoiTao; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
