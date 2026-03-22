package com.ezyenglish.controller;

import com.ezyenglish.dto.request.*;
import com.ezyenglish.dto.response.JwtResponse;
import com.ezyenglish.dto.response.MessageResponse;
import com.ezyenglish.model.*;
import com.ezyenglish.repository.*;
import com.ezyenglish.security.jwt.JwtUtils;
import com.ezyenglish.security.service.UserDetailsImpl;
import com.ezyenglish.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private PasswordResetOtpRepository passwordResetOtpRepository;

    @Autowired
    private PendingUserRepository pendingUserRepository;

    @Autowired
    private EmailService emailService;

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

    @PostMapping({"/signin", "/login"})
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

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

    @PostMapping({"/signup", "/register"})
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        System.out.println("REGISTER API HIT");
        System.out.println("Username: " + signUpRequest.getUsername());
        System.out.println("Email: " + signUpRequest.getEmail());

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        if (pendingUserRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is pending verification!"));
        }

        if (pendingUserRepository.existsByEmail(signUpRequest.getEmail())) {
            pendingUserRepository.deleteByEmail(signUpRequest.getEmail());
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        System.out.println("Generated OTP: " + otp);

        PendingUser pendingUser = new PendingUser();
        pendingUser.setUsername(signUpRequest.getUsername());
        pendingUser.setEmail(signUpRequest.getEmail());
        pendingUser.setPassword(encoder.encode(signUpRequest.getPassword()));
        pendingUser.setFirstName(signUpRequest.getFirstName());
        pendingUser.setLastName(signUpRequest.getLastName());
        pendingUser.setPhone(signUpRequest.getPhone());
        pendingUser.setRoles(signUpRequest.getRoles());
        pendingUser.setOtp(otp);
        pendingUser.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));

        pendingUserRepository.save(pendingUser);
        emailService.sendOtpEmail(signUpRequest.getEmail(), otp);

        System.out.println("Pending user saved and OTP email sent");
        return ResponseEntity.ok(
                new MessageResponse("OTP sent to email. Please verify to complete registration.")
        );
    }

    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        System.out.println("VERIFY OTP API HIT");
        System.out.println("Email: " + request.getEmail());
        System.out.println("OTP: " + request.getOtp());

        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Pending registration not found"));

        if (!pendingUser.getOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Invalid OTP"));
        }

        if (pendingUser.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("OTP expired"));
        }

        User user = new User(
                pendingUser.getUsername(),
                pendingUser.getEmail(),
                pendingUser.getPassword()
        );

        user.setFirstName(pendingUser.getFirstName());
        user.setLastName(pendingUser.getLastName());
        user.setPhone(pendingUser.getPhone());

        Set<String> strRoles = pendingUser.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role studentRole = roleRepository.findByName(ERole.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_STUDENT is not found."));
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
        System.out.println("Verified user saved with ID: " + user.getId());

        for (Role role : roles) {
            switch (role.getName()) {
                case ROLE_STUDENT:
                    if (!studentProfileRepository.existsByUserId(user.getId())) {
                        studentProfileRepository.save(new StudentProfile(user.getId()));
                        System.out.println("Student profile created");
                    }
                    break;
                case ROLE_TEACHER:
                    if (!teacherProfileRepository.existsByUserId(user.getId())) {
                        teacherProfileRepository.save(new TeacherProfile(user.getId()));
                        System.out.println("Teacher profile created");
                    }
                    break;
                case ROLE_PARENT:
                    if (!parentProfileRepository.existsByUserId(user.getId())) {
                        parentProfileRepository.save(new ParentProfile(user.getId()));
                        System.out.println("Parent profile created");
                    }
                    break;
                default:
                    break;
            }
        }

        pendingUserRepository.deleteByEmail(request.getEmail());
        System.out.println("Pending user deleted");
        System.out.println("Registration completed successfully");

        return ResponseEntity.ok(new MessageResponse("Registration completed successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        passwordResetOtpRepository.deleteByEmail(request.getEmail());

        String otp = String.format("%06d", new Random().nextInt(1000000));

        PasswordResetOtp resetOtp = new PasswordResetOtp(
                user.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(10)
        );

        passwordResetOtpRepository.save(resetOtp);
        emailService.sendPasswordResetOtp(user.getEmail(), otp);

        return ResponseEntity.ok(new MessageResponse("Password reset OTP sent to email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetOtp resetOtp = passwordResetOtpRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Reset request not found"));

        if (!resetOtp.getOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid OTP"));
        }

        if (resetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(new MessageResponse("OTP expired"));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetOtpRepository.deleteByEmail(request.getEmail());

        return ResponseEntity.ok(new MessageResponse("Password reset successfully."));
    }

    @PostMapping("/send-reset-password-otp")
    public ResponseEntity<?> sendResetPasswordOtp(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("User is not authenticated."));
        }

        String loginValue = authentication.getName();

        User user = userRepository.findByUsernameOrEmail(loginValue, loginValue)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        passwordResetOtpRepository.deleteByEmail(user.getEmail());

        String otp = String.format("%06d", new Random().nextInt(1000000));

        PasswordResetOtp resetOtp = new PasswordResetOtp(
                user.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(10)
        );

        passwordResetOtpRepository.save(resetOtp);
        emailService.sendPasswordResetOtp(user.getEmail(), otp);

        return ResponseEntity.ok(new MessageResponse("Password reset OTP sent to your registered email."));
    }

    @PostMapping("/reset-password-authenticated")
    public ResponseEntity<?> resetPasswordAuthenticated(
            @Valid @RequestBody AuthenticatedResetPasswordRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("User is not authenticated."));
        }

        String loginValue = authentication.getName();

        User user = userRepository.findByUsernameOrEmail(loginValue, loginValue)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        PasswordResetOtp resetOtp = passwordResetOtpRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("Reset request not found"));

        if (!resetOtp.getOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid OTP"));
        }

        if (resetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(new MessageResponse("OTP expired"));
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetOtpRepository.deleteByEmail(user.getEmail());

        return ResponseEntity.ok(new MessageResponse("Password reset successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("User is not authenticated."));
        }

        String loginValue = authentication.getName();

        User user = userRepository.findByUsernameOrEmail(loginValue, loginValue)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("User is not authenticated."));
        }

        String loginValue = authentication.getName();

        User user = userRepository.findByUsernameOrEmail(loginValue, loginValue)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setProfileImageUrl(request.getProfileImageUrl());


        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Profile updated successfully."));
    }

    @PostMapping("/resend-registration-otp")
    public ResponseEntity<?> resendRegistrationOtp(@Valid @RequestBody EmailRequest request) {

        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Pending registration not found"));

        String otp = String.format("%06d", new Random().nextInt(1000000));

        pendingUser.setOtp(otp);
        pendingUser.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));

        pendingUserRepository.save(pendingUser);
        emailService.sendOtpEmail(pendingUser.getEmail(), otp);

        return ResponseEntity.ok(new MessageResponse("OTP resent successfully."));
    }

    @PostMapping("/resend-forgot-password-otp")
    public ResponseEntity<?> resendForgotPasswordOtp(@Valid @RequestBody EmailRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        passwordResetOtpRepository.deleteByEmail(request.getEmail());

        String otp = String.format("%06d", new Random().nextInt(1000000));

        PasswordResetOtp resetOtp = new PasswordResetOtp(
                user.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(10)
        );

        passwordResetOtpRepository.save(resetOtp);
        emailService.sendPasswordResetOtp(user.getEmail(), otp);

        return ResponseEntity.ok(new MessageResponse("OTP resent successfully."));
    }

    @PostMapping("/resend-reset-password-otp")
    public ResponseEntity<?> resendResetPasswordOtp(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("User not authenticated"));
        }

        String loginValue = authentication.getName();

        User user = userRepository.findByUsernameOrEmail(loginValue, loginValue)
                .orElseThrow(() -> new RuntimeException("User not found"));

        passwordResetOtpRepository.deleteByEmail(user.getEmail());

        String otp = String.format("%06d", new Random().nextInt(1000000));

        PasswordResetOtp resetOtp = new PasswordResetOtp(
                user.getEmail(),
                otp,
                LocalDateTime.now().plusMinutes(10)
        );

        passwordResetOtpRepository.save(resetOtp);
        emailService.sendPasswordResetOtp(user.getEmail(), otp);

        return ResponseEntity.ok(new MessageResponse("OTP resent successfully."));
    }
}