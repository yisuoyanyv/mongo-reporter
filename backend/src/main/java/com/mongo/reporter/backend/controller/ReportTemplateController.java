package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.ReportTemplate;
import com.mongo.reporter.backend.repository.ReportTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "*")
public class ReportTemplateController {

    @Autowired
    private ReportTemplateRepository templateRepository;

    /**
     * 获取所有模板
     */
    @GetMapping
    public ResponseEntity<List<ReportTemplate>> getAllTemplates(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            String username = getUsernameFromToken(auth);
            List<ReportTemplate> templates;
            
            if (username != null) {
                // 返回用户自己的模板和公开模板
                templates = templateRepository.findByOwnerOrIsPublicTrue(username);
            } else {
                // 只返回公开模板
                templates = templateRepository.findByIsPublicTrue();
            }
            
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 根据ID获取模板
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReportTemplate> getTemplate(@PathVariable String id,
                                                   @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            Optional<ReportTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportTemplate template = templateOpt.get();
            String username = getUsernameFromToken(auth);
            
            // 检查权限
            if (template.isPublic() || (username != null && username.equals(template.getOwner()))) {
                return ResponseEntity.ok(template);
            }
            
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 创建新模板
     */
    @PostMapping
    public ResponseEntity<ReportTemplate> createTemplate(@RequestBody ReportTemplate template,
                                                       @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            String username = getUsernameFromToken(auth);
            if (username == null) {
                return ResponseEntity.status(401).build();
            }

            template.setOwner(username);
            template.setCreatedAt(new Date().toString());
            template.setUpdatedAt(new Date().toString());
            
            ReportTemplate savedTemplate = templateRepository.save(template);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReportTemplate> updateTemplate(@PathVariable String id,
                                                       @RequestBody ReportTemplate template,
                                                       @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            String username = getUsernameFromToken(auth);
            if (username == null) {
                return ResponseEntity.status(401).build();
            }

            Optional<ReportTemplate> existingTemplateOpt = templateRepository.findById(id);
            if (existingTemplateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportTemplate existingTemplate = existingTemplateOpt.get();
            if (!username.equals(existingTemplate.getOwner())) {
                return ResponseEntity.status(403).build();
            }

            template.setId(id);
            template.setOwner(username);
            template.setUpdatedAt(new Date().toString());
            
            ReportTemplate savedTemplate = templateRepository.save(template);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id,
                                             @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            String username = getUsernameFromToken(auth);
            if (username == null) {
                return ResponseEntity.status(401).build();
            }

            Optional<ReportTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportTemplate template = templateOpt.get();
            if (!username.equals(template.getOwner())) {
                return ResponseEntity.status(403).build();
            }

            templateRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 搜索模板
     */
    @GetMapping("/search")
    public ResponseEntity<List<ReportTemplate>> searchTemplates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            String username = getUsernameFromToken(auth);
            if (keyword == null) keyword = "";
            if (tags == null) tags = new ArrayList<>();
            
            List<ReportTemplate> templates = templateRepository.searchTemplatesByKeywordAndFilters(
                keyword, category, tags, username);
            
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取模板分类
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getTemplateCategories(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            List<ReportTemplate> templates = templateRepository.findAllCategories();
            Set<String> categories = new HashSet<>();
            
            for (ReportTemplate template : templates) {
                if (template.getCategory() != null && !template.getCategory().trim().isEmpty()) {
                    // 检查权限
                    if (template.isPublic() || 
                        (auth != null && getUsernameFromToken(auth) != null && 
                         getUsernameFromToken(auth).equals(template.getOwner()))) {
                        categories.add(template.getCategory().trim());
                    }
                }
            }
            
            return ResponseEntity.ok(new ArrayList<>(categories));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取模板标签
     */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getTemplateTags(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            List<ReportTemplate> templates = templateRepository.findAllTags();
            Set<String> tags = new HashSet<>();
            
            for (ReportTemplate template : templates) {
                if (template.getTags() != null) {
                    // 检查权限
                    if (template.isPublic() || 
                        (auth != null && getUsernameFromToken(auth) != null && 
                         getUsernameFromToken(auth).equals(template.getOwner()))) {
                        tags.addAll(template.getTags());
                    }
                }
            }
            
            return ResponseEntity.ok(new ArrayList<>(tags));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取热门模板
     */
    @GetMapping("/popular")
    public ResponseEntity<List<ReportTemplate>> getPopularTemplates() {
        try {
            List<ReportTemplate> templates = templateRepository.findByIsPublicTrueOrderByUsageCountDesc();
            return ResponseEntity.ok(templates.stream().limit(10).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取高评分模板
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<ReportTemplate>> getTopRatedTemplates() {
        try {
            List<ReportTemplate> templates = templateRepository.findByIsPublicTrueOrderByRatingDesc();
            return ResponseEntity.ok(templates.stream().limit(10).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 获取最新模板
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ReportTemplate>> getLatestTemplates() {
        try {
            List<ReportTemplate> templates = templateRepository.findByIsPublicTrueOrderByCreatedAtDesc();
            return ResponseEntity.ok(templates.stream().limit(10).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 使用模板创建报表
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<Map<String, Object>> useTemplate(@PathVariable String id,
                                                          @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            String username = getUsernameFromToken(auth);
            if (username == null) {
                return ResponseEntity.status(401).build();
            }

            Optional<ReportTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportTemplate template = templateOpt.get();
            if (!template.isPublic() && !username.equals(template.getOwner())) {
                return ResponseEntity.status(403).build();
            }

            // 增加使用次数
            template.setUsageCount(template.getUsageCount() + 1);
            templateRepository.save(template);

            // 返回模板配置
            Map<String, Object> result = new HashMap<>();
            result.put("template", template);
            result.put("message", "模板使用成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 评分模板
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<Map<String, Object>> rateTemplate(@PathVariable String id,
                                                           @RequestBody Map<String, Object> ratingData,
                                                           @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            String username = getUsernameFromToken(auth);
            if (username == null) {
                return ResponseEntity.status(401).build();
            }

            Optional<ReportTemplate> templateOpt = templateRepository.findById(id);
            if (templateOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            ReportTemplate template = templateOpt.get();
            if (!template.isPublic()) {
                return ResponseEntity.status(403).build();
            }

            Double rating = (Double) ratingData.get("rating");
            if (rating == null || rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().build();
            }

            // 更新评分
            if (template.getRatings() == null) {
                template.setRatings(new ArrayList<>());
            }
            template.getRatings().add(username + ":" + rating);
            
            // 计算平均评分
            double avgRating = template.getRatings().stream()
                .mapToDouble(r -> Double.parseDouble(r.split(":")[1]))
                .average()
                .orElse(0.0);
            template.setRating(avgRating);
            
            templateRepository.save(template);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "评分成功");
            result.put("rating", avgRating);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // 私有方法：从JWT token中提取用户名
    private String getUsernameFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        
        // 这里应该实现JWT token解析逻辑
        // 暂时返回一个默认用户名用于测试
        return "admin";
    }
} 