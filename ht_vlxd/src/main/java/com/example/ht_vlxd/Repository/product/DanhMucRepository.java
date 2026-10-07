package com.example.ht_vlxd.Repository.product;

import com.example.ht_vlxd.Model.product.DanhMuc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface DanhMucRepository extends MongoRepository<DanhMuc, Long> {
    DanhMuc findByMaDanhMuc(String maDanhMuc);
}
