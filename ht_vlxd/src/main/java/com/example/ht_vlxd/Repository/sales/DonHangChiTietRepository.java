package com.example.ht_vlxd.Repository.sales;

import com.example.ht_vlxd.Model.sales.DonHang;
import com.example.ht_vlxd.Model.sales.DonHangChiTiet;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.*;
import org.bson.Document;
import java.util.*;

/** Compatibility API; detail rows are embedded in the parent document. */
@Repository
public class DonHangChiTietRepository {
    private final DonHangRepository parents;
    private final MongoTemplate mongo;
    public DonHangChiTietRepository(DonHangRepository parents, MongoTemplate mongo) {
        this.parents = parents; this.mongo = mongo;
    }
    public List<DonHangChiTiet> findByDonHangId(Long id) {
        DonHang parent = parents.findById(id).orElse(null);
        if (parent == null) return List.of();
        parent.getChiTiet().forEach(line -> line.setDonHang(parent));
        return parent.getChiTiet();
    }
    public List<DonHangChiTiet> findAll() {
        return parents.findAll().stream().flatMap(parent -> {
            parent.getChiTiet().forEach(line -> line.setDonHang(parent));
            return parent.getChiTiet().stream();
        }).toList();
    }
    public DonHangChiTiet save(DonHangChiTiet line) {
        if (line.getDonHang() == null || line.getDonHang().getId() == null)
            throw new IllegalArgumentException("Chi tiết phải thuộc chứng từ đã lưu.");
        if (line.getSoLuong() == null || line.getSoLuong().signum() <= 0)
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        if (line.getId() == null) {
            Document counter = mongo.findAndModify(Query.query(Criteria.where("_id").is("DonHangChiTiet")),
                new Update().inc("value", 1L), FindAndModifyOptions.options().upsert(true).returnNew(true), Document.class, "sequences");
            line.setId(((Number) counter.get("value")).longValue());
        }
        var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(line.getDonHang().getId())
            .and("chiTiet._id").ne(line.getId())), new Update().push("chiTiet", line), DonHang.class);
        if (result.getModifiedCount() != 1) throw new IllegalArgumentException("Chi tiết đã tồn tại hoặc chứng từ không tồn tại.");
        line.getDonHang().getChiTiet().add(line);
        return line;
    }
}
