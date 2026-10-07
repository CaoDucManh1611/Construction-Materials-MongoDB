package com.example.ht_vlxd.Model.sales;
import com.example.ht_vlxd.Model.customer.KhachHang;
import com.example.ht_vlxd.Model.auth.NguoiDung;

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

@Document(collection = "hop_dong")
public class HopDong {
    private Long nguoiDuyetId;
    public Long getNguoiDuyetId() { return nguoiDuyetId; }
    public void setNguoiDuyetId(Long value) { this.nguoiDuyetId = value; }
    private LocalDateTime ngayDuyet;
    public LocalDateTime getNgayDuyet() { return ngayDuyet; }
    public void setNgayDuyet(LocalDateTime value) { this.ngayDuyet = value; }
    private java.util.List<DonHangChiTiet> chiTiet = new java.util.ArrayList<>();
    public java.util.List<DonHangChiTiet> getChiTiet() { return chiTiet; }
    public void setChiTiet(java.util.List<DonHangChiTiet> value) { this.chiTiet = value; }
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal tienDaThu = BigDecimal.ZERO;
    public BigDecimal getTienDaThu() { return tienDaThu; }
    public void setTienDaThu(BigDecimal value) { this.tienDaThu = value; }

    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maHopDong;

    @DocumentReference
    private DonHang donHang;

    @DocumentReference
    private KhachHang khachHang;

    @DocumentReference
    private NguoiDung nvLap;


    private LocalDate ngayKy;


    private LocalDate ngayHieuLuc;


    private LocalDate ngayHetHan;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal giaTri;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal tienDatCoc = BigDecimal.ZERO;


    private String dieuKhoanTt;


    private String noiDung;


    private String trangThai = "NHAP";


    private String fileUrl;


    private LocalDateTime ngayTao = LocalDateTime.now();

    public HopDong() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaHopDong() { return maHopDong; }
    public void setMaHopDong(String maHopDong) { this.maHopDong = maHopDong; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public NguoiDung getNvLap() { return nvLap; }
    public void setNvLap(NguoiDung nvLap) { this.nvLap = nvLap; }
    public LocalDate getNgayKy() { return ngayKy; }
    public void setNgayKy(LocalDate ngayKy) { this.ngayKy = ngayKy; }
    public LocalDate getNgayHieuLuc() { return ngayHieuLuc; }
    public void setNgayHieuLuc(LocalDate ngayHieuLuc) { this.ngayHieuLuc = ngayHieuLuc; }
    public BigDecimal getGiaTri() { return giaTri; }
    public void setGiaTri(BigDecimal giaTri) { this.giaTri = giaTri; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDate getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(LocalDate ngayHetHan) { this.ngayHetHan = ngayHetHan; }
    public BigDecimal getTienDatCoc() { return tienDatCoc; }
    public void setTienDatCoc(BigDecimal tienDatCoc) { this.tienDatCoc = tienDatCoc; }
    public String getDieuKhoanTt() { return dieuKhoanTt; }
    public void setDieuKhoanTt(String dieuKhoanTt) { this.dieuKhoanTt = dieuKhoanTt; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
