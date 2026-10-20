package com.learntrack.course.controller;

import com.learntrack.course.dto.EnrollmentResponse;
import com.learntrack.course.dto.ProgressRequest;
import com.learntrack.course.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/me")
    public List<EnrollmentResponse> myEnrollments(Authentication auth) {
        return enrollmentService.getMyEnrollments(auth.getName());
    }

    @PatchMapping("/{id}/progress")
    public EnrollmentResponse updateProgress(@PathVariable Long id,
                                             @Valid @RequestBody ProgressRequest request,
                                             Authentication auth) {
        return enrollmentService.updateProgress(id, auth.getName(), request.progressPercent());
    }
}
