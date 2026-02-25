package com.ezyenglish.config;

import com.ezyenglish.model.ERole;
import com.ezyenglish.model.Role;
import com.ezyenglish.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleSeeder {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.findByName(ERole.ROLE_STUDENT).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_STUDENT));
            }
            if (roleRepository.findByName(ERole.ROLE_TEACHER).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_TEACHER));
            }
            if (roleRepository.findByName(ERole.ROLE_PARENT).isEmpty()) {
                roleRepository.save(new Role(ERole.ROLE_PARENT));
            }
        };
    }
}
