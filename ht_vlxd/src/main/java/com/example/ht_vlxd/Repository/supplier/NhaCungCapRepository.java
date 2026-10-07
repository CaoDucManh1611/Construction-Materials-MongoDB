package com.example.ht_vlxd.Repository.supplier;

import com.example.ht_vlxd.Model.supplier.NhaCungCap;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface NhaCungCapRepository extends MongoRepository<NhaCungCap, Long> {
    NhaCungCap findByMaNcc(String maNcc);
}
