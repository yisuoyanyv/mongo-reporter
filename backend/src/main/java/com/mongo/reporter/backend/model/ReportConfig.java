package com.mongo.reporter.backend.model;

import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "report_configs")
public class ReportConfig {
    @Id
    private String id;
    private String name;
    private String dataSource;
    private String collection;
    private String chartType;
    private Map<String, String> fieldMapping;
    private Map<String, Object> chartConfig;
    private String owner;
    private boolean publicShare;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    public Map<String, String> getFieldMapping() { return fieldMapping; }
    public void setFieldMapping(Map<String, String> fieldMapping) { this.fieldMapping = fieldMapping; }
    public Map<String, Object> getChartConfig() { return chartConfig; }
    public void setChartConfig(Map<String, Object> chartConfig) { this.chartConfig = chartConfig; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public boolean isPublicShare() { return publicShare; }
    public void setPublicShare(boolean publicShare) { this.publicShare = publicShare; }
} 