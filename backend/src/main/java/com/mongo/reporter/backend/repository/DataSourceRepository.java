package com.mongo.reporter.backend.repository;

import com.mongo.reporter.backend.model.DataSource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSourceRepository extends MongoRepository<DataSource, String> {
    List<DataSource> findByOwner(String owner);
    List<DataSource> findByIsDefaultTrue();
    List<DataSource> findByOwnerOrIsDefaultTrue(String owner);
} 