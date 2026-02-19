package com.ezyenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentProfile document — stores student-specific e-learning data.
 * References the User document via userId.
 */
@Document(collection = "student_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String gradeLevel;

    private List<String> enrolledCourses = new ArrayList<>();

    private double overallProgress;

    private int totalPoints;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public StudentProfile(String userId) {
        this.userId = userId;
    }
}
