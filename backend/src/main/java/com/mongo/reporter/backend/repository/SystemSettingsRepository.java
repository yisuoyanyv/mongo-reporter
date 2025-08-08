package com.mongo.reporter.backend.repository;

import com.mongo.reporter.backend.model.SystemSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends MongoRepository<SystemSettings, String> {
    
    /**
     * 根据分类查找设置
     */
    Optional<SystemSettings> findByCategory(String category);
    
    /**
     * 查找所有设置分类
     */
    @Query("SELECT DISTINCT category FROM system_settings")
    List<String> findAllCategories();
    
    /**
     * 根据分类删除设置
     */
    void deleteByCategory(String category);
    
    /**
     * 检查分类是否存在
     */
    boolean existsByCategory(String category);
} 