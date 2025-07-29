package com.mongo.reporter.backend.repository;

import com.mongo.reporter.backend.model.ReportTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportTemplateRepository extends MongoRepository<ReportTemplate, String> {
    
    // 根据所有者查找模板
    List<ReportTemplate> findByOwner(String owner);
    
    // 根据分类查找模板
    List<ReportTemplate> findByCategory(String category);
    
    // 根据标签查找模板
    List<ReportTemplate> findByTagsContaining(String tag);
    
    // 查找公开的模板
    List<ReportTemplate> findByIsPublicTrue();
    
    // 根据状态查找模板
    List<ReportTemplate> findByStatus(String status);
    
    // 根据所有者或公开状态查找模板
    List<ReportTemplate> findByOwnerOrIsPublicTrue(String owner);
    
    // 根据分类和所有者或公开状态查找模板
    List<ReportTemplate> findByCategoryAndOwnerOrIsPublicTrue(String category, String owner);
    
    // 根据标签和所有者或公开状态查找模板
    List<ReportTemplate> findByTagsContainingAndOwnerOrIsPublicTrue(String tag, String owner);
    
    // 根据名称查找模板
    Optional<ReportTemplate> findByName(String name);
    
    // 根据名称和所有者查找模板
    Optional<ReportTemplate> findByNameAndOwner(String name, String owner);
    
    // 搜索模板
    @Query("{'$or': [{'owner': ?3}, {'isPublic': true}], '$and': [{'$or': [{'name': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}]}, {'$or': [{'category': ?1}, {'category': {'$exists': false}}]}]}")
    List<ReportTemplate> searchTemplatesByKeywordAndFilters(String keyword, String category, List<String> tags, String owner);
    
    // 获取所有分类
    @Query(value = "{}", fields = "{'category': 1}")
    List<ReportTemplate> findAllCategories();
    
    // 获取所有标签
    @Query(value = "{}", fields = "{'tags': 1}")
    List<ReportTemplate> findAllTags();
    
    // 获取热门模板（按使用次数排序）
    List<ReportTemplate> findByIsPublicTrueOrderByUsageCountDesc();
    
    // 获取高评分模板（按评分排序）
    List<ReportTemplate> findByIsPublicTrueOrderByRatingDesc();
    
    // 获取最新模板（按创建时间排序）
    List<ReportTemplate> findByIsPublicTrueOrderByCreatedAtDesc();
} 