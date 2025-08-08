package com.mongo.reporter.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

@Service
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * 发送全局通知
     */
    public void sendGlobalNotification(String title, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", "notification_" + System.currentTimeMillis());
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("time", new Date());
        notification.put("read", false);
        
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
    
    /**
     * 发送用户特定通知
     */
    public void sendUserNotification(String username, String title, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", "notification_" + System.currentTimeMillis());
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("time", new Date());
        notification.put("read", false);
        
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
    }
    
    /**
     * 发送系统状态通知
     */
    public void sendSystemStatusNotification(String status, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("id", "system_" + System.currentTimeMillis());
        notification.put("title", "系统状态更新");
        notification.put("message", message);
        notification.put("type", status.equals("UP") ? "success" : "warning");
        notification.put("time", new Date());
        notification.put("read", false);
        
        messagingTemplate.convertAndSend("/topic/system-status", notification);
    }
    
    /**
     * 发送报表相关通知
     */
    public void sendReportNotification(String username, String reportName, String action) {
        String title = "报表操作通知";
        String message = String.format("报表 '%s' 已%s", reportName, action);
        String type = "info";
        
        if (username != null && !username.isEmpty()) {
            sendUserNotification(username, title, message, type);
        } else {
            sendGlobalNotification(title, message, type);
        }
    }
} 