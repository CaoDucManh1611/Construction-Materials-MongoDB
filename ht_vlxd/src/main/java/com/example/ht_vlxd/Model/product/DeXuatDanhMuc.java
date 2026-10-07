package com.example.ht_vlxd.Model.product;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Document(collection = "de_xuat_danh_muc")
public class DeXuatDanhMuc {
    @Id private Long id;
    @Version private Long version;
    private String loai;
    private String thaoTac;
    private HangHoa hangHoa;
    private DanhMuc danhMuc;
    private Long doiTuongId;
    private Long nguoiLapId;
    private Long nguoiDuyetId;
    private String trangThai = "CHO_DUYET";
    private LocalDateTime ngayLap = LocalDateTime.now();
    public Long getId() { return id; }
    public String getLoai() { return loai; } public void setLoai(String v) { loai = v; }
    public String getThaoTac() { return thaoTac; } public void setThaoTac(String v) { thaoTac = v; }
    public HangHoa getHangHoa() { return hangHoa; } public void setHangHoa(HangHoa v) { hangHoa = v; }
    public DanhMuc getDanhMuc() { return danhMuc; } public void setDanhMuc(DanhMuc v) { danhMuc = v; }
    public Long getDoiTuongId() { return doiTuongId; } public void setDoiTuongId(Long v) { doiTuongId = v; }
    public Long getNguoiLapId() { return nguoiLapId; } public void setNguoiLapId(Long v) { nguoiLapId = v; }
    public Long getNguoiDuyetId() { return nguoiDuyetId; } public void setNguoiDuyetId(Long v) { nguoiDuyetId = v; }
    public String getTrangThai() { return trangThai; } public void setTrangThai(String v) { trangThai = v; }
    public LocalDateTime getNgayLap() { return ngayLap; }
}
