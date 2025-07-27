package com.mongo.reporter.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Component
public class SampleDataGenerator {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void generateSampleData() {
        generateUsersData();
        generateSalesData();
        generateProductsData();
        generateOrdersData();
    }

    private void generateUsersData() {
        if (mongoTemplate.collectionExists("users")) {
            return; // 数据已存在
        }

        List<Map<String, Object>> users = Arrays.asList(
            createUser("admin", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa", "ADMIN"),
            createUser("testuser", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa", "USER"),
            createUser("john", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa", "USER"),
            createUser("jane", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa", "USER")
        );

        for (Map<String, Object> user : users) {
            mongoTemplate.save(user, "users");
        }
        System.out.println("✅ 示例用户数据已生成");
    }

    private Map<String, Object> createUser(String username, String password, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);
        user.put("role", role);
        return user;
    }

    private void generateSalesData() {
        if (mongoTemplate.collectionExists("sales")) {
            return;
        }

        List<Map<String, Object>> sales = Arrays.asList(
            createSale("1月", 12000, "华东"),
            createSale("2月", 15000, "华东"),
            createSale("3月", 18000, "华东"),
            createSale("1月", 8000, "华北"),
            createSale("2月", 10000, "华北"),
            createSale("3月", 12000, "华北"),
            createSale("1月", 6000, "华南"),
            createSale("2月", 9000, "华南"),
            createSale("3月", 11000, "华南")
        );

        for (Map<String, Object> sale : sales) {
            mongoTemplate.save(sale, "sales");
        }
        System.out.println("✅ 示例销售数据已生成");
    }

    private Map<String, Object> createSale(String month, int amount, String region) {
        Map<String, Object> sale = new HashMap<>();
        sale.put("month", month);
        sale.put("amount", amount);
        sale.put("region", region);
        return sale;
    }

    private void generateProductsData() {
        if (mongoTemplate.collectionExists("products")) {
            return;
        }

        List<Map<String, Object>> products = Arrays.asList(
            createProduct("笔记本电脑", "电子产品", 5999, 50),
            createProduct("智能手机", "电子产品", 3999, 100),
            createProduct("平板电脑", "电子产品", 2999, 30),
            createProduct("办公椅", "办公用品", 599, 200),
            createProduct("办公桌", "办公用品", 899, 80),
            createProduct("打印机", "办公用品", 1299, 25),
            createProduct("咖啡机", "生活用品", 399, 150),
            createProduct("保温杯", "生活用品", 89, 300)
        );

        for (Map<String, Object> product : products) {
            mongoTemplate.save(product, "products");
        }
        System.out.println("✅ 示例产品数据已生成");
    }

    private Map<String, Object> createProduct(String name, String category, int price, int stock) {
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("category", category);
        product.put("price", price);
        product.put("stock", stock);
        return product;
    }

    private void generateOrdersData() {
        if (mongoTemplate.collectionExists("orders")) {
            return;
        }

        List<Map<String, Object>> orders = Arrays.asList(
            createOrder("ORD001", "张三", "笔记本电脑", 5999, "2024-01-15"),
            createOrder("ORD002", "李四", "智能手机", 3999, "2024-01-16"),
            createOrder("ORD003", "王五", "平板电脑", 2999, "2024-01-17"),
            createOrder("ORD004", "赵六", "办公椅", 599, "2024-01-18"),
            createOrder("ORD005", "钱七", "办公桌", 899, "2024-01-19"),
            createOrder("ORD006", "孙八", "打印机", 1299, "2024-01-20"),
            createOrder("ORD007", "周九", "咖啡机", 399, "2024-01-21"),
            createOrder("ORD008", "吴十", "保温杯", 89, "2024-01-22")
        );

        for (Map<String, Object> order : orders) {
            mongoTemplate.save(order, "orders");
        }
        System.out.println("✅ 示例订单数据已生成");
    }

    private Map<String, Object> createOrder(String orderId, String customer, String product, int amount, String date) {
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("customer", customer);
        order.put("product", product);
        order.put("amount", amount);
        order.put("date", date);
        return order;
    }
} 