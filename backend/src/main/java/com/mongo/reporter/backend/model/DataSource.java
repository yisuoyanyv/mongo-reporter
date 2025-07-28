package com.mongo.reporter.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "data_sources")
public class DataSource {
    @Id
    private String id;
    private String name;
    private String uri;
    private String username;
    private String password;
    private String authDatabase;
    private String owner;
    private boolean isDefault;
    private boolean useAuth;
    private String connectionStatus; // success, error, unknown
    private Date lastTestTime;

    public DataSource() {}

    public DataSource(String name, String uri, String owner, boolean isDefault) {
        this.name = name;
        this.uri = uri;
        this.owner = owner;
        this.isDefault = isDefault;
        this.useAuth = false;
    }

    public DataSource(String name, String uri, String username, String password, String authDatabase, String owner, boolean isDefault) {
        this.name = name;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.authDatabase = authDatabase;
        this.owner = owner;
        this.isDefault = isDefault;
        this.useAuth = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthDatabase() {
        return authDatabase;
    }

    public void setAuthDatabase(String authDatabase) {
        this.authDatabase = authDatabase;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isUseAuth() {
        return useAuth;
    }

    public void setUseAuth(boolean useAuth) {
        this.useAuth = useAuth;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public Date getLastTestTime() {
        return lastTestTime;
    }

    public void setLastTestTime(Date lastTestTime) {
        this.lastTestTime = lastTestTime;
    }

    // 构建认证URI的方法
    public String buildAuthenticatedUri() {
        if (!useAuth || username == null || password == null) {
            return uri;
        }
        
        // 解析原始URI
        String baseUri = uri;
        if (baseUri.contains("@")) {
            // 如果URI已经包含认证信息，先移除
            int atIndex = baseUri.indexOf("@");
            int slashIndex = baseUri.indexOf("//");
            if (slashIndex >= 0) {
                baseUri = baseUri.substring(0, slashIndex + 2) + baseUri.substring(atIndex + 1);
            }
        }
        
        // 添加认证信息
        String authDb = authDatabase != null ? authDatabase : "admin";
        return baseUri.replace("://", "://" + username + ":" + password + "@") + "?authSource=" + authDb;
    }
} 