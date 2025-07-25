package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.ReportConfig;
import com.mongo.reporter.backend.repository.ReportConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.web.bind.annotation.RequestHeader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ReportConfigRepository reportConfigRepository;

    private final List<ReportConfig> reports = new ArrayList<>();
    private final String jwtSecret = "mongo-reporter-secret";

    @PostMapping("/save")
    public ReportConfig saveReport(@RequestBody ReportConfig config, @RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        config.setOwner(username);
        return reportConfigRepository.save(config);
    }

    @GetMapping("/list")
    public List<ReportConfig> listReports(@RequestHeader("Authorization") String auth) {
        String username = getUsernameFromToken(auth);
        return reportConfigRepository.findAll().stream()
            .filter(r -> username.equals(r.getOwner()) || r.isPublicShare())
            .toList();
    }

    @DeleteMapping("/delete/{id}")
    public void deleteReport(@PathVariable String id) {
        reportConfigRepository.deleteById(id);
    }

    @GetMapping("/collections")
    public List<String> listCollections() {
        return mongoTemplate.getCollectionNames().stream().toList();
    }

    @GetMapping("/data")
    public List<Map<String, Object>> getData(@RequestParam String collection) {
        return mongoTemplate.findAll(Map.class, collection);
    }

    @GetMapping("/get/{id}")
    public ReportConfig getReport(@PathVariable String id) {
        return reportConfigRepository.findById(id).orElse(null);
    }

    @PostMapping("/collections")
    public List<String> listCollectionsByUri(@RequestBody Map<String, String> body) {
        String uri = body.get("uri");
        MongoTemplate temp = new MongoTemplate(new com.mongodb.client.MongoClient() {
            @Override
            public void close() {}
            @Override
            public com.mongodb.client.MongoDatabase getDatabase(String databaseName) {
                return com.mongodb.client.MongoClients.create(uri).getDatabase(databaseName);
            }
            @Override
            public com.mongodb.MongoClientSettings getSettings() { return null; }
            @Override
            public com.mongodb.client.MongoClient startSession() { return null; }
            @Override
            public com.mongodb.client.MongoClient startSession(com.mongodb.ClientSessionOptions options) { return null; }
        }, "temp");
        return temp.getCollectionNames().stream().toList();
    }

    @PostMapping("/fields")
    public Set<String> listFieldsByUriAndCollection(@RequestBody Map<String, String> body) {
        String uri = body.get("uri");
        String collection = body.get("collection");
        MongoTemplate temp = new MongoTemplate(com.mongodb.client.MongoClients.create(uri), "temp");
        List<Map> docs = temp.findAll(Map.class, collection);
        Set<String> fields = new HashSet<>();
        for (Map doc : docs) fields.addAll(doc.keySet());
        return fields;
    }

    private String getUsernameFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        String token = auth.substring(7);
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
} 