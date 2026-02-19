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
 * TeacherProfile document — stores teacher-specific data.
 */
@Document(collection = "teacher_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String subject;

    private String qualification;

    private List<String> taughtCourses = new ArrayList<>();

    private String bio;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public TeacherProfile(String userId) {
        this.userId = userId;
    }
}
