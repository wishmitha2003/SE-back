package com.ezyenglish.service;

import com.ezyenglish.model.StudentProfile;
import com.ezyenglish.model.TeacherProfile;
import com.ezyenglish.repository.StudentProfileRepository;
import com.ezyenglish.repository.TeacherProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for teacher-specific operations (e.g., course management).
 */
@Service
public class TeacherService {

    @Autowired
    private TeacherProfileRepository teacherProfileRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    /**
     * Assign a course to a teacher.
     */
    public TeacherProfile assignCourse(String userId, String courseId) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for userId: " + userId));

        if (!profile.getTaughtCourses().contains(courseId)) {
            profile.getTaughtCourses().add(courseId);
            teacherProfileRepository.save(profile);
        }

        return profile;
    }

    /**
     * Remove a course from a teacher.
     */
    public TeacherProfile removeCourse(String userId, String courseId) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for userId: " + userId));

        profile.getTaughtCourses().remove(courseId);
        teacherProfileRepository.save(profile);

        return profile;
    }

    /**
     * Get all students enrolled in a specific course.
     */
    public List<StudentProfile> getStudentsByCourse(String courseId) {
        return studentProfileRepository.findByEnrolledCoursesContaining(courseId);
    }

    /**
     * Get teacher profile by user ID.
     */
    public TeacherProfile getTeacherProfile(String userId) {
        return teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for userId: " + userId));
    }
}
