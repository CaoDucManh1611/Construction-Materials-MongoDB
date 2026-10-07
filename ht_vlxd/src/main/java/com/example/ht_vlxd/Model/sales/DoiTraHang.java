package com.example.ht_vlxd.Model.sales;
import com.example.ht_vlxd.Model.product.HangHoa;
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
import java.time.LocalDateTime;

@Document(collection = "doi_tra_hang")
public class DoiTraHang {
    private String phanLoai;
    public String getPhanLoai() { return phanLoai; }
    public void setPhanLoai(String value) { this.phanLoai = value; }
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal donGiaGoc = BigDecimal.ZERO;
    public BigDecimal getDonGiaGoc() { return donGiaGoc; }
    public void setDonGiaGoc(BigDecimal value) { this.donGiaGoc = value; }
    private Long khoNhanId;
    public Long getKhoNhanId() { return khoNhanId; }
    public void setKhoNhanId(Long value) { this.khoNhanId = value; }

    @Id
    private Long id;

    @Version
    private Long version;

    @Indexed(unique = true, sparse = true)
    private String maDoiTra;

    @DocumentReference
    private DonHang donHang;

    @DocumentReference
    private KhachHang khachHang;


    private String loai; // DOI, TRA


    private String lyDo;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soLuong;

    @DocumentReference
    private HangHoa hangHoa;


    private LocalDateTime ngayYeuCau = LocalDateTime.now();


    private String trangThai = "CHO_DUYET"; // CHO_DUYET, DA_DUYET, TU_CHOI, HOAN_THANH

    @DocumentReference
    private NguoiDung nguoiXuLy;


    private String ghiChuXuLy;


    private LocalDateTime ngayXuLy;

    public DoiTraHang() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaDoiTra() { return maDoiTra; }
    public void setMaDoiTra(String maDoiTra) { this.maDoiTra = maDoiTra; }
    public DonHang getDonHang() { return donHang; }
    public void setDonHang(DonHang donHang) { this.donHang = donHang; }
    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }
    public HangHoa getHangHoa() { return hangHoa; }
    public void setHangHoa(HangHoa hangHoa) { this.hangHoa = hangHoa; }
    public LocalDateTime getNgayYeuCau() { return ngayYeuCau; }
    public void setNgayYeuCau(LocalDateTime ngayYeuCau) { this.ngayYeuCau = ngayYeuCau; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public NguoiDung getNguoiXuLy() { return nguoiXuLy; }
    public void setNguoiXuLy(NguoiDung nguoiXuLy) { this.nguoiXuLy = nguoiXuLy; }
    public String getGhiChuXuLy() { return ghiChuXuLy; }
    public void setGhiChuXuLy(String ghiChuXuLy) { this.ghiChuXuLy = ghiChuXuLy; }
    public LocalDateTime getNgayXuLy() { return ngayXuLy; }
    public void setNgayXuLy(LocalDateTime ngayXuLy) { this.ngayXuLy = ngayXuLy; }
}
