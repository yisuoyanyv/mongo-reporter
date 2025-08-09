package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.service.SystemSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
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
            Map.entry("version", "1.4.6"),
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

    // 测试邮件配置（连通性）
    @PostMapping("/test/email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestBody Map<String, Object> body) {
        String host = String.valueOf(body.get("smtpServer"));
        int port;
        try {
            port = Integer.parseInt(String.valueOf(body.get("smtpPort")));
        } catch (Exception e) {
            port = 25;
        }
        Map<String, Object> result = new HashMap<>();
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            long latency = System.currentTimeMillis() - start;
            result.put("success", true);
            result.put("message", "SMTP连通性正常");
            result.put("latencyMs", latency);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", "SMTP连接失败: " + ex.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    // 测试备份路径（可写性）
    @PostMapping("/test/backup")
    public ResponseEntity<Map<String, Object>> testBackup(@RequestBody Map<String, Object> body) {
        String path = String.valueOf(body.get("backupPath"));
        Map<String, Object> result = new HashMap<>();
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    result.put("success", false);
                    result.put("message", "目录不存在且创建失败");
                    return ResponseEntity.ok(result);
                }
            }
            File probe = new File(dir, ".write_probe_" + System.currentTimeMillis());
            try (FileOutputStream fos = new FileOutputStream(probe)) {
                fos.write("ok".getBytes());
                fos.flush();
            }
            boolean deleted = probe.delete();
            result.put("success", true);
            result.put("message", deleted ? "路径可写" : "路径可写（临时文件删除失败，可忽略）");
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", "路径不可写: " + ex.getMessage());
            return ResponseEntity.ok(result);
        }
    }
} 