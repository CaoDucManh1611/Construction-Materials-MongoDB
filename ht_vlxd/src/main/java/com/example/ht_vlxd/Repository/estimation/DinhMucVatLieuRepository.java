package com.example.ht_vlxd.Repository.estimation;

import com.example.ht_vlxd.Model.estimation.DinhMucVatLieu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

@Repository
public interface DinhMucVatLieuRepository extends MongoRepository<DinhMucVatLieu, Long> {
    List<DinhMucVatLieu> findByLoaiCongTrinh(String loaiCongTrinh);
}
