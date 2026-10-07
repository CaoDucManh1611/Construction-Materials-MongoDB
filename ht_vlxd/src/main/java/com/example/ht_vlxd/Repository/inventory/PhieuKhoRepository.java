package com.example.ht_vlxd.Repository.inventory;

import com.example.ht_vlxd.Model.inventory.PhieuKho;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface PhieuKhoRepository extends MongoRepository<PhieuKho, Long> {
    PhieuKho findByMaPhieu(String maPhieu);
    List<PhieuKho> findByLoaiPhieu(String loaiPhieu);
}
