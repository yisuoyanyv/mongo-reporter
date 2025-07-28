package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.DataSource;
import com.mongo.reporter.backend.repository.DataSourceRepository;
import com.mongo.reporter.backend.util.MongoConnectionUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.crypto.SecretKey;
import java.util.*;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoException;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {
    
    @Autowired
    private DataSourceRepository dataSourceRepository;
    
    @Autowired
    private MongoConnectionUtil mongoConnectionUtil;
    
    private final String jwtSecret = "mongo-reporter-secret-key-256-bits-long";
    private final SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    @GetMapping
    public List<DataSource> list() {
        // 返回所有数据源，包括默认的和用户创建的
        return dataSourceRepository.findAll();
    }

    @GetMapping("/user")
    public List<DataSource> listByUser(@RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            return dataSourceRepository.findByOwnerOrIsDefaultTrue(username);
        }
        return dataSourceRepository.findByIsDefaultTrue();
    }

    @GetMapping("/{id}")
    public DataSource get(@PathVariable String id) {
        return dataSourceRepository.findById(id).orElse(null);
    }

    @PostMapping
    public DataSource add(@RequestBody DataSource dataSource, @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        if (username != null) {
            dataSource.setOwner(username);
        }
        return dataSourceRepository.save(dataSource);
    }

    @PostMapping("/test")
    public Map<String, Object> testConnection(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否有完整的数据源信息
        if (request.containsKey("dataSource")) {
            // 使用完整的数据源对象进行测试
            Map<String, Object> dataSourceMap = (Map<String, Object>) request.get("dataSource");
            DataSource dataSource = new DataSource();
            dataSource.setUri((String) dataSourceMap.get("uri"));
            dataSource.setUsername((String) dataSourceMap.get("username"));
            dataSource.setPassword((String) dataSourceMap.get("password"));
            dataSource.setAuthDatabase((String) dataSourceMap.get("authDatabase"));
            dataSource.setUseAuth(Boolean.TRUE.equals(dataSourceMap.get("useAuth")));
            
            boolean success = mongoConnectionUtil.testConnection(dataSource);
            result.put("success", success);
            result.put("message", success ? "连接成功" : "连接失败");
            
            // 如果有数据源ID，更新数据库中的连接状态
            String dataSourceId = (String) dataSourceMap.get("id");
            if (dataSourceId != null && !dataSourceId.trim().isEmpty()) {
                try {
                    DataSource existingDataSource = dataSourceRepository.findById(dataSourceId).orElse(null);
                    if (existingDataSource != null) {
                        existingDataSource.setConnectionStatus(success ? "success" : "error");
                        existingDataSource.setLastTestTime(new Date());
                        dataSourceRepository.save(existingDataSource);
                    }
                } catch (Exception e) {
                    // 记录错误但不影响测试结果
                    System.err.println("保存连接状态失败: " + e.getMessage());
                }
            }
        } else {
            // 兼容旧的测试方式
            String uri = (String) request.get("uri");
            if (uri == null || uri.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "连接字符串不能为空");
                return result;
            }
            
            DataSource dataSource = new DataSource();
            dataSource.setUri(uri);
            dataSource.setUseAuth(false);
            
            boolean success = mongoConnectionUtil.testConnection(dataSource);
            result.put("success", success);
            result.put("message", success ? "连接成功" : "连接失败");
        }
        
        return result;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        DataSource dataSource = dataSourceRepository.findById(id).orElse(null);
        if (dataSource != null && (dataSource.isDefault() || username.equals(dataSource.getOwner()))) {
            dataSourceRepository.deleteById(id);
        }
    }

    @PutMapping("/{id}")
    public DataSource update(@PathVariable String id, @RequestBody DataSource dataSource, @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        DataSource existing = dataSourceRepository.findById(id).orElse(null);
        if (existing != null && (existing.isDefault() || username.equals(existing.getOwner()))) {
            dataSource.setId(id);
            if (username != null) {
                dataSource.setOwner(username);
            }
            return dataSourceRepository.save(dataSource);
        }
        throw new IllegalArgumentException("Invalid data source or permission denied");
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
} 