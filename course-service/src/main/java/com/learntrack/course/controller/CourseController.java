package com.learntrack.course.controller;

import com.learntrack.course.dto.CourseRequest;
import com.learntrack.course.dto.CourseResponse;
import com.learntrack.course.dto.EnrollmentResponse;
import com.learntrack.course.service.CourseService;
import com.learntrack.course.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public CourseController(CourseService courseService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public List<CourseResponse> catalog() {
        return courseService.getCatalog();
    }

    @GetMapping("/{id}")
    public CourseResponse getCourse(@PathVariable Long id) {
        return courseService.getCourse(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public CourseResponse create(@Valid @RequestBody CourseRequest request, Authentication auth) {
        return courseService.createCourse(request, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest request,
                                 Authentication auth) {
        return courseService.updateCourse(id, request, auth.getName());
    }

    @PostMapping("/{id}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public EnrollmentResponse enroll(@PathVariable Long id, Authentication auth) {
        return enrollmentService.enroll(id, auth.getName());
    }
}
