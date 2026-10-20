package com.learntrack.course.dto;

import com.learntrack.course.model.Enrollment;
import java.time.Instant;

public record EnrollmentResponse(Long id, Long courseId, String courseTitle,
                                 int progressPercent, Instant enrolledAt) {
    public static EnrollmentResponse from(Enrollment e) {
        return new EnrollmentResponse(e.getId(), e.getCourse().getId(), e.getCourse().getTitle(),
                e.getProgressPercent(), e.getEnrolledAt());
    }
}
