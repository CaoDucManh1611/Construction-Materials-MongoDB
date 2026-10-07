package com.example.ht_vlxd.Repository.customer;

import com.example.ht_vlxd.Model.customer.KhachHang;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface KhachHangRepository extends MongoRepository<KhachHang, Long> {
    KhachHang findByMaKhachHang(String maKhachHang);
    default KhachHang findByNguoiDungUsername(String username) {
        return findAll().stream().filter(k -> k.getNguoiDung() != null && username.equals(k.getNguoiDung().getUsername())).findFirst().orElse(null);
    }
}
