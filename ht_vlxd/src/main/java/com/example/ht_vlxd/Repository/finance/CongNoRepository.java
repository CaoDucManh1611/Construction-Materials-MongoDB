package com.example.ht_vlxd.Repository.finance;

import com.example.ht_vlxd.Model.finance.CongNo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface CongNoRepository extends MongoRepository<CongNo, Long> {
    @Query("{ 'khachHang': ?0 }")
    List<CongNo> findByKhachHangId(Long khachHangId);
    @Query("{ 'donHang': ?0 }")
    CongNo findByDonHangId(Long id);
}
