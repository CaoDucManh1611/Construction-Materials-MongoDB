package com.example.ht_vlxd.Model.estimation;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;

@Document(collection = "dinh_muc_vat_lieu")
@org.springframework.data.mongodb.core.index.CompoundIndex(name = "DinhMucVatLieu_unique", def = "{'loaiCongTrinh':1,'loaiVatLieu':1}", unique = true)
public class DinhMucVatLieu {
    @Id
    private Long id;

    @Version
    private Long version;


    private String loaiCongTrinh;


    private String loaiVatLieu;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal heSoM2;


    private String donViTinh;

    private CachTinh cachTinh = CachTinh.THEO_TANG;

    private KieuLamTron kieuLamTron = KieuLamTron.DEM;


    private String ghiChu;

    public enum CachTinh {
        THEO_TANG, MOT_LAN
    }

    public enum KieuLamTron {
        DEM, THE_TICH
    }

    public DinhMucVatLieu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLoaiCongTrinh() { return loaiCongTrinh; }
    public void setLoaiCongTrinh(String loaiCongTrinh) { this.loaiCongTrinh = loaiCongTrinh; }
    public String getLoaiVatLieu() { return loaiVatLieu; }
    public void setLoaiVatLieu(String loaiVatLieu) { this.loaiVatLieu = loaiVatLieu; }
    public BigDecimal getHeSoM2() { return heSoM2; }
    public void setHeSoM2(BigDecimal heSoM2) { this.heSoM2 = heSoM2; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public CachTinh getCachTinh() { return cachTinh; }
    public void setCachTinh(CachTinh cachTinh) { this.cachTinh = cachTinh; }
    public KieuLamTron getKieuLamTron() { return kieuLamTron; }
    public void setKieuLamTron(KieuLamTron kieuLamTron) { this.kieuLamTron = kieuLamTron; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
