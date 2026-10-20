package com.learntrack.course.exception;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(Long id) {
        super("Course " + id + " not found");
    }
}
