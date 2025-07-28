package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.User;
import com.mongo.reporter.backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String jwtSecret = "mongo-reporter-secret-key-256-bits-long";
    private final SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            Map<String, Object> res = new HashMap<>();
            res.put("error", "用户名已存在");
            return res;
        }
        
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        Map<String, Object> res = new HashMap<>();
        res.put("msg", "注册成功");
        return res;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Optional<User> dbUserOpt = userRepository.findByUsername(user.getUsername());
        Map<String, Object> res = new HashMap<>();
        
        if (dbUserOpt.isPresent()) {
            User dbUser = dbUserOpt.get();
            if (encoder.matches(user.getPassword(), dbUser.getPassword())) {
                String token = Jwts.builder()
                        .setSubject(dbUser.getUsername())
                        .claim("role", dbUser.getRole())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                        .signWith(key, SignatureAlgorithm.HS256)
                        .compact();
                res.put("token", token);
                res.put("username", dbUser.getUsername());
                res.put("role", dbUser.getRole());
            } else {
                res.put("error", "用户名或密码错误");
            }
        } else {
            res.put("error", "用户名或密码错误");
        }
        return res;
    }
} 