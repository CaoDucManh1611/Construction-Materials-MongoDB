package com.example.ht_vlxd.Model.inventory;
import com.example.ht_vlxd.Model.product.HangHoa;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "ton_kho")
@org.springframework.data.mongodb.core.index.CompoundIndex(name = "TonKho_unique", def = "{'hangHoa':1,'kho':1}", unique = true)
public class TonKho {
    public void setNgayCapNhat(LocalDateTime value) { this.ngayCapNhat = value; }
    @Id
    private Long id;

    @Version
    private Long version;

    @DocumentReference
    private HangHoa hangHoa;

    @DocumentReference
    private Kho kho;


    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal soLuong = BigDecimal.ZERO;


    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    public TonKho() {}

    public TonKho(Long id, HangHoa hangHoa, Kho kho, BigDecimal soLuong) {
        this.id = id;
        this.hangHoa = hangHoa;
        this.kho = kho;
        this.soLuong = soLuong;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public HangHoa getHangHoa() { return hangHoa; }
    public void setHangHoa(HangHoa hangHoa) { this.hangHoa = hangHoa; }
    public Kho getKho() { return kho; }
    public void setKho(Kho kho) { this.kho = kho; }
    public BigDecimal getSoLuong() { return soLuong; }
    public void setSoLuong(BigDecimal soLuong) { this.soLuong = soLuong; }
}
