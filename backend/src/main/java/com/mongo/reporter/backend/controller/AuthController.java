package com.mongo.reporter.backend.controller;

import com.mongo.reporter.backend.model.User;
import com.mongo.reporter.backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String jwtSecret = "mongo-reporter-secret";

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
        Map<String, Object> res = new HashMap<>();
        res.put("msg", "注册成功");
        return res;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        User dbUser = userRepository.findByUsername(user.getUsername());
        Map<String, Object> res = new HashMap<>();
        if (dbUser != null && encoder.matches(user.getPassword(), dbUser.getPassword())) {
            String token = Jwts.builder()
                    .setSubject(dbUser.getUsername())
                    .claim("role", dbUser.getRole())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(SignatureAlgorithm.HS256, jwtSecret)
                    .compact();
            res.put("token", token);
            res.put("username", dbUser.getUsername());
            res.put("role", dbUser.getRole());
        } else {
            res.put("error", "用户名或密码错误");
        }
        return res;
    }
} 