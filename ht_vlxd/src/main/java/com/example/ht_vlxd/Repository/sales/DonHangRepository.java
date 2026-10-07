package com.example.ht_vlxd.Repository.sales;

import com.example.ht_vlxd.Model.sales.DonHang;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

@Repository
public interface DonHangRepository extends MongoRepository<DonHang, Long> {
    DonHang findByMaDonHang(String maDonHang);
    @Query("{ 'khachHang': ?0 }")
    List<DonHang> findByKhachHangId(Long khachHangId);
}
