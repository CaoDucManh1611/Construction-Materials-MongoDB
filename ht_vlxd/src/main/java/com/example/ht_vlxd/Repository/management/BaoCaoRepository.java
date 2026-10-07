package com.example.ht_vlxd.Repository.management;

import com.example.ht_vlxd.Model.management.BaoCao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface BaoCaoRepository extends MongoRepository<BaoCao, Long> {
    List<BaoCao> findByLoai(String loai);
}
