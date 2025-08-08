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
    
    // 统计方法
    long countByPublicShare(boolean publicShare);
    
    // 分类相关查询
    List<ReportConfig> findByCategoryAndOwner(String category, String owner);
    List<ReportConfig> findByCategoryAndPublicShareTrue(String category);
    List<ReportConfig> findByCategoryAndOwnerOrPublicShareTrue(String category, String owner);
    
    // 标签相关查询
    List<ReportConfig> findByTagsContaining(String tag);
    List<ReportConfig> findByTagsContainingAndOwner(String tag, String owner);
    List<ReportConfig> findByTagsContainingAndPublicShareTrue(String tag);
    List<ReportConfig> findByTagsContainingAndOwnerOrPublicShareTrue(String tag, String owner);
    
    // 获取所有分类 - 使用MongoDB聚合查询
    @Query(value = "{}", fields = "{'category': 1}")
    List<ReportConfig> findAllCategories();
    
    // 获取所有标签 - 使用MongoDB聚合查询
    @Query(value = "{}", fields = "{'tags': 1}")
    List<ReportConfig> findAllTags();
    
    // 搜索报表 - 使用MongoDB查询语法
    @Query("{'$or': [{'owner': ?3}, {'publicShare': true}], '$and': [{'$or': [{'name': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}]}, {'$or': [{'category': ?1}, {'category': {'$exists': false}}]}]}")
    List<ReportConfig> searchReportsByKeywordAndFilters(String keyword, String category, List<String> tags, String owner);
    
    @Query("{'publicShare': true, '$and': [{'$or': [{'name': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}]}, {'$or': [{'category': ?1}, {'category': {'$exists': false}}]}]}")
    List<ReportConfig> searchPublicReportsByKeywordAndFilters(String keyword, String category, List<String> tags);
    
    // 按日期范围查询报表数量
    @Query("{'createdAt': {'$gte': ?0, '$lte': ?1}}")
    long countByCreatedAtBetween(String startDate, String endDate);
    
    // 按分类统计报表数量
    @Query("{'category': ?0}")
    long countByCategory(String category);
    
    // 按标签统计报表数量
    @Query("{'tags': {'$in': [?0]}}")
    long countByTagsContaining(String tag);
} 