package com.example.ht_vlxd.Repository.inventory;

import com.example.ht_vlxd.Model.inventory.TonKho;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface TonKhoRepository extends MongoRepository<TonKho, Long> {
    @Query("{ 'hangHoa': ?0, 'kho': ?1 }")
    TonKho findByHangHoaIdAndKhoId(Long productId, Long warehouseId);
}
