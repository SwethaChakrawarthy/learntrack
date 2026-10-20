package com.learntrack.course.exception;

public class NotCourseOwnerException extends RuntimeException {
    public NotCourseOwnerException() {
        super("Only the course's instructor can modify it");
    }
}
