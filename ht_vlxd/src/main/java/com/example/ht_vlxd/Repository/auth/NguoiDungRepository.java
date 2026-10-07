package com.example.ht_vlxd.Repository.auth;

import com.example.ht_vlxd.Model.auth.NguoiDung;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface NguoiDungRepository extends MongoRepository<NguoiDung, Long> {
    NguoiDung findByUsername(String username);
    NguoiDung findByEmail(String email);
    NguoiDung findBySoDienThoai(String soDienThoai);
    java.util.List<NguoiDung> findByHoTen(String hoTen);
    default java.util.List<NguoiDung> findByRoleName(String roleName) {
        return findAll().stream().filter(u -> u.getRole() != null && roleName.equals(u.getRole().getName())).toList();
    }
}
