package com.learntrack.course.exception;

public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(Long id) {
        super("Enrollment " + id + " not found");
    }
}
