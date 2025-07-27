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

    public ReportConfig() {}

    public ReportConfig(String name, String description, String owner, String dataSourceUri, String collection) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.dataSourceUri = dataSourceUri;
        this.collection = collection;
        this.publicShare = false;
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
} 