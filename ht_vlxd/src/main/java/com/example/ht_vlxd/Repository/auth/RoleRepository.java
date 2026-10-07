package com.example.ht_vlxd.Repository.auth;

import com.example.ht_vlxd.Model.auth.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface RoleRepository extends MongoRepository<Role, Long> {
    Role findByName(String name);
}
