package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.ReportConfig;
import com.mongo.reporter.backend.model.DataSource;
import com.mongo.reporter.backend.repository.ReportConfigRepository;
import com.mongo.reporter.backend.util.MongoConnectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import java.util.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.MongoException;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private ReportConfigRepository reportConfigRepository;
    
    @Autowired
    private MongoConnectionUtil mongoConnectionUtil;
    
    private final String jwtSecret = "mongo-reporter-secret-key-256-bits-long";
    private final SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    @GetMapping("/collections")
    public List<String> listCollectionsByUri(@RequestParam String uri) {
        return listCollectionsByUriWithAuth(uri, null, null, null);
    }
    
    @PostMapping("/collections/auth")
    public List<String> listCollectionsByUriWithAuth(@RequestParam String uri, 
                                                    @RequestParam(required = false) String username,
                                                    @RequestParam(required = false) String password,
                                                    @RequestParam(required = false) String authDatabase) {
        MongoClient mongoClient = null;
        try {
            // 创建数据源对象
            DataSource dataSource = new DataSource();
            dataSource.setUri(uri);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setAuthDatabase(authDatabase);
            dataSource.setUseAuth(username != null && password != null);
            
            mongoClient = mongoConnectionUtil.createMongoClient(dataSource);
            
            // 从URI中提取数据库名称
            String databaseName = mongoConnectionUtil.extractDatabaseName(uri);
            if (databaseName == null) {
                return new ArrayList<>();
            }
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoIterable<String> collectionNames = database.listCollectionNames();
            
            List<String> result = new ArrayList<>();
            for (String name : collectionNames) {
                result.add(name);
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (mongoClient != null) {
                try {
                    mongoClient.close();
                } catch (Exception e) {
                    // 忽略关闭时的异常
                }
            }
        }
    }

    @GetMapping("/fields")
    public List<String> listFieldsByUriAndCollection(@RequestParam String uri, @RequestParam String collection) {
        return listFieldsByUriAndCollectionWithAuth(uri, collection, null, null, null);
    }
    
    @PostMapping("/fields/auth")
    public List<String> listFieldsByUriAndCollectionWithAuth(@RequestParam String uri, 
                                                            @RequestParam String collection,
                                                            @RequestParam(required = false) String username,
                                                            @RequestParam(required = false) String password,
                                                            @RequestParam(required = false) String authDatabase) {
        MongoClient mongoClient = null;
        try {
            // 创建数据源对象
            DataSource dataSource = new DataSource();
            dataSource.setUri(uri);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setAuthDatabase(authDatabase);
            dataSource.setUseAuth(username != null && password != null);
            
            mongoClient = mongoConnectionUtil.createMongoClient(dataSource);
            
            // 从URI中提取数据库名称
            String databaseName = mongoConnectionUtil.extractDatabaseName(uri);
            if (databaseName == null) {
                return new ArrayList<>();
            }
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            List<Map> rawData = database.getCollection(collection, Map.class).find().limit(1).into(new ArrayList<>());
            
            if (rawData.isEmpty()) {
                return new ArrayList<>();
            }
            Map firstDoc = rawData.get(0);
            return new ArrayList<>(firstDoc.keySet());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (mongoClient != null) {
                try {
                    mongoClient.close();
                } catch (Exception e) {
                    // 忽略关闭时的异常
                }
            }
        }
    }

    @GetMapping("/data")
    public List<Map<String, Object>> getData(@RequestParam String uri, @RequestParam String collection, 
                                            @RequestParam(defaultValue = "100") int limit) {
        return getDataWithAuth(uri, collection, limit, null, null, null);
    }
    
    @PostMapping("/data/auth")
    public List<Map<String, Object>> getDataWithAuth(@RequestParam String uri, 
                                                    @RequestParam String collection,
                                                    @RequestParam(defaultValue = "100") int limit,
                                                    @RequestParam(required = false) String username,
                                                    @RequestParam(required = false) String password,
                                                    @RequestParam(required = false) String authDatabase) {
        MongoClient mongoClient = null;
        try {
            // 创建数据源对象
            DataSource dataSource = new DataSource();
            dataSource.setUri(uri);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setAuthDatabase(authDatabase);
            dataSource.setUseAuth(username != null && password != null);
            
            mongoClient = mongoConnectionUtil.createMongoClient(dataSource);
            
            // 从URI中提取数据库名称
            String databaseName = mongoConnectionUtil.extractDatabaseName(uri);
            if (databaseName == null) {
                return new ArrayList<>();
            }
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            List<Map> rawData = database.getCollection(collection, Map.class).find().limit(limit).into(new ArrayList<>());
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map item : rawData) {
                Map<String, Object> processedItem = new HashMap<>(item);
                // 处理ObjectId字段，将_id转换为字符串
                if (processedItem.containsKey("_id")) {
                    Object idValue = processedItem.get("_id");
                    if (idValue != null) {
                        processedItem.put("_id", idValue.toString());
                    }
                }
                result.add(processedItem);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (mongoClient != null) {
                try {
                    mongoClient.close();
                } catch (Exception e) {
                    // 忽略关闭时的异常
                }
            }
        }
    }

    @GetMapping("/configs")
    public List<ReportConfig> listReports(@RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            return reportConfigRepository.findByOwnerOrPublicShareTrue(username);
        }
        return reportConfigRepository.findByPublicShareTrue();
    }

    // 新增：按分类获取报表
    @GetMapping("/configs/category/{category}")
    public List<ReportConfig> getReportsByCategory(@PathVariable String category, 
                                                  @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            return reportConfigRepository.findByCategoryAndOwnerOrPublicShareTrue(category, username);
        }
        return reportConfigRepository.findByCategoryAndPublicShareTrue(category);
    }

    // 新增：按标签获取报表
    @GetMapping("/configs/tag/{tag}")
    public List<ReportConfig> getReportsByTag(@PathVariable String tag, 
                                             @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            return reportConfigRepository.findByTagsContainingAndOwnerOrPublicShareTrue(tag, username);
        }
        return reportConfigRepository.findByTagsContainingAndPublicShareTrue(tag);
    }

    // 新增：获取所有分类
    @GetMapping("/categories")
    public List<String> getAllCategories(@RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            return reportConfigRepository.findDistinctCategoriesByOwnerOrPublicShareTrue(username);
        }
        return reportConfigRepository.findDistinctCategoriesByPublicShareTrue();
    }

    // 新增：获取所有标签
    @GetMapping("/tags")
    public List<String> getAllTags(@RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            return reportConfigRepository.findDistinctTagsByOwnerOrPublicShareTrue(username);
        }
        return reportConfigRepository.findDistinctTagsByPublicShareTrue();
    }

    // 新增：搜索报表
    @GetMapping("/configs/search")
    public List<ReportConfig> searchReports(@RequestParam String keyword,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) List<String> tags,
                                          @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            return reportConfigRepository.searchReportsByKeywordAndFilters(keyword, category, tags, username);
        }
        return reportConfigRepository.searchPublicReportsByKeywordAndFilters(keyword, category, tags);
    }

    @GetMapping("/configs/{id}")
    public ReportConfig getReport(@PathVariable String id, @RequestHeader(value = "Authorization", required = false) String auth) {
        ReportConfig report = reportConfigRepository.findById(id).orElse(null);
        if (report == null) {
            return null;
        }
        
        // 如果是公开分享的报表，任何人都可以查看
        if (report.isPublicShare()) {
            return report;
        }
        
        // 如果有认证信息，检查是否是所有者
        if (auth != null) {
            String username = getUsernameFromToken(auth);
            if (username != null && username.equals(report.getOwner())) {
                return report;
            }
        }
        
        // 开发环境：如果owner为null，允许访问（用于测试）
        if (report.getOwner() == null) {
            return report;
        }
        
        // 默认情况下，如果没有认证或不是所有者，返回null
        return null;
    }

    @PostMapping("/configs")
    public ReportConfig saveReport(@RequestBody ReportConfig report, @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            report.setOwner(username);
        }
        
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(new Date().toString());
        }
        report.setUpdatedAt(new Date().toString());
        
        return reportConfigRepository.save(report);
    }

    @PutMapping("/configs/{id}")
    public ReportConfig updateReport(@PathVariable String id, @RequestBody ReportConfig report, @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        ReportConfig existing = reportConfigRepository.findById(id).orElse(null);
        if (existing != null && (existing.isPublicShare() || username.equals(existing.getOwner()))) {
            report.setId(id);
            if (username != null) {
                report.setOwner(username);
            }
            // 保留原有的创建时间
            if (existing.getCreatedAt() != null) {
                report.setCreatedAt(existing.getCreatedAt());
            } else if (report.getCreatedAt() == null) {
                report.setCreatedAt(new Date().toString());
            }
            report.setUpdatedAt(new Date().toString());
            return reportConfigRepository.save(report);
        }
        throw new IllegalArgumentException("Invalid report or permission denied");
    }

    @DeleteMapping("/configs/{id}")
    public void deleteReport(@PathVariable String id, @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        ReportConfig report = reportConfigRepository.findById(id).orElse(null);
        if (report != null && (report.isPublicShare() || username.equals(report.getOwner()))) {
            reportConfigRepository.deleteById(id);
        }
    }

    @PostMapping("/chart-data")
    public Map<String, Object> getChartData(@RequestBody Map<String, Object> request, 
                                           @RequestHeader(value = "Authorization", required = false) String auth) {
        String uri = (String) request.get("uri");
        String collection = (String) request.get("collection");
        Map<String, Object> widget = (Map<String, Object>) request.get("widget");
        List<Map<String, Object>> filters = (List<Map<String, Object>>) request.get("filters");
        
        if (uri == null || collection == null || widget == null) {
            return Map.of("success", false, "message", "缺少必要参数");
        }
        
        MongoClient mongoClient = null;
        try {
            // 检查是否有认证信息
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String authDatabase = (String) request.get("authDatabase");
            
            // 创建数据源对象
            DataSource dataSource = new DataSource();
            dataSource.setUri(uri);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setAuthDatabase(authDatabase);
            dataSource.setUseAuth(username != null && password != null);
            
            mongoClient = mongoConnectionUtil.createMongoClient(dataSource);
            
            // 从URI中提取数据库名称
            String databaseName = mongoConnectionUtil.extractDatabaseName(uri);
            if (databaseName == null) {
                return Map.of("success", false, "message", "无法解析数据库名称");
            }
            
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            
            // 应用过滤条件
            List<Map> rawData = database.getCollection(collection, Map.class).find().into(new ArrayList<>());
            List<Map<String, Object>> data = new ArrayList<>();
            for (Map item : rawData) {
                data.add((Map<String, Object>) item);
            }
            
            // 应用过滤条件
            if (filters != null && !filters.isEmpty()) {
                data = applyFilters(data, filters);
            }
            
            // 兼容 name 和 type 字段
            String chartType = (String) widget.get("name");
            if (chartType == null) {
                chartType = (String) widget.get("type");
            }
            if (chartType == null) {
                return Map.of("success", false, "message", "图表类型未指定");
            }
            
            if ("line".equals(chartType) || "bar".equals(chartType)) {
                return processLineBarChart(data, widget);
            } else if ("pie".equals(chartType)) {
                return processPieChart(data, widget);
            } else if ("scatter".equals(chartType)) {
                return processScatterChart(data, widget);
            } else if ("gauge".equals(chartType)) {
                return processGaugeChart(data, widget);
            } else if ("funnel".equals(chartType)) {
                return processFunnelChart(data, widget);
            } else if ("radar".equals(chartType)) {
                return processRadarChart(data, widget);
            } else if ("table".equals(chartType)) {
                return processTableData(data, widget);
            } else if ("area".equals(chartType) || "stacked".equals(chartType)) {
                return processAreaStackedChart(data, widget);
            } else if ("heatmap".equals(chartType)) {
                return processHeatmapChart(data, widget);
            } else if ("sankey".equals(chartType)) {
                return processSankeyChart(data, widget);
            } else if ("tree".equals(chartType)) {
                return processTreeChart(data, widget);
            } else if ("map".equals(chartType)) {
                return processMapChart(data, widget);
            } else if ("dashboard".equals(chartType)) {
                return processDashboardChart(data, widget);
            } else if ("candlestick".equals(chartType)) {
                return processCandlestickChart(data, widget);
            } else {
                return Map.of("success", false, "message", "不支持的图表类型: " + chartType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "获取数据失败: " + e.getMessage());
        } finally {
            if (mongoClient != null) {
                try {
                    mongoClient.close();
                } catch (Exception e) {
                    // 忽略关闭时的异常
                }
            }
        }
    }

    private List<Map<String, Object>> applyFilters(List<Map<String, Object>> data, List<Map<String, Object>> filters) {
        List<Map<String, Object>> filteredData = new ArrayList<>();
        
        for (Map<String, Object> item : data) {
            boolean include = true;
            
            for (Map<String, Object> filter : filters) {
                String field = (String) filter.get("field");
                String operator = (String) filter.get("operator");
                Object value = filter.get("value");
                String logic = (String) filter.get("logic");
                
                if (field == null || operator == null || value == null) {
                    continue;
                }
                
                Object fieldValue = item.get(field);
                boolean conditionMet = evaluateCondition(fieldValue, operator, value);
                
                if ("or".equals(logic)) {
                    include = include || conditionMet;
                } else {
                    include = include && conditionMet;
                }
            }
            
            if (include) {
                filteredData.add(item);
            }
        }
        
        return filteredData;
    }
    
    private boolean evaluateCondition(Object fieldValue, String operator, Object filterValue) {
        if (fieldValue == null) {
            return false;
        }
        
        String fieldStr = String.valueOf(fieldValue);
        String filterStr = String.valueOf(filterValue);
        
        switch (operator) {
            case "eq":
                return fieldStr.equals(filterStr);
            case "ne":
                return !fieldStr.equals(filterStr);
            case "gt":
                try {
                    double fieldNum = Double.parseDouble(fieldStr);
                    double filterNum = Double.parseDouble(filterStr);
                    return fieldNum > filterNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "gte":
                try {
                    double fieldNum = Double.parseDouble(fieldStr);
                    double filterNum = Double.parseDouble(filterStr);
                    return fieldNum >= filterNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "lt":
                try {
                    double fieldNum = Double.parseDouble(fieldStr);
                    double filterNum = Double.parseDouble(filterStr);
                    return fieldNum < filterNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "lte":
                try {
                    double fieldNum = Double.parseDouble(fieldStr);
                    double filterNum = Double.parseDouble(filterStr);
                    return fieldNum <= filterNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "in":
                return fieldStr.contains(filterStr);
            case "nin":
                return !fieldStr.contains(filterStr);
            case "regex":
                return fieldStr.matches(filterStr);
            default:
                return false;
        }
    }

    private Map<String, Object> processLineBarChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        String xField = (String) widget.get("xField");
        String yField = (String) widget.get("yField");
        String seriesField = (String) widget.get("seriesField");
        Boolean enableStats = (Boolean) widget.get("enableStats");
        String aggregation = (String) widget.get("aggregation");
        String groupBy = (String) widget.get("groupBy");
        String sortBy = (String) widget.get("sortBy");
        String sortOrder = (String) widget.get("sortOrder");
        Integer limit = (Integer) widget.get("limit");
        
        // 如果启用了统计功能
        if (enableStats != null && enableStats && aggregation != null) {
            return processAggregatedChart(data, widget, "line".equals(widget.get("name")) ? "line" : "bar");
        }
        
        Map<String, Map<String, Double>> seriesData = new HashMap<>();
        Set<String> xAxisValues = new LinkedHashSet<>();
        
        for (Map<String, Object> item : data) {
            String xValue = String.valueOf(item.get(xField));
            String seriesValue = seriesField != null ? String.valueOf(item.get(seriesField)) : "default";
            Number yValue = (Number) item.get(yField);
            
            xAxisValues.add(xValue);
            seriesData.computeIfAbsent(seriesValue, k -> new HashMap<>()).put(xValue, yValue != null ? yValue.doubleValue() : 0.0);
        }
        
        List<String> xAxis = new ArrayList<>(xAxisValues);
        List<Map<String, Object>> series = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, Double>> entry : seriesData.entrySet()) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("name", entry.getKey());
            seriesItem.put("type", widget.get("name"));
            seriesItem.put("smooth", true);
            
            List<Double> seriesValues = new ArrayList<>();
            for (String xValue : xAxis) {
                seriesValues.add(entry.getValue().getOrDefault(xValue, 0.0));
            }
            seriesItem.put("data", seriesValues);
            
            series.add(seriesItem);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("xAxis", xAxis);
        result.put("series", series);
        
        return result;
    }

    private Map<String, Object> processAggregatedChart(List<Map<String, Object>> data, Map<String, Object> widget, String chartType) {
        String groupBy = (String) widget.get("groupBy");
        String aggregation = (String) widget.get("aggregation");
        String valueField = (String) widget.get("valueField");
        String sortBy = (String) widget.get("sortBy");
        String sortOrder = (String) widget.get("sortOrder");
        Integer limit = (Integer) widget.get("limit");
        
        Map<String, List<Number>> groupedData = new HashMap<>();
        
        for (Map<String, Object> item : data) {
            String groupKey = groupBy != null ? String.valueOf(item.get(groupBy)) : "总计";
            Number value = (Number) item.get(valueField);
            
            if (value != null) {
                groupedData.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(value);
            }
        }
        
        List<Map<String, Object>> aggregatedData = new ArrayList<>();
        for (Map.Entry<String, List<Number>> entry : groupedData.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", calculateAggregation(entry.getValue(), aggregation));
            aggregatedData.add(item);
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            aggregatedData.sort((a, b) -> {
                double aValue = ((Number) a.get("value")).doubleValue();
                double bValue = ((Number) b.get("value")).doubleValue();
                return "desc".equals(sortOrder) ? Double.compare(bValue, aValue) : Double.compare(aValue, bValue);
            });
        }
        
        // 限制数量
        if (limit != null && limit > 0 && aggregatedData.size() > limit) {
            aggregatedData = aggregatedData.subList(0, limit);
        }
        
        List<String> xAxis = aggregatedData.stream().map(item -> (String) item.get("name")).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        List<Number> yAxis = aggregatedData.stream().map(item -> (Number) item.get("value")).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        Map<String, Object> series = new HashMap<>();
        series.put("name", aggregation + "(" + valueField + ")");
        series.put("type", chartType);
        series.put("data", yAxis);
        if ("line".equals(chartType)) {
            series.put("smooth", true);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("xAxis", xAxis);
        result.put("series", List.of(series));
        
        return result;
    }
    
    private double calculateAggregation(List<Number> values, String aggregation) {
        if (values.isEmpty()) return 0.0;
        
        switch (aggregation) {
            case "sum":
                return values.stream().mapToDouble(Number::doubleValue).sum();
            case "avg":
                return values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
            case "count":
                return values.size();
            case "max":
                return values.stream().mapToDouble(Number::doubleValue).max().orElse(0.0);
            case "min":
                return values.stream().mapToDouble(Number::doubleValue).min().orElse(0.0);
            case "std":
                double avg = values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
                double variance = values.stream().mapToDouble(v -> Math.pow(v.doubleValue() - avg, 2)).average().orElse(0.0);
                return Math.sqrt(variance);
            case "variance":
                double mean = values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
                return values.stream().mapToDouble(v -> Math.pow(v.doubleValue() - mean, 2)).average().orElse(0.0);
            default:
                return 0.0;
        }
    }

    private Map<String, Object> processPieChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        String nameField = (String) widget.get("nameField");
        String valueField = (String) widget.get("valueField");
        Boolean enableStats = (Boolean) widget.get("enableStats");
        String aggregation = (String) widget.get("aggregation");
        String sortBy = (String) widget.get("sortBy");
        String sortOrder = (String) widget.get("sortOrder");
        Integer limit = (Integer) widget.get("limit");
        
        Map<String, Double> aggregatedData = new HashMap<>();
        
        for (Map<String, Object> item : data) {
            String name = String.valueOf(item.get(nameField));
            Number value = (Number) item.get(valueField);
            
            if (enableStats != null && enableStats && "count".equals(aggregation)) {
                aggregatedData.merge(name, 1.0, Double::sum);
            } else {
                aggregatedData.merge(name, value != null ? value.doubleValue() : 0.0, Double::sum);
            }
        }
        
        List<Map<String, Object>> seriesData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : aggregatedData.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            seriesData.add(item);
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            seriesData.sort((a, b) -> {
                double aValue = ((Number) a.get("value")).doubleValue();
                double bValue = ((Number) b.get("value")).doubleValue();
                return "desc".equals(sortOrder) ? Double.compare(bValue, aValue) : Double.compare(aValue, bValue);
            });
        }
        
        // 限制数量
        if (limit != null && limit > 0 && seriesData.size() > limit) {
            seriesData = seriesData.subList(0, limit);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("series", seriesData);
        
        return result;
    }
    
    private Map<String, Object> processScatterChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        String xField = (String) widget.get("xField");
        String yField = (String) widget.get("yField");
        String sizeField = (String) widget.get("sizeField");
        String seriesField = (String) widget.get("seriesField");
        
        Map<String, List<List<Object>>> seriesData = new HashMap<>();
        
        for (Map<String, Object> item : data) {
            Number xValue = (Number) item.get(xField);
            Number yValue = (Number) item.get(yField);
            Number sizeValue = sizeField != null ? (Number) item.get(sizeField) : 10;
            String seriesValue = seriesField != null ? String.valueOf(item.get(seriesField)) : "default";
            
            if (xValue != null && yValue != null) {
                List<Object> point = new ArrayList<>();
                point.add(xValue.doubleValue());
                point.add(yValue.doubleValue());
                if (sizeField != null && sizeValue != null) {
                    point.add(sizeValue.doubleValue());
                }
                
                seriesData.computeIfAbsent(seriesValue, k -> new ArrayList<>()).add(point);
            }
        }
        
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, List<List<Object>>> entry : seriesData.entrySet()) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("name", entry.getKey());
            seriesItem.put("type", "scatter");
            seriesItem.put("data", entry.getValue());
            series.add(seriesItem);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("series", series);
        
        return result;
    }
    
    private Map<String, Object> processGaugeChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        String valueField = (String) widget.get("valueField");
        Number min = (Number) widget.get("min");
        Number max = (Number) widget.get("max");
        Boolean enableStats = (Boolean) widget.get("enableStats");
        String aggregation = (String) widget.get("aggregation");
        
        // 收集数值数据
        List<Number> values = new ArrayList<>();
        for (Map<String, Object> item : data) {
            Number value = (Number) item.get(valueField);
            if (value != null) {
                values.add(value);
            }
        }
        
        // 根据统计配置计算值
        double calculatedValue;
        String displayName;
        
        if (enableStats != null && enableStats && aggregation != null) {
            calculatedValue = calculateAggregation(values, aggregation);
            displayName = getAggregationDisplayName(aggregation);
        } else {
            // 默认计算平均值
            calculatedValue = values.isEmpty() ? 0.0 : values.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
            displayName = "平均值";
        }
        
        double minValue = min != null ? min.doubleValue() : 0.0;
        double maxValue = max != null ? max.doubleValue() : 100.0;
        
        List<Map<String, Object>> seriesData = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("name", displayName);
        item.put("value", calculatedValue);
        seriesData.add(item);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("series", seriesData);
        result.put("min", minValue);
        result.put("max", maxValue);
        
        return result;
    }
    
    private Map<String, Object> processFunnelChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        String nameField = (String) widget.get("nameField");
        String valueField = (String) widget.get("valueField");
        Boolean enableStats = (Boolean) widget.get("enableStats");
        String aggregation = (String) widget.get("aggregation");
        String sortBy = (String) widget.get("sortBy");
        String sortOrder = (String) widget.get("sortOrder");
        Integer limit = (Integer) widget.get("limit");
        
        Map<String, Double> aggregatedData = new HashMap<>();
        
        for (Map<String, Object> item : data) {
            String name = String.valueOf(item.get(nameField));
            Number value = (Number) item.get(valueField);
            
            if (enableStats != null && enableStats && "count".equals(aggregation)) {
                aggregatedData.merge(name, 1.0, Double::sum);
            } else {
                aggregatedData.merge(name, value != null ? value.doubleValue() : 0.0, Double::sum);
            }
        }
        
        List<Map<String, Object>> seriesData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : aggregatedData.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            seriesData.add(item);
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            seriesData.sort((a, b) -> {
                double aValue = ((Number) a.get("value")).doubleValue();
                double bValue = ((Number) b.get("value")).doubleValue();
                return "desc".equals(sortOrder) ? Double.compare(bValue, aValue) : Double.compare(aValue, bValue);
            });
        }
        
        // 限制数量
        if (limit != null && limit > 0 && seriesData.size() > limit) {
            seriesData = seriesData.subList(0, limit);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("series", seriesData);
        
        return result;
    }
    
    private Map<String, Object> processRadarChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        String nameField = (String) widget.get("nameField");
        String valueField = (String) widget.get("valueField");
        String seriesField = (String) widget.get("seriesField");
        Boolean enableStats = (Boolean) widget.get("enableStats");
        String aggregation = (String) widget.get("aggregation");
        String sortBy = (String) widget.get("sortBy");
        String sortOrder = (String) widget.get("sortOrder");
        Integer limit = (Integer) widget.get("limit");
        
        Map<String, Map<String, Double>> seriesData = new HashMap<>();
        Set<String> indicators = new HashSet<>();
        
        for (Map<String, Object> item : data) {
            String name = String.valueOf(item.get(nameField));
            Number value = (Number) item.get(valueField);
            String series = seriesField != null ? String.valueOf(item.get(seriesField)) : "default";
            
            if (value != null) {
                indicators.add(name);
                seriesData.computeIfAbsent(series, k -> new HashMap<>()).merge(name, value.doubleValue(), Double::sum);
            }
        }
        
        // 构建雷达图数据
        List<String> indicatorList = new ArrayList<>(indicators);
        if (sortBy != null && !sortBy.isEmpty()) {
            // 排序指标
            indicatorList.sort((a, b) -> {
                double aValue = seriesData.values().stream().mapToDouble(m -> m.getOrDefault(a, 0.0)).sum();
                double bValue = seriesData.values().stream().mapToDouble(m -> m.getOrDefault(b, 0.0)).sum();
                return "desc".equals(sortOrder) ? Double.compare(bValue, aValue) : Double.compare(aValue, bValue);
            });
        }
        
        // 限制数量
        if (limit != null && limit > 0 && indicatorList.size() > limit) {
            indicatorList = indicatorList.subList(0, limit);
        }
        
        // 构建指标数据
        List<Map<String, Object>> indicatorsData = new ArrayList<>();
        for (String indicator : indicatorList) {
            Map<String, Object> indicatorItem = new HashMap<>();
            indicatorItem.put("name", indicator);
            indicatorItem.put("max", seriesData.values().stream().mapToDouble(m -> m.getOrDefault(indicator, 0.0)).max().orElse(100.0));
            indicatorsData.add(indicatorItem);
        }
        
        // 构建系列数据
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> entry : seriesData.entrySet()) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("name", entry.getKey());
            seriesItem.put("type", "radar");
            
            List<Double> values = new ArrayList<>();
            for (String indicator : indicatorList) {
                values.add(entry.getValue().getOrDefault(indicator, 0.0));
            }
            seriesItem.put("data", values);
            series.add(seriesItem);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("indicators", indicatorsData);
        result.put("series", series);
        
        return result;
    }
    
    private Map<String, Object> processTableData(List<Map<String, Object>> data, Map<String, Object> widget) {
        String sortBy = (String) widget.get("sortBy");
        String sortOrder = (String) widget.get("sortOrder");
        Integer limit = (Integer) widget.get("limit");
        List<String> displayFields = (List<String>) widget.get("displayFields");
        
        List<Map<String, Object>> tableData = new ArrayList<>(data);
        
        // 处理ObjectId字段，将_id转换为字符串
        tableData = tableData.stream().map(item -> {
            Map<String, Object> processedItem = new HashMap<>(item);
            if (processedItem.containsKey("_id")) {
                Object idValue = processedItem.get("_id");
                if (idValue != null) {
                    processedItem.put("_id", idValue.toString());
                }
            }
            return processedItem;
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        // 字段过滤
        if (displayFields != null && !displayFields.isEmpty()) {
            tableData = tableData.stream().map(item -> {
                Map<String, Object> filteredItem = new HashMap<>();
                for (String field : displayFields) {
                    if (item.containsKey(field)) {
                        filteredItem.put(field, item.get(field));
                    }
                }
                return filteredItem;
            }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            tableData.sort((a, b) -> {
                Object aValue = a.get(sortBy);
                Object bValue = b.get(sortBy);
                
                if (aValue instanceof Number && bValue instanceof Number) {
                    double aNum = ((Number) aValue).doubleValue();
                    double bNum = ((Number) bValue).doubleValue();
                    return "desc".equals(sortOrder) ? Double.compare(bNum, aNum) : Double.compare(aNum, bNum);
                } else {
                    String aStr = String.valueOf(aValue);
                    String bStr = String.valueOf(bValue);
                    return "desc".equals(sortOrder) ? bStr.compareTo(aStr) : aStr.compareTo(bStr);
                }
            });
        }
        
        // 限制数量
        if (limit != null && limit > 0 && tableData.size() > limit) {
            tableData = tableData.subList(0, limit);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", tableData);
        result.put("total", data.size());
        
        return result;
    }



    private String getUsernameFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String token = auth.substring(7);
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getAggregationDisplayName(String aggregation) {
        switch (aggregation) {
            case "sum":
                return "总和";
            case "avg":
                return "平均值";
            case "count":
                return "计数";
            case "max":
                return "最大值";
            case "min":
                return "最小值";
            case "std":
                return "标准差";
            case "variance":
                return "方差";
            default:
                return aggregation;
        }
    }
    
    // 新增图表类型处理方法
    
    private Map<String, Object> processAreaStackedChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        // 面积图和堆叠图处理逻辑
        String xField = (String) widget.get("xField");
        String yField = (String) widget.get("yField");
        String seriesField = (String) widget.get("seriesField");
        
        if (xField == null || yField == null) {
            return Map.of("success", false, "message", "缺少必要字段配置");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // 处理数据
        Map<String, List<Object>> seriesData = new HashMap<>();
        List<String> xAxisData = new ArrayList<>();
        
        for (Map<String, Object> item : data) {
            Object xValue = item.get(xField);
            Object yValue = item.get(yField);
            Object seriesValue = seriesField != null ? item.get(seriesField) : "default";
            
            if (xValue != null && yValue != null) {
                String xStr = String.valueOf(xValue);
                String seriesStr = String.valueOf(seriesValue);
                
                if (!xAxisData.contains(xStr)) {
                    xAxisData.add(xStr);
                }
                
                seriesData.computeIfAbsent(seriesStr, k -> new ArrayList<>()).add(yValue);
            }
        }
        
        // 构建系列数据
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map.Entry<String, List<Object>> entry : seriesData.entrySet()) {
            Map<String, Object> seriesItem = new HashMap<>();
            seriesItem.put("name", entry.getKey());
            seriesItem.put("type", "line");
            seriesItem.put("areaStyle", new HashMap<>());
            seriesItem.put("data", entry.getValue());
            series.add(seriesItem);
        }
        
        result.put("xAxis", xAxisData);
        result.put("series", series);
        
        return result;
    }
    
    private Map<String, Object> processHeatmapChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        // 热力图处理逻辑
        String xField = (String) widget.get("xField");
        String yField = (String) widget.get("yField");
        String valueField = (String) widget.get("valueField");
        
        if (xField == null || yField == null || valueField == null) {
            return Map.of("success", false, "message", "热力图需要配置x、y和value字段");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // 构建热力图数据
        List<List<Object>> heatmapData = new ArrayList<>();
        Set<String> xAxisData = new LinkedHashSet<>();
        Set<String> yAxisData = new LinkedHashSet<>();
        
        for (Map<String, Object> item : data) {
            Object xValue = item.get(xField);
            Object yValue = item.get(yField);
            Object value = item.get(valueField);
            
            if (xValue != null && yValue != null && value != null) {
                String xStr = String.valueOf(xValue);
                String yStr = String.valueOf(yValue);
                
                xAxisData.add(xStr);
                yAxisData.add(yStr);
                
                List<Object> dataPoint = new ArrayList<>();
                dataPoint.add(xStr);
                dataPoint.add(yStr);
                dataPoint.add(value);
                heatmapData.add(dataPoint);
            }
        }
        
        Map<String, Object> series = new HashMap<>();
        series.put("type", "heatmap");
        series.put("data", heatmapData);
        
        result.put("xAxis", new ArrayList<>(xAxisData));
        result.put("yAxis", new ArrayList<>(yAxisData));
        result.put("series", List.of(series));
        
        return result;
    }
    
    private Map<String, Object> processSankeyChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        // 桑基图处理逻辑
        String sourceField = (String) widget.get("sourceField");
        String targetField = (String) widget.get("targetField");
        String valueField = (String) widget.get("valueField");
        
        if (sourceField == null || targetField == null || valueField == null) {
            return Map.of("success", false, "message", "桑基图需要配置source、target和value字段");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // 构建桑基图数据
        Map<String, Double> linkMap = new HashMap<>();
        Set<String> nodes = new LinkedHashSet<>();
        
        for (Map<String, Object> item : data) {
            Object source = item.get(sourceField);
            Object target = item.get(targetField);
            Object value = item.get(valueField);
            
            if (source != null && target != null && value != null) {
                String sourceStr = String.valueOf(source);
                String targetStr = String.valueOf(target);
                Double valueNum = parseNumber(value);
                
                if (valueNum != null) {
                    nodes.add(sourceStr);
                    nodes.add(targetStr);
                    
                    String key = sourceStr + "->" + targetStr;
                    linkMap.merge(key, valueNum, Double::sum);
                }
            }
        }
        
        // 构建节点和链接数据
        List<Map<String, Object>> nodeData = new ArrayList<>();
        for (String node : nodes) {
            Map<String, Object> nodeItem = new HashMap<>();
            nodeItem.put("name", node);
            nodeData.add(nodeItem);
        }
        
        List<Map<String, Object>> linkData = new ArrayList<>();
        for (Map.Entry<String, Double> entry : linkMap.entrySet()) {
            String[] parts = entry.getKey().split("->");
            Map<String, Object> linkItem = new HashMap<>();
            linkItem.put("source", parts[0]);
            linkItem.put("target", parts[1]);
            linkItem.put("value", entry.getValue());
            linkData.add(linkItem);
        }
        
        result.put("nodes", nodeData);
        result.put("links", linkData);
        
        return result;
    }
    
    private Map<String, Object> processTreeChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        // 树图处理逻辑
        String nameField = (String) widget.get("nameField");
        String parentField = (String) widget.get("parentField");
        String valueField = (String) widget.get("valueField");
        
        if (nameField == null) {
            return Map.of("success", false, "message", "树图需要配置name字段");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // 构建树形数据
        Map<String, Map<String, Object>> nodeMap = new HashMap<>();
        Map<String, List<String>> childrenMap = new HashMap<>();
        
        for (Map<String, Object> item : data) {
            Object name = item.get(nameField);
            Object parent = parentField != null ? item.get(parentField) : null;
            Object value = valueField != null ? item.get(valueField) : 1;
            
            if (name != null) {
                String nameStr = String.valueOf(name);
                String parentStr = parent != null ? String.valueOf(parent) : null;
                
                Map<String, Object> node = new HashMap<>();
                node.put("name", nameStr);
                if (value != null) {
                    node.put("value", parseNumber(value));
                }
                nodeMap.put(nameStr, node);
                
                if (parentStr != null && !parentStr.isEmpty()) {
                    childrenMap.computeIfAbsent(parentStr, k -> new ArrayList<>()).add(nameStr);
                }
            }
        }
        
        // 构建树形结构
        List<Map<String, Object>> treeData = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : nodeMap.entrySet()) {
            String name = entry.getKey();
            Map<String, Object> node = entry.getValue();
            
            List<String> children = childrenMap.get(name);
            if (children != null && !children.isEmpty()) {
                List<Map<String, Object>> childNodes = new ArrayList<>();
                for (String childName : children) {
                    childNodes.add(nodeMap.get(childName));
                }
                node.put("children", childNodes);
            }
            
            // 只添加根节点
            if (!nodeMap.containsKey(parentField != null ? String.valueOf(data.get(0).get(parentField)) : null)) {
                treeData.add(node);
            }
        }
        
        result.put("data", treeData);
        
        return result;
    }
    
    private Map<String, Object> processMapChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        // 地图处理逻辑
        String regionField = (String) widget.get("regionField");
        String valueField = (String) widget.get("valueField");
        
        if (regionField == null || valueField == null) {
            return Map.of("success", false, "message", "地图需要配置region和value字段");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // 构建地图数据
        List<Map<String, Object>> mapData = new ArrayList<>();
        
        for (Map<String, Object> item : data) {
            Object region = item.get(regionField);
            Object value = item.get(valueField);
            
            if (region != null && value != null) {
                Map<String, Object> dataItem = new HashMap<>();
                dataItem.put("name", String.valueOf(region));
                dataItem.put("value", parseNumber(value));
                mapData.add(dataItem);
            }
        }
        
        Map<String, Object> series = new HashMap<>();
        series.put("type", "map");
        series.put("data", mapData);
        
        result.put("series", List.of(series));
        
        return result;
    }
    
    private Map<String, Object> processDashboardChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        // 仪表板处理逻辑 - 返回多个指标
        String[] metricFields = ((String) widget.get("metricFields")).split(",");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        List<Map<String, Object>> metrics = new ArrayList<>();
        
        for (String field : metricFields) {
            if (field != null && !field.trim().isEmpty()) {
                String fieldName = field.trim();
                Map<String, Object> metric = new HashMap<>();
                metric.put("name", fieldName);
                
                // 计算指标值
                double sum = 0;
                double avg = 0;
                double max = Double.NEGATIVE_INFINITY;
                double min = Double.POSITIVE_INFINITY;
                int count = 0;
                
                for (Map<String, Object> item : data) {
                    Object value = item.get(fieldName);
                    if (value != null) {
                        Double numValue = parseNumber(value);
                        if (numValue != null) {
                            sum += numValue;
                            max = Math.max(max, numValue);
                            min = Math.min(min, numValue);
                            count++;
                        }
                    }
                }
                
                if (count > 0) {
                    avg = sum / count;
                    metric.put("sum", sum);
                    metric.put("avg", avg);
                    metric.put("max", max);
                    metric.put("min", min);
                    metric.put("count", count);
                    metrics.add(metric);
                }
            }
        }
        
        result.put("metrics", metrics);
        
        return result;
    }
    
    private Map<String, Object> processCandlestickChart(List<Map<String, Object>> data, Map<String, Object> widget) {
        // K线图处理逻辑
        String dateField = (String) widget.get("dateField");
        String openField = (String) widget.get("openField");
        String closeField = (String) widget.get("closeField");
        String highField = (String) widget.get("highField");
        String lowField = (String) widget.get("lowField");
        
        if (dateField == null || openField == null || closeField == null || 
            highField == null || lowField == null) {
            return Map.of("success", false, "message", "K线图需要配置date、open、close、high、low字段");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // 构建K线数据
        List<String> xAxisData = new ArrayList<>();
        List<List<Object>> candlestickData = new ArrayList<>();
        
        for (Map<String, Object> item : data) {
            Object date = item.get(dateField);
            Object open = item.get(openField);
            Object close = item.get(closeField);
            Object high = item.get(highField);
            Object low = item.get(lowField);
            
            if (date != null && open != null && close != null && high != null && low != null) {
                String dateStr = String.valueOf(date);
                Double openNum = parseNumber(open);
                Double closeNum = parseNumber(close);
                Double highNum = parseNumber(high);
                Double lowNum = parseNumber(low);
                
                if (openNum != null && closeNum != null && highNum != null && lowNum != null) {
                    xAxisData.add(dateStr);
                    
                    List<Object> dataPoint = new ArrayList<>();
                    dataPoint.add(openNum);
                    dataPoint.add(closeNum);
                    dataPoint.add(lowNum);
                    dataPoint.add(highNum);
                    candlestickData.add(dataPoint);
                }
            }
        }
        
        Map<String, Object> series = new HashMap<>();
        series.put("type", "candlestick");
        series.put("data", candlestickData);
        
        result.put("xAxis", xAxisData);
        result.put("series", List.of(series));
        
        return result;
    }
    
    private Double parseNumber(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
} 