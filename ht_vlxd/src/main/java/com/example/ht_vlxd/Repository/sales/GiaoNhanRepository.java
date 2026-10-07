package com.example.ht_vlxd.Repository.sales;

import com.example.ht_vlxd.Model.sales.GiaoNhan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface GiaoNhanRepository extends MongoRepository<GiaoNhan, Long> {
    GiaoNhan findByMaGiaoNhan(String maGiaoNhan);
    @Query("{ 'donHang': ?0 }")
    List<GiaoNhan> findByDonHangId(Long donHangId);
    @Query("{ 'nvGiao': ?0 }")
    List<GiaoNhan> findByNvGiaoId(Long nvGiaoId);
}
