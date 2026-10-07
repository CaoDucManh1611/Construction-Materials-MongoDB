package com.example.ht_vlxd.Repository.product;

import com.example.ht_vlxd.Model.product.HangHoa;
import com.example.ht_vlxd.Model.product.TrangThaiHangHoa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface HangHoaRepository extends MongoRepository<HangHoa, Long> {
    HangHoa findByMaHang(String maHang);
    default List<HangHoa> findByDanhMucMaDanhMucAndTrangThai(String maDanhMuc, TrangThaiHangHoa trangThai) {
        return findAll().stream().filter(h -> h.getDanhMuc() != null && maDanhMuc.equals(h.getDanhMuc().getMaDanhMuc()) && trangThai == h.getTrangThai()).toList();
    }
}
