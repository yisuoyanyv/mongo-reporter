package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.service.SystemSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SystemSettingsController {

    @Autowired
    private SystemSettingsService systemSettingsService;

    // 获取所有系统设置
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSettings() {
        Map<String, Object> settings = systemSettingsService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    // 获取特定分类的设置
    @GetMapping("/{category}")
    public ResponseEntity<Map<String, Object>> getSettingsByCategory(@PathVariable String category) {
        Map<String, Object> settings = systemSettingsService.getSettingsByCategory(category);
        return ResponseEntity.ok(settings);
    }

    // 更新所有设置
    @PostMapping
    public ResponseEntity<Map<String, Object>> updateAllSettings(@RequestBody Map<String, Object> settings) {
        systemSettingsService.saveAllSettings(settings);
        Map<String, Object> updatedSettings = systemSettingsService.getAllSettings();
        return ResponseEntity.ok(updatedSettings);
    }

    // 更新特定分类的设置
    @PutMapping("/{category}")
    public ResponseEntity<Map<String, Object>> updateSettingsByCategory(
            @PathVariable String category, 
            @RequestBody Map<String, Object> settings) {
        systemSettingsService.saveSettings(category, settings);
        Map<String, Object> updatedSettings = systemSettingsService.getSettingsByCategory(category);
        return ResponseEntity.ok(updatedSettings);
    }

    // 重置设置到默认值
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSettings() {
        systemSettingsService.resetSettings();
        Map<String, Object> defaultSettings = systemSettingsService.getAllSettings();
        return ResponseEntity.ok(defaultSettings);
    }

    // 获取系统信息
    @GetMapping("/system/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = Map.ofEntries(
            Map.entry("appName", "MongoReporter"),
            Map.entry("version", "1.4.2"),
            Map.entry("javaVersion", System.getProperty("java.version")),
            Map.entry("osName", System.getProperty("os.name")),
            Map.entry("osVersion", System.getProperty("os.version")),
            Map.entry("userHome", System.getProperty("user.home")),
            Map.entry("userDir", System.getProperty("user.dir")),
            Map.entry("javaHome", System.getProperty("java.home")),
            Map.entry("totalMemory", Runtime.getRuntime().totalMemory()),
            Map.entry("freeMemory", Runtime.getRuntime().freeMemory()),
            Map.entry("maxMemory", Runtime.getRuntime().maxMemory()),
            Map.entry("availableProcessors", Runtime.getRuntime().availableProcessors())
        );
        return ResponseEntity.ok(systemInfo);
    }
} 