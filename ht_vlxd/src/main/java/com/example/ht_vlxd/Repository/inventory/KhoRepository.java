package com.example.ht_vlxd.Repository.inventory;

import com.example.ht_vlxd.Model.inventory.Kho;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface KhoRepository extends MongoRepository<Kho, Long> {
    Kho findByMaKho(String maKho);
}
