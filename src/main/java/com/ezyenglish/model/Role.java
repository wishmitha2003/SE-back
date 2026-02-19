package com.ezyenglish.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Role document stored in the 'roles' collection.
 */
@Document(collection = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    private String id;

    private ERole name;

    public Role(ERole name) {
        this.name = name;
    }
}
