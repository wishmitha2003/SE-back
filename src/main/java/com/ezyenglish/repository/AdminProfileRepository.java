package com.ezyenglish.repository;

import com.ezyenglish.model.AdminProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminProfileRepository extends MongoRepository<AdminProfile, String> {

    Optional<AdminProfile> findByUserId(String userId);

    List<AdminProfile> findByChildStudentIdsContaining(String studentId);

    boolean existsByUserId(String userId);
}
