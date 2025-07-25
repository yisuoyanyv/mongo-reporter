package com.mongo.reporter.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/datasource")
public class DataSourceController {
    // 内存模拟数据源配置，后续可替换为数据库持久化
    private final List<Map<String, String>> dataSources = new ArrayList<>();

    @GetMapping
    public List<Map<String, String>> list() {
        return dataSources;
    }

    @GetMapping("/{index}")
    public Map<String, String> get(@PathVariable int index) {
        if (index >= 0 && index < dataSources.size()) {
            return dataSources.get(index);
        }
        return null;
    }

    @PostMapping
    public Map<String, String> add(@RequestBody Map<String, String> config) {
        dataSources.add(config);
        return config;
    }

    @DeleteMapping("/{index}")
    public void delete(@PathVariable int index) {
        if (index >= 0 && index < dataSources.size()) {
            dataSources.remove(index);
        }
    }

    @PutMapping("/{index}")
    public Map<String, String> update(@PathVariable int index, @RequestBody Map<String, String> config) {
        if (index >= 0 && index < dataSources.size()) {
            dataSources.set(index, config);
            return config;
        }
        throw new IllegalArgumentException("Invalid index");
    }
} 