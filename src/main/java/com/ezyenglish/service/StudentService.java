package com.ezyenglish.service;

import com.ezyenglish.model.StudentProfile;
import com.ezyenglish.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for student-specific operations (e.g., course enrollment).
 */
@Service
public class StudentService {

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    /**
     * Enroll a student in a course.
     */
    public StudentProfile enrollInCourse(String userId, String courseId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found for userId: " + userId));

        if (!profile.getEnrolledCourses().contains(courseId)) {
            profile.getEnrolledCourses().add(courseId);
            studentProfileRepository.save(profile);
        }

        return profile;
    }

    /**
     * Unenroll a student from a course.
     */
    public StudentProfile unenrollFromCourse(String userId, String courseId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found for userId: " + userId));

        profile.getEnrolledCourses().remove(courseId);
        studentProfileRepository.save(profile);

        return profile;
    }

    /**
     * Get student profile by user ID.
     */
    public StudentProfile getStudentProfile(String userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found for userId: " + userId));
    }

    /**
     * Get all student profiles.
     */
    public List<StudentProfile> getAllStudents() {
        return studentProfileRepository.findAll();
    }

    /**
     * Update student progress and points.
     */
    public StudentProfile updateProgress(String userId, double progress, int points) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student profile not found for userId: " + userId));

        profile.setOverallProgress(progress);
        profile.setTotalPoints(points);
        studentProfileRepository.save(profile);

        return profile;
    }
}
