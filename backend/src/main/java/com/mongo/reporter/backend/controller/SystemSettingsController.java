package com.mongo.reporter.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SystemSettingsController {

    // 模拟系统设置存储
    private static final Map<String, Object> systemSettings = new HashMap<>();

    static {
        // 基本设置
        systemSettings.put("appName", "MongoReporter");
        systemSettings.put("appVersion", "1.0.0");
        systemSettings.put("language", "zh-CN");
        systemSettings.put("timezone", "Asia/Shanghai");
        
        // 主题设置
        systemSettings.put("theme", "light");
        systemSettings.put("primaryColor", "#409EFF");
        systemSettings.put("chartTheme", "light");
        
        // 通知设置
        systemSettings.put("emailNotifications", true);
        systemSettings.put("systemNotifications", true);
        systemSettings.put("notificationSound", true);
        
        // 安全设置
        systemSettings.put("sessionTimeout", 30);
        systemSettings.put("maxLoginAttempts", 5);
        systemSettings.put("passwordMinLength", 8);
        systemSettings.put("requireSpecialChars", true);
        
        // 性能设置
        systemSettings.put("dataCacheEnabled", true);
        systemSettings.put("cacheTimeout", 300);
        systemSettings.put("maxConcurrentUsers", 100);
        systemSettings.put("queryTimeout", 30);
        
        // 备份设置
        systemSettings.put("autoBackupEnabled", true);
        systemSettings.put("backupFrequency", "daily");
        systemSettings.put("backupRetention", 30);
        systemSettings.put("backupLocation", "/backups");
    }

    // 获取所有系统设置
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSettings() {
        return ResponseEntity.ok(systemSettings);
    }

    // 获取特定设置
    @GetMapping("/{key}")
    public ResponseEntity<Object> getSetting(@PathVariable String key) {
        if (systemSettings.containsKey(key)) {
            return ResponseEntity.ok(systemSettings.get(key));
        }
        return ResponseEntity.notFound().build();
    }

    // 更新设置
    @PostMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> settings) {
        systemSettings.putAll(settings);
        return ResponseEntity.ok(systemSettings);
    }

    // 更新特定设置
    @PutMapping("/{key}")
    public ResponseEntity<Object> updateSetting(@PathVariable String key, @RequestBody Object value) {
        systemSettings.put(key, value);
        return ResponseEntity.ok(value);
    }

    // 重置设置到默认值
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSettings() {
        systemSettings.clear();
        // 重新初始化默认设置
        systemSettings.put("appName", "MongoReporter");
        systemSettings.put("appVersion", "1.0.0");
        systemSettings.put("language", "zh-CN");
        systemSettings.put("timezone", "Asia/Shanghai");
        systemSettings.put("theme", "light");
        systemSettings.put("primaryColor", "#409EFF");
        systemSettings.put("chartTheme", "light");
        systemSettings.put("emailNotifications", true);
        systemSettings.put("systemNotifications", true);
        systemSettings.put("notificationSound", true);
        systemSettings.put("sessionTimeout", 30);
        systemSettings.put("maxLoginAttempts", 5);
        systemSettings.put("passwordMinLength", 8);
        systemSettings.put("requireSpecialChars", true);
        systemSettings.put("dataCacheEnabled", true);
        systemSettings.put("cacheTimeout", 300);
        systemSettings.put("maxConcurrentUsers", 100);
        systemSettings.put("queryTimeout", 30);
        systemSettings.put("autoBackupEnabled", true);
        systemSettings.put("backupFrequency", "daily");
        systemSettings.put("backupRetention", 30);
        systemSettings.put("backupLocation", "/backups");
        
        return ResponseEntity.ok(systemSettings);
    }

    // 获取系统信息
    @GetMapping("/system/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("totalMemory", Runtime.getRuntime().totalMemory());
        systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
        systemInfo.put("maxMemory", Runtime.getRuntime().maxMemory());
        systemInfo.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        return ResponseEntity.ok(systemInfo);
    }
} 