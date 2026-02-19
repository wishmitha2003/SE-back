package com.ezyenglish.config;

import com.ezyenglish.model.ERole;
import com.ezyenglish.model.Role;
import com.ezyenglish.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Database seeder — pre-populates the roles collection on startup
 * if it is empty. This ensures ROLE_STUDENT, ROLE_TEACHER, and
 * ROLE_PARENT are always available.
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            logger.info("Seeding roles collection...");

            roleRepository.save(new Role(ERole.ROLE_STUDENT));
            roleRepository.save(new Role(ERole.ROLE_TEACHER));
            roleRepository.save(new Role(ERole.ROLE_PARENT));

            logger.info("Roles seeded successfully: ROLE_STUDENT, ROLE_TEACHER, ROLE_PARENT");
        } else {
            logger.info("Roles collection already populated. Skipping seed.");
        }
    }
}
