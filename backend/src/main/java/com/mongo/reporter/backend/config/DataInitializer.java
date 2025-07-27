package com.mongo.reporter.backend.config;

import com.mongo.reporter.backend.model.DataSource;
import com.mongo.reporter.backend.model.ReportConfig;
import com.mongo.reporter.backend.model.User;
import com.mongo.reporter.backend.repository.DataSourceRepository;
import com.mongo.reporter.backend.repository.ReportConfigRepository;
import com.mongo.reporter.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportConfigRepository reportConfigRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        // 初始化默认用户
        initializeUsers();
        
        // 初始化默认数据源
        initializeDataSources();
        
        // 初始化示例报表配置
        initializeSampleReports();
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);

            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(encoder.encode("test123"));
            testUser.setRole("USER");
            userRepository.save(testUser);

            System.out.println("✅ 默认用户已创建: admin/admin123, testuser/test123");
        }
    }

    private void initializeDataSources() {
        if (dataSourceRepository.count() == 0) {
            List<DataSource> defaultDataSources = Arrays.asList(
                new DataSource("销售数据", "mongodb://localhost:27017/mongo-reporter", "admin", true),
                new DataSource("产品数据", "mongodb://localhost:27017/mongo-reporter", "admin", true),
                new DataSource("用户数据", "mongodb://localhost:27017/mongo-reporter", "admin", true),
                new DataSource("订单数据", "mongodb://localhost:27017/mongo-reporter", "admin", true)
            );

            dataSourceRepository.saveAll(defaultDataSources);
            System.out.println("✅ 默认数据源已创建: " + defaultDataSources.size() + " 个");
        }
    }

    private void initializeSampleReports() {
        if (reportConfigRepository.count() == 0) {
            // 销售趋势图
            ReportConfig salesReport = new ReportConfig(
                "销售趋势图", 
                "按月份和地区展示销售数据趋势", 
                "admin", 
                "mongodb://localhost:27017/mongo-reporter", 
                "sales"
            );
            
            Map<String, Object> salesWidget = new HashMap<>();
            salesWidget.put("name", "line");
            salesWidget.put("title", "销售趋势");
            salesWidget.put("xField", "month");
            salesWidget.put("yField", "amount");
            salesWidget.put("seriesField", "region");
            
            salesReport.setWidgets(Arrays.asList(salesWidget));
            salesReport.setPublicShare(true);
            salesReport.setCreatedAt(new Date().toString());
            salesReport.setUpdatedAt(new Date().toString());
            
            // 产品分类统计
            ReportConfig productReport = new ReportConfig(
                "产品分类统计", 
                "按产品分类统计库存和价格", 
                "admin", 
                "mongodb://localhost:27017/mongo-reporter", 
                "products"
            );
            
            Map<String, Object> productWidget = new HashMap<>();
            productWidget.put("name", "pie");
            productWidget.put("title", "产品分类分布");
            productWidget.put("nameField", "category");
            productWidget.put("valueField", "stock");
            
            productReport.setWidgets(Arrays.asList(productWidget));
            productReport.setPublicShare(true);
            productReport.setCreatedAt(new Date().toString());
            productReport.setUpdatedAt(new Date().toString());
            
            // 订单金额统计
            ReportConfig orderReport = new ReportConfig(
                "订单金额统计", 
                "按客户统计订单金额", 
                "admin", 
                "mongodb://localhost:27017/mongo-reporter", 
                "orders"
            );
            
            Map<String, Object> orderWidget = new HashMap<>();
            orderWidget.put("name", "bar");
            orderWidget.put("title", "客户订单金额");
            orderWidget.put("xField", "customer");
            orderWidget.put("yField", "amount");
            
            orderReport.setWidgets(Arrays.asList(orderWidget));
            orderReport.setPublicShare(false);
            orderReport.setCreatedAt(new Date().toString());
            orderReport.setUpdatedAt(new Date().toString());
            
            reportConfigRepository.saveAll(Arrays.asList(salesReport, productReport, orderReport));
            System.out.println("✅ 示例报表配置已创建: 3 个");
        }
    }
} 