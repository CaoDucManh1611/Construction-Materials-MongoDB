package com.example.ht_vlxd.Repository.finance;

import com.example.ht_vlxd.Model.finance.ThanhToan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface ThanhToanRepository extends MongoRepository<ThanhToan, Long> {
    ThanhToan findByMaThanhToan(String maThanhToan);
    @Query("{ 'congNo': ?0 }")
    List<ThanhToan> findByCongNoId(Long congNoId);
    ThanhToan findByMaGiaoDich(String code);
}
