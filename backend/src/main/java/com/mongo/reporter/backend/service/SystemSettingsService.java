package com.mongo.reporter.backend.service;

import com.mongo.reporter.backend.model.SystemSettings;
import com.mongo.reporter.backend.repository.SystemSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SystemSettingsService {
    
    @Autowired
    private SystemSettingsRepository systemSettingsRepository;
    
    /**
     * 获取所有系统设置
     */
    public Map<String, Object> getAllSettings() {
        Map<String, Object> allSettings = new HashMap<>();
        
        // 获取所有分类的设置
        List<SystemSettings> settingsList = systemSettingsRepository.findAll();
        
        for (SystemSettings settings : settingsList) {
            allSettings.put(settings.getCategory(), settings.getSettings());
        }
        
        // 如果没有设置，返回默认设置
        if (allSettings.isEmpty()) {
            allSettings = getDefaultSettings();
        }
        
        return allSettings;
    }
    
    /**
     * 获取特定分类的设置
     */
    public Map<String, Object> getSettingsByCategory(String category) {
        Optional<SystemSettings> settings = systemSettingsRepository.findByCategory(category);
        return settings.map(SystemSettings::getSettings).orElse(getDefaultSettingsForCategory(category));
    }
    
    /**
     * 保存设置
     */
    public void saveSettings(String category, Map<String, Object> settings) {
        SystemSettings systemSettings = systemSettingsRepository.findByCategory(category)
                .orElse(new SystemSettings(category, settings));
        
        systemSettings.setSettings(settings);
        systemSettings.setUpdatedAt(new Date());
        systemSettings.setUpdatedBy("admin"); // TODO: 从当前用户获取
        
        systemSettingsRepository.save(systemSettings);
    }
    
    /**
     * 保存所有设置
     */
    public void saveAllSettings(Map<String, Object> allSettings) {
        for (Map.Entry<String, Object> entry : allSettings.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> settings = (Map<String, Object>) entry.getValue();
                saveSettings(entry.getKey(), settings);
            }
        }
    }
    
    /**
     * 重置设置到默认值
     */
    public void resetSettings() {
        systemSettingsRepository.deleteAll();
        Map<String, Object> defaultSettings = getDefaultSettings();
        saveAllSettings(defaultSettings);
    }
    
    /**
     * 获取默认设置
     */
    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        
        defaultSettings.put("basic", getDefaultSettingsForCategory("basic"));
        defaultSettings.put("theme", getDefaultSettingsForCategory("theme"));
        defaultSettings.put("notification", getDefaultSettingsForCategory("notification"));
        defaultSettings.put("security", getDefaultSettingsForCategory("security"));
        defaultSettings.put("performance", getDefaultSettingsForCategory("performance"));
        defaultSettings.put("backup", getDefaultSettingsForCategory("backup"));
        
        return defaultSettings;
    }
    
    /**
     * 获取特定分类的默认设置
     */
    private Map<String, Object> getDefaultSettingsForCategory(String category) {
        Map<String, Object> settings = new HashMap<>();
        
        switch (category) {
            case "basic":
                settings.put("systemName", "MongoReporter");
                settings.put("systemDescription", "基于MongoDB的数据报表平台");
                settings.put("defaultLanguage", "zh-CN");
                settings.put("timezone", "Asia/Shanghai");
                settings.put("dataRetentionDays", 365);
                break;
                
            case "theme":
                settings.put("mode", "light");
                settings.put("primaryColor", "#409EFF");
                settings.put("chartTheme", "default");
                settings.put("fontSize", 14);
                settings.put("borderRadius", 8);
                settings.put("preset", "default");
                break;
                
            case "notification":
                settings.put("emailEnabled", false);
                settings.put("smtpServer", "");
                settings.put("smtpPort", 587);
                settings.put("emailAccount", "");
                settings.put("emailPassword", "");
                settings.put("systemNotification", true);
                settings.put("reportCompletionNotification", true);
                settings.put("datasourceErrorNotification", true);
                break;
                
            case "security":
                settings.put("minPasswordLength", 8);
                settings.put("passwordComplexity", Arrays.asList("uppercase", "lowercase", "numbers"));
                settings.put("sessionTimeout", 30);
                settings.put("loginLockEnabled", true);
                settings.put("loginLockThreshold", 5);
                settings.put("loginLockDuration", 15);
                break;
                
            case "performance":
                settings.put("dataCacheEnabled", true);
                settings.put("cacheDuration", 30);
                settings.put("queryTimeout", 30);
                settings.put("maxQueryResults", 1000);
                settings.put("maxConcurrentQueries", 10);
                break;
                
            case "backup":
                settings.put("autoBackupEnabled", false);
                settings.put("backupFrequency", "daily");
                settings.put("backupTime", "02:00");
                settings.put("backupRetentionCount", 30);
                settings.put("backupPath", "/backup");
                break;
                
            default:
                break;
        }
        
        return settings;
    }
} 