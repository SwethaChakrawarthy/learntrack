package com.learntrack.course.service;

import com.learntrack.course.dto.EnrollmentResponse;
import com.learntrack.course.exception.AlreadyEnrolledException;
import com.learntrack.course.exception.CourseNotFoundException;
import com.learntrack.course.exception.EnrollmentNotFoundException;
import com.learntrack.course.model.Course;
import com.learntrack.course.model.Enrollment;
import com.learntrack.course.repository.CourseRepository;
import com.learntrack.course.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    public EnrollmentResponse enroll(Long courseId, String studentEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        if (enrollmentRepository.existsByCourseIdAndStudentEmail(courseId, studentEmail)) {
            throw new AlreadyEnrolledException(courseId);
        }
        Enrollment saved = enrollmentRepository.save(new Enrollment(course, studentEmail));
        // Step 3: publish an EnrollmentCreated event to Kafka here.
        return EnrollmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String studentEmail) {
        return enrollmentRepository.findByStudentEmailOrderByEnrolledAtDesc(studentEmail).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    @Transactional
    public EnrollmentResponse updateProgress(Long enrollmentId, String studentEmail, int progressPercent) {
        // Looking up by id AND owner returns 404 for other students' enrollments,
        // so the API doesn't reveal which enrollment ids exist.
        Enrollment enrollment = enrollmentRepository.findByIdAndStudentEmail(enrollmentId, studentEmail)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));
        enrollment.updateProgress(progressPercent);
        return EnrollmentResponse.from(enrollment);
    }
}
