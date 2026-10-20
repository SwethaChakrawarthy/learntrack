package com.learntrack.course.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({CourseNotFoundException.class, EnrollmentNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AlreadyEnrolledException.class)
    public ProblemDetail handleAlreadyEnrolled(AlreadyEnrolledException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Two simultaneous enroll requests can both pass the exists-check;
    // the database unique constraint catches the second one.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConstraintViolation(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Request conflicts with existing data");
    }

    @ExceptionHandler(NotCourseOwnerException.class)
    public ProblemDetail handleNotOwner(NotCourseOwnerException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
