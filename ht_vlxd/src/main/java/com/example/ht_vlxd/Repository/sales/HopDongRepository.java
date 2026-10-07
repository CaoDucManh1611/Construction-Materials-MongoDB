package com.example.ht_vlxd.Repository.sales;

import com.example.ht_vlxd.Model.sales.HopDong;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface HopDongRepository extends MongoRepository<HopDong, Long> {
    HopDong findByMaHopDong(String maHopDong);
    @Query("{ 'donHang': ?0 }")
    HopDong findByDonHangId(Long donHangId);
}
