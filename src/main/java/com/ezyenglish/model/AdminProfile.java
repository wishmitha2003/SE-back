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
 * AdminProfile document — stores admin-specific data,
 * including linked child student IDs.
 */
@Document(collection = "admin_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private List<String> childStudentIds = new ArrayList<>();

    private String relationship;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public AdminProfile(String userId) {
        this.userId = userId;
    }
}
