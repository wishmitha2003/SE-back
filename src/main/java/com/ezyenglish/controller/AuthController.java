package com.ezyenglish.controller;

import com.ezyenglish.dto.request.LoginRequest;
import com.ezyenglish.dto.request.SignupRequest;
import com.ezyenglish.dto.response.JwtResponse;
import com.ezyenglish.dto.response.MessageResponse;
import com.ezyenglish.model.ERole;
import com.ezyenglish.model.ParentProfile;
import com.ezyenglish.model.Role;
import com.ezyenglish.model.StudentProfile;
import com.ezyenglish.model.TeacherProfile;
import com.ezyenglish.model.User;
import com.ezyenglish.repository.ParentProfileRepository;
import com.ezyenglish.repository.RoleRepository;
import com.ezyenglish.repository.StudentProfileRepository;
import com.ezyenglish.repository.TeacherProfileRepository;
import com.ezyenglish.repository.UserRepository;
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
    @PostMapping({"/signin","login"})
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles
                )
        );
    }

    /**
     * POST /api/auth/signup or /api/auth/register — Register a new user.
     */
    @PostMapping({"/signup", "/register"})
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        System.out.println("REGISTER API HIT");
        System.out.println("Username: " + signUpRequest.getUsername());
        System.out.println("Email: " + signUpRequest.getEmail());

        // Check for duplicate username
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            System.out.println("Username already taken");
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            System.out.println("Email already in use");
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user account
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword())
        );

        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setPhone(signUpRequest.getPhone());

        // Assign roles
        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_STUDENT is not found. Please seed the database."));
            roles.add(studentRole);
            System.out.println("Assigned default role: ROLE_STUDENT");
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "teacher":
                        Role teacherRole = roleRepository.findByName(ERole.ROLE_TEACHER)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_TEACHER is not found."));
                        roles.add(teacherRole);
                        System.out.println("Assigned role: ROLE_TEACHER");
                        break;

                    case "parent":
                        Role parentRole = roleRepository.findByName(ERole.ROLE_PARENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_PARENT is not found."));
                        roles.add(parentRole);
                        System.out.println("Assigned role: ROLE_PARENT");
                        break;

                    case "student":
                    default:
                        Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_STUDENT is not found."));
                        roles.add(studentRole);
                        System.out.println("Assigned role: ROLE_STUDENT");
                        break;
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        System.out.println("User saved with ID: " + user.getId());

        // Create role-specific profile(s)
        for (Role role : roles) {
            switch (role.getName()) {
                case ROLE_STUDENT:
                    if (!studentProfileRepository.existsByUserId(user.getId())) {
                        StudentProfile studentProfile = new StudentProfile(user.getId());
                        studentProfileRepository.save(studentProfile);
                        System.out.println("Student profile created");
                    }
                    break;

                case ROLE_TEACHER:
                    if (!teacherProfileRepository.existsByUserId(user.getId())) {
                        TeacherProfile teacherProfile = new TeacherProfile(user.getId());
                        teacherProfileRepository.save(teacherProfile);
                        System.out.println("Teacher profile created");
                    }
                    break;

                case ROLE_PARENT:
                    if (!parentProfileRepository.existsByUserId(user.getId())) {
                        ParentProfile parentProfile = new ParentProfile(user.getId());
                        parentProfileRepository.save(parentProfile);
                        System.out.println("Parent profile created");
                    }
                    break;

                default:
                    break;
            }
        }

        System.out.println("Registration completed successfully");
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}