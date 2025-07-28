package com.mongo.reporter.backend.repository;

import com.mongo.reporter.backend.model.ReportConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportConfigRepository extends MongoRepository<ReportConfig, String> {
    List<ReportConfig> findByOwner(String owner);
    List<ReportConfig> findByPublicShareTrue();
    List<ReportConfig> findByOwnerOrPublicShareTrue(String owner);
    
    // 分类相关查询
    List<ReportConfig> findByCategoryAndOwner(String category, String owner);
    List<ReportConfig> findByCategoryAndPublicShareTrue(String category);
    List<ReportConfig> findByCategoryAndOwnerOrPublicShareTrue(String category, String owner);
    
    // 标签相关查询
    List<ReportConfig> findByTagsContaining(String tag);
    List<ReportConfig> findByTagsContainingAndOwner(String tag, String owner);
    List<ReportConfig> findByTagsContainingAndPublicShareTrue(String tag);
    List<ReportConfig> findByTagsContainingAndOwnerOrPublicShareTrue(String tag, String owner);
    
    // 获取所有分类
    @Query("SELECT DISTINCT category FROM report_configs WHERE owner = ?0 OR publicShare = true")
    List<String> findDistinctCategoriesByOwnerOrPublicShareTrue(String owner);
    
    @Query("SELECT DISTINCT category FROM report_configs WHERE publicShare = true")
    List<String> findDistinctCategoriesByPublicShareTrue();
    
    // 获取所有标签
    @Query("SELECT DISTINCT tags FROM report_configs WHERE owner = ?0 OR publicShare = true")
    List<String> findDistinctTagsByOwnerOrPublicShareTrue(String owner);
    
    @Query("SELECT DISTINCT tags FROM report_configs WHERE publicShare = true")
    List<String> findDistinctTagsByPublicShareTrue();
    
    // 搜索报表
    @Query("SELECT * FROM report_configs WHERE (owner = ?3 OR publicShare = true) AND (name LIKE %?0% OR description LIKE %?0%) AND (?1 IS NULL OR category = ?1) AND (?2 IS NULL OR tags CONTAINS ANY ?2)")
    List<ReportConfig> searchReportsByKeywordAndFilters(String keyword, String category, List<String> tags, String owner);
    
    @Query("SELECT * FROM report_configs WHERE publicShare = true AND (name LIKE %?0% OR description LIKE %?0%) AND (?1 IS NULL OR category = ?1) AND (?2 IS NULL OR tags CONTAINS ANY ?2)")
    List<ReportConfig> searchPublicReportsByKeywordAndFilters(String keyword, String category, List<String> tags);
} 