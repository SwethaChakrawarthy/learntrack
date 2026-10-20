package com.learntrack.course.dto;

import com.learntrack.course.model.Course;
import java.time.Instant;

public record CourseResponse(Long id, String title, String description,
                             String instructorEmail, Instant createdAt) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(),
                course.getInstructorEmail(), course.getCreatedAt());
    }
}
