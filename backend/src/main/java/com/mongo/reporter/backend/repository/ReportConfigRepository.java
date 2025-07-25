package com.mongo.reporter.backend.repository;

import com.mongo.reporter.backend.model.ReportConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportConfigRepository extends MongoRepository<ReportConfig, String> {
} 