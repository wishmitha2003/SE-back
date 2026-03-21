package com.ezyenglish.repository;

import com.ezyenglish.model.PendingUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PendingUserRepository extends MongoRepository<PendingUser, String> {
    Optional<PendingUser> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    void deleteByEmail(String email);
}
