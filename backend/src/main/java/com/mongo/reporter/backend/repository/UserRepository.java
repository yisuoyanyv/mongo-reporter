package com.mongo.reporter.backend.repository;

import com.mongo.reporter.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
} 