package com.ezyenglish.repository;

import com.ezyenglish.model.StudentProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends MongoRepository<StudentProfile, String> {

    Optional<StudentProfile> findByUserId(String userId);

    List<StudentProfile> findByEnrolledCoursesContaining(String courseId);

    List<StudentProfile> findByGradeLevel(String gradeLevel);

    boolean existsByUserId(String userId);
}
