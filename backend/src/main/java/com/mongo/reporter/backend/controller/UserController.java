package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.User;
import com.mongo.reporter.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Date;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 获取所有用户
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 创建新用户
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        
        // 设置默认值
        if (user.getRole() == null) {
            user.setRole("user");
        }
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User existing = existingUser.get();
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setRole(user.getRole());
            existing.setStatus(user.getStatus());
            existing.setFullName(user.getFullName());
            existing.setDepartment(user.getDepartment());
            existing.setPhone(user.getPhone());
            
            User updatedUser = userRepository.save(existing);
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 切换用户状态
    @PutMapping("/{id}/status")
    public ResponseEntity<User> toggleUserStatus(@PathVariable String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            String newStatus = "active".equals(existingUser.getStatus()) ? "inactive" : "active";
            existingUser.setStatus(newStatus);
            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    // 根据角色获取用户
    @GetMapping("/role/{role}")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable String role) {
        List<User> users = userRepository.findByRole(role);
        return ResponseEntity.ok(users);
    }

    // 根据状态获取用户
    @GetMapping("/status/{status}")
    public ResponseEntity<List<User>> getUsersByStatus(@PathVariable String status) {
        List<User> users = userRepository.findByStatus(status);
        return ResponseEntity.ok(users);
    }
    
    // 获取用户权限信息
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Object>> getUserPermissions(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> permissions = new HashMap<>();
            
            // 根据角色设置权限
            switch (user.getRole()) {
                case "admin":
                    permissions.put("reports", Arrays.asList("create", "read", "update", "delete", "export", "share", "duplicate"));
                    permissions.put("datasources", Arrays.asList("create", "read", "update", "delete", "test"));
                    permissions.put("users", Arrays.asList("create", "read", "update", "delete"));
                    permissions.put("system", Arrays.asList("settings", "monitor", "backup"));
                    permissions.put("templates", Arrays.asList("create", "read", "update", "delete", "share"));
                    break;
                case "manager":
                    permissions.put("reports", Arrays.asList("create", "read", "update", "export", "share", "duplicate"));
                    permissions.put("datasources", Arrays.asList("read", "test"));
                    permissions.put("users", Arrays.asList("read"));
                    permissions.put("system", Arrays.asList("monitor"));
                    permissions.put("templates", Arrays.asList("read", "use"));
                    break;
                case "user":
                    permissions.put("reports", Arrays.asList("create", "read", "update", "export", "share"));
                    permissions.put("datasources", Arrays.asList("read"));
                    permissions.put("users", Arrays.asList("read"));
                    permissions.put("system", Arrays.asList());
                    permissions.put("templates", Arrays.asList("read", "use"));
                    break;
                case "viewer":
                    permissions.put("reports", Arrays.asList("read", "export"));
                    permissions.put("datasources", Arrays.asList("read"));
                    permissions.put("users", Arrays.asList());
                    permissions.put("system", Arrays.asList());
                    permissions.put("templates", Arrays.asList("read"));
                    break;
                default:
                    permissions.put("reports", Arrays.asList("read"));
                    permissions.put("datasources", Arrays.asList("read"));
                    permissions.put("users", Arrays.asList());
                    permissions.put("system", Arrays.asList());
                    permissions.put("templates", Arrays.asList("read"));
            }
            
            permissions.put("role", user.getRole());
            permissions.put("userId", user.getId());
            permissions.put("username", user.getUsername());
            
            return ResponseEntity.ok(permissions);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 检查用户权限
    @PostMapping("/{id}/check-permission")
    public ResponseEntity<Map<String, Object>> checkUserPermission(
            @PathVariable String id,
            @RequestBody Map<String, Object> permissionRequest) {
        
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        String resource = (String) permissionRequest.get("resource");
        String action = (String) permissionRequest.get("action");
        
        if (resource == null || action == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // 获取用户权限
        ResponseEntity<Map<String, Object>> permissionsResponse = getUserPermissions(id);
        if (permissionsResponse.getStatusCode().is2xxSuccessful()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> permissions = permissionsResponse.getBody();
            @SuppressWarnings("unchecked")
            List<String> resourcePermissions = (List<String>) permissions.get(resource);
            
            boolean hasPermission = resourcePermissions != null && resourcePermissions.contains(action);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hasPermission", hasPermission);
            result.put("userId", id);
            result.put("resource", resource);
            result.put("action", action);
            result.put("role", user.getRole());
            
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(500).build();
        }
    }
    
    // 批量更新用户权限
    @PutMapping("/{id}/permissions")
    public ResponseEntity<Map<String, Object>> updateUserPermissions(
            @PathVariable String id,
            @RequestBody Map<String, Object> permissionsUpdate) {
        
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        String newRole = (String) permissionsUpdate.get("role");
        
        if (newRole != null && !newRole.isEmpty()) {
            // 验证角色是否有效
            List<String> validRoles = Arrays.asList("admin", "manager", "user", "viewer");
            if (validRoles.contains(newRole)) {
                user.setRole(newRole);
                user.setUpdatedAt(new Date());
                userRepository.save(user);
                
                Map<String, Object> result = new HashMap<>();
                result.put("message", "用户权限更新成功");
                result.put("userId", id);
                result.put("newRole", newRole);
                
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "无效的角色"));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "角色不能为空"));
        }
    }
} 