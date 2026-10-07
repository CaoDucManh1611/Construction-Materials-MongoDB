package com.example.ht_vlxd.Repository.sales;

import com.example.ht_vlxd.Model.sales.DoiTraHang;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface DoiTraHangRepository extends MongoRepository<DoiTraHang, Long> {
    DoiTraHang findByMaDoiTra(String maDoiTra);
    @Query("{ 'donHang': ?0 }")
    List<DoiTraHang> findByDonHangId(Long donHangId);
    @Query("{ 'khachHang': ?0 }")
    List<DoiTraHang> findByKhachHangId(Long khachHangId);
}
