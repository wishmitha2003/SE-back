package com.ezyenglish.repository;

import com.ezyenglish.model.ParentProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentProfileRepository extends MongoRepository<ParentProfile, String> {

    Optional<ParentProfile> findByUserId(String userId);

    List<ParentProfile> findByChildStudentIdsContaining(String studentId);

    boolean existsByUserId(String userId);
}
