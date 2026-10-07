package com.example.ht_vlxd.Repository.inventory;

import com.example.ht_vlxd.Model.inventory.PhieuKho;
import com.example.ht_vlxd.Model.inventory.PhieuKhoChiTiet;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.*;
import org.bson.Document;
import java.util.*;

/** Compatibility API; detail rows are embedded in the parent document. */
@Repository
public class PhieuKhoChiTietRepository {
    private final PhieuKhoRepository parents;
    private final MongoTemplate mongo;
    public PhieuKhoChiTietRepository(PhieuKhoRepository parents, MongoTemplate mongo) {
        this.parents = parents; this.mongo = mongo;
    }
    public List<PhieuKhoChiTiet> findByPhieuKhoId(Long id) {
        PhieuKho parent = parents.findById(id).orElse(null);
        if (parent == null) return List.of();
        parent.getChiTiet().forEach(line -> line.setPhieuKho(parent));
        return parent.getChiTiet();
    }
    public List<PhieuKhoChiTiet> findAll() {
        return parents.findAll().stream().flatMap(parent -> {
            parent.getChiTiet().forEach(line -> line.setPhieuKho(parent));
            return parent.getChiTiet().stream();
        }).toList();
    }
    public PhieuKhoChiTiet save(PhieuKhoChiTiet line) {
        if (line.getPhieuKho() == null || line.getPhieuKho().getId() == null)
            throw new IllegalArgumentException("Chi tiết phải thuộc chứng từ đã lưu.");
        if (line.getSoLuong() == null || line.getSoLuong().signum() <= 0)
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        if (line.getId() == null) {
            Document counter = mongo.findAndModify(Query.query(Criteria.where("_id").is("PhieuKhoChiTiet")),
                new Update().inc("value", 1L), FindAndModifyOptions.options().upsert(true).returnNew(true), Document.class, "sequences");
            line.setId(((Number) counter.get("value")).longValue());
        }
        var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(line.getPhieuKho().getId())
            .and("chiTiet._id").ne(line.getId())), new Update().push("chiTiet", line), PhieuKho.class);
        if (result.getModifiedCount() != 1) throw new IllegalArgumentException("Chi tiết đã tồn tại hoặc chứng từ không tồn tại.");
        line.getPhieuKho().getChiTiet().add(line);
        return line;
    }
}
