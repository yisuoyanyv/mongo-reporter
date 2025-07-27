package com.mongo.reporter.backend.repository;

import com.mongo.reporter.backend.model.ReportConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportConfigRepository extends MongoRepository<ReportConfig, String> {
    List<ReportConfig> findByOwner(String owner);
    List<ReportConfig> findByPublicShareTrue();
    List<ReportConfig> findByOwnerOrPublicShareTrue(String owner);
} 