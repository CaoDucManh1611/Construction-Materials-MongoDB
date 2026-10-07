package com.example.ht_vlxd.Config.common;

import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MongoConfig {
    @Bean
    org.springframework.boot.ApplicationRunner prepareCollections(MongoTemplate mongo) {
        return args -> {
            mongo.getConverter().getMappingContext().getPersistentEntity(com.example.ht_vlxd.Model.finance.HoaDon.class);
            mongo.getConverter().getMappingContext().getPersistentEntity(com.example.ht_vlxd.Model.inventory.LenhXuat.class);
            mongo.getConverter().getMappingContext().getPersistentEntity(com.example.ht_vlxd.Model.sales.BangBaoGia.class);
            mongo.getConverter().getMappingContext().getPersistentEntity(com.example.ht_vlxd.Model.product.DeXuatDanhMuc.class);
            if (!mongo.collectionExists("sequences")) mongo.createCollection("sequences");
            var resolver = new org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver(mongo.getConverter().getMappingContext());
            for (var entity : mongo.getConverter().getMappingContext().getPersistentEntities()) {
                if (entity.getType().isAnnotationPresent(org.springframework.data.mongodb.core.mapping.Document.class)) {
                    if (!mongo.collectionExists(entity.getCollection())) mongo.createCollection(entity.getCollection());
                    // Proposal snapshots may repeat a business code across revisions; only the catalog owns that uniqueness.
                    if (entity.getType() != com.example.ht_vlxd.Model.product.DeXuatDanhMuc.class)
                        for (var index : resolver.resolveIndexFor(entity.getType())) mongo.indexOps(entity.getType()).ensureIndex(index);
                }
            }
        };
    }
    @Bean
    MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    // Keep numeric API identifiers; allocate them atomically in MongoDB.
    @Bean
    BeforeConvertCallback<Object> numericIds(org.springframework.beans.factory.ObjectProvider<MongoTemplate> templates) {
        return (entity, collection) -> {
            try {
                var field = entity.getClass().getDeclaredField("id");
                field.setAccessible(true);
                if (field.getType() == Long.class && field.get(entity) == null) {
                    Document counter = templates.getObject().findAndModify(
                        Query.query(Criteria.where("_id").is(collection)),
                        new Update().inc("value", 1L),
                        FindAndModifyOptions.options().upsert(true).returnNew(true),
                        Document.class, "sequences");
                    field.set(entity, ((Number) counter.get("value")).longValue());
                }
            } catch (NoSuchFieldException ignored) {
                // Value objects have no identifier.
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Cannot allocate document identifier", ex);
            }
            return entity;
        };
    }
}
