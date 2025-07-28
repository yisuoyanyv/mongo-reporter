package com.mongo.reporter.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Map;

@Document(collection = "report_configs")
public class ReportConfig {
    @Id
    private String id;
    private String name;
    private String description;
    private String owner;
    private String dataSourceUri;
    private String collection;
    private List<Map<String, Object>> widgets;
    private Map<String, Object> fieldMapping;
    private List<Map<String, Object>> filters;
    private boolean publicShare;
    private String createdAt;
    private String updatedAt;
    
    // 新增字段：分类和标签
    private String category; // 报表分类
    private List<String> tags; // 标签列表
    private String version; // 版本号
    private String status; // 状态：draft(草稿)、published(已发布)、archived(已归档)

    public ReportConfig() {}

    public ReportConfig(String name, String description, String owner, String dataSourceUri, String collection) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.dataSourceUri = dataSourceUri;
        this.collection = collection;
        this.publicShare = false;
        this.status = "draft";
        this.version = "1.0.0";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getDataSourceUri() { return dataSourceUri; }
    public void setDataSourceUri(String dataSourceUri) { this.dataSourceUri = dataSourceUri; }

    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }

    public List<Map<String, Object>> getWidgets() { return widgets; }
    public void setWidgets(List<Map<String, Object>> widgets) { this.widgets = widgets; }

    public Map<String, Object> getFieldMapping() { return fieldMapping; }
    public void setFieldMapping(Map<String, Object> fieldMapping) { this.fieldMapping = fieldMapping; }

    public List<Map<String, Object>> getFilters() { return filters; }
    public void setFilters(List<Map<String, Object>> filters) { this.filters = filters; }

    public boolean isPublicShare() { return publicShare; }
    public void setPublicShare(boolean publicShare) { this.publicShare = publicShare; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    // 新增字段的getter和setter
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 