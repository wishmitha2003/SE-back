package com.ezyenglish.controller;

import com.ezyenglish.dto.request.LoginRequest;
import com.ezyenglish.dto.request.SignupRequest;
import com.ezyenglish.dto.response.JwtResponse;
import com.ezyenglish.dto.response.MessageResponse;
import com.ezyenglish.model.*;
import com.ezyenglish.repository.*;
import com.ezyenglish.security.jwt.JwtUtils;
import com.ezyenglish.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication controller — handles signup and signin.
 * All endpoints are public (/api/auth/**).
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private ParentProfileRepository parentProfileRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * POST /api/auth/signin — Authenticate user and return JWT.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    /**
     * POST /api/auth/signup — Register a new user with specified roles.
     * Automatically creates the corresponding role-specific profile.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {

        // Check for duplicate username
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user account
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setPhone(signUpRequest.getPhone());

        // Assign roles
        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // Default to ROLE_STUDENT
            Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_STUDENT is not found. Please seed the database."));
            roles.add(studentRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "teacher":
                        Role teacherRole = roleRepository.findByName(ERole.ROLE_TEACHER)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_TEACHER is not found."));
                        roles.add(teacherRole);
                        break;
                    case "parent":
                        Role parentRole = roleRepository.findByName(ERole.ROLE_PARENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_PARENT is not found."));
                        roles.add(parentRole);
                        break;
                    case "student":
                    default:
                        Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_STUDENT is not found."));
                        roles.add(studentRole);
                        break;
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        // Create role-specific profile(s)
        for (Role role : roles) {
            switch (role.getName()) {
                case ROLE_STUDENT:
                    if (!studentProfileRepository.existsByUserId(user.getId())) {
                        StudentProfile studentProfile = new StudentProfile(user.getId());
                        studentProfileRepository.save(studentProfile);
                    }
                    break;
                case ROLE_TEACHER:
                    if (!teacherProfileRepository.existsByUserId(user.getId())) {
                        TeacherProfile teacherProfile = new TeacherProfile(user.getId());
                        teacherProfileRepository.save(teacherProfile);
                    }
                    break;
                case ROLE_PARENT:
                    if (!parentProfileRepository.existsByUserId(user.getId())) {
                        ParentProfile parentProfile = new ParentProfile(user.getId());
                        parentProfileRepository.save(parentProfile);
                    }
                    break;
            }
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
