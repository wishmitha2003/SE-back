package com.ezyenglish.repository;

import com.ezyenglish.model.TeacherProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends MongoRepository<TeacherProfile, String> {

    Optional<TeacherProfile> findByUserId(String userId);

    List<TeacherProfile> findBySubject(String subject);

    List<TeacherProfile> findByTaughtCoursesContaining(String courseId);

    boolean existsByUserId(String userId);
}
