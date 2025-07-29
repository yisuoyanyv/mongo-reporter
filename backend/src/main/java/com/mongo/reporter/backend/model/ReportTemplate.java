package com.mongo.reporter.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.Map;

@Document(collection = "report_templates")
public class ReportTemplate {
    @Id
    private String id;
    private String name;
    private String description;
    private String category;
    private List<String> tags;
    private String owner;
    private boolean isPublic;
    private String version;
    private String status; // draft, published, archived
    private String createdAt;
    private String updatedAt;
    
    // 模板配置
    private List<Map<String, Object>> widgets;
    private Map<String, Object> fieldMapping;
    private List<Map<String, Object>> filters;
    private Map<String, Object> layout;
    private Map<String, Object> theme;
    
    // 使用统计
    private int usageCount;
    private double rating;
    private List<String> ratings; // 用户评分列表

    public ReportTemplate() {}

    public ReportTemplate(String name, String description, String owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.isPublic = false;
        this.status = "draft";
        this.version = "1.0.0";
        this.usageCount = 0;
        this.rating = 0.0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<Map<String, Object>> getWidgets() { return widgets; }
    public void setWidgets(List<Map<String, Object>> widgets) { this.widgets = widgets; }

    public Map<String, Object> getFieldMapping() { return fieldMapping; }
    public void setFieldMapping(Map<String, Object> fieldMapping) { this.fieldMapping = fieldMapping; }

    public List<Map<String, Object>> getFilters() { return filters; }
    public void setFilters(List<Map<String, Object>> filters) { this.filters = filters; }

    public Map<String, Object> getLayout() { return layout; }
    public void setLayout(Map<String, Object> layout) { this.layout = layout; }

    public Map<String, Object> getTheme() { return theme; }
    public void setTheme(Map<String, Object> theme) { this.theme = theme; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public List<String> getRatings() { return ratings; }
    public void setRatings(List<String> ratings) { this.ratings = ratings; }
} 