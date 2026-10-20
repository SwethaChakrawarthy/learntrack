package com.learntrack.course.exception;

public class AlreadyEnrolledException extends RuntimeException {
    public AlreadyEnrolledException(Long courseId) {
        super("Already enrolled in course " + courseId);
    }
}
