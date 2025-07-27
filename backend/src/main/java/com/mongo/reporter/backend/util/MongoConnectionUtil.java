package com.mongo.reporter.backend.util;

import com.mongo.reporter.backend.model.DataSource;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MongoConnectionUtil {
    
    /**
     * 创建MongoDB客户端连接
     * @param dataSource 数据源配置
     * @return MongoClient实例
     */
    public MongoClient createMongoClient(DataSource dataSource) {
        if (dataSource.isUseAuth() && dataSource.getUsername() != null && dataSource.getPassword() != null) {
            return createAuthenticatedClient(dataSource);
        } else {
            return MongoClients.create(dataSource.getUri());
        }
    }
    
    /**
     * 创建带认证的MongoDB客户端连接
     * @param dataSource 数据源配置
     * @return MongoClient实例
     */
    private MongoClient createAuthenticatedClient(DataSource dataSource) {
        try {
            // 解析URI获取主机和端口
            String uri = dataSource.getUri();
            final String host;
            final int port;
            
            if (uri.startsWith("mongodb://")) {
                String hostPort = uri.substring(10);
                if (hostPort.contains("/")) {
                    hostPort = hostPort.substring(0, hostPort.indexOf("/"));
                }
                if (hostPort.contains(":")) {
                    String[] parts = hostPort.split(":");
                    host = parts[0];
                    port = Integer.parseInt(parts[1]);
                } else {
                    host = hostPort;
                    port = 27017;
                }
            } else {
                host = "localhost";
                port = 27017;
            }
            
            // 创建认证凭据
            String authDatabase = dataSource.getAuthDatabase() != null ? dataSource.getAuthDatabase() : "admin";
            MongoCredential credential = MongoCredential.createCredential(
                dataSource.getUsername(),
                authDatabase,
                dataSource.getPassword().toCharArray()
            );
            
            // 创建客户端设置
            MongoClientSettings settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder -> 
                    builder.hosts(java.util.Arrays.asList(new ServerAddress(host, port))))
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(10000, TimeUnit.MILLISECONDS)
                           .readTimeout(30000, TimeUnit.MILLISECONDS))
                .applyToServerSettings(builder -> 
                    builder.heartbeatFrequency(10000, TimeUnit.MILLISECONDS))
                .build();
            
            return MongoClients.create(settings);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create authenticated MongoDB client", e);
        }
    }
    
    /**
     * 测试MongoDB连接
     * @param dataSource 数据源配置
     * @return 连接测试结果
     */
    public boolean testConnection(DataSource dataSource) {
        MongoClient mongoClient = null;
        try {
            mongoClient = createMongoClient(dataSource);
            // 尝试获取数据库列表来验证连接
            mongoClient.listDatabaseNames().first();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
    
    /**
     * 获取数据库实例
     * @param dataSource 数据源配置
     * @param databaseName 数据库名称
     * @return MongoDatabase实例
     */
    public MongoDatabase getDatabase(DataSource dataSource, String databaseName) {
        MongoClient mongoClient = createMongoClient(dataSource);
        return mongoClient.getDatabase(databaseName);
    }
    
    /**
     * 从URI中提取数据库名称
     * @param uri MongoDB连接URI
     * @return 数据库名称
     */
    public String extractDatabaseName(String uri) {
        if (uri == null || uri.trim().isEmpty()) {
            return null;
        }
        
        try {
            if (uri.startsWith("mongodb://")) {
                String path = uri.substring(10);
                if (path.contains("/")) {
                    String dbPart = path.substring(path.indexOf("/") + 1);
                    if (dbPart.contains("?")) {
                        dbPart = dbPart.substring(0, dbPart.indexOf("?"));
                    }
                    return dbPart;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
} 