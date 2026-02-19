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
 * ParentProfile document — stores parent-specific data,
 * including linked child student IDs.
 */
@Document(collection = "parent_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentProfile {

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

    public ParentProfile(String userId) {
        this.userId = userId;
    }
}
