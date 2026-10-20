package com.learntrack.course.service;

import com.learntrack.course.dto.EnrollmentResponse;
import com.learntrack.course.event.EnrollmentCreatedEvent;
import com.learntrack.course.exception.AlreadyEnrolledException;
import com.learntrack.course.exception.CourseNotFoundException;
import com.learntrack.course.exception.EnrollmentNotFoundException;
import com.learntrack.course.model.Course;
import com.learntrack.course.model.Enrollment;
import com.learntrack.course.repository.CourseRepository;
import com.learntrack.course.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock EnrollmentRepository enrollmentRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks EnrollmentService enrollmentService;

    private final Course course = new Course("Java Basics", "Intro", "t@x.com");

    @Test
    void enroll_createsEnrollment_withZeroProgress() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndStudentEmail(1L, "s@x.com")).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse response = enrollmentService.enroll(1L, "s@x.com");

        assertThat(response.courseTitle()).isEqualTo("Java Basics");
        assertThat(response.progressPercent()).isZero();
    }

    @Test
    void enroll_publishesEnrollmentCreatedEvent() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndStudentEmail(1L, "s@x.com")).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> inv.getArgument(0));

        enrollmentService.enroll(1L, "s@x.com");

        ArgumentCaptor<EnrollmentCreatedEvent> event = ArgumentCaptor.forClass(EnrollmentCreatedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().studentEmail()).isEqualTo("s@x.com");
        assertThat(event.getValue().courseTitle()).isEqualTo("Java Basics");
        assertThat(event.getValue().eventId()).isNotBlank();
    }

    @Test
    void enroll_twice_isRejected() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndStudentEmail(1L, "s@x.com")).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(1L, "s@x.com"))
                .isInstanceOf(AlreadyEnrolledException.class);
        verify(enrollmentRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void enroll_inMissingCourse_throwsNotFound() {
        when(courseRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enroll(42L, "s@x.com"))
                .isInstanceOf(CourseNotFoundException.class);
    }

    @Test
    void updateProgress_onAnotherStudentsEnrollment_looksNotFound() {
        when(enrollmentRepository.findByIdAndStudentEmail(5L, "intruder@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.updateProgress(5L, "intruder@x.com", 50))
                .isInstanceOf(EnrollmentNotFoundException.class);
    }

    @Test
    void updateProgress_updatesOwnEnrollment() {
        Enrollment enrollment = new Enrollment(course, "s@x.com");
        when(enrollmentRepository.findByIdAndStudentEmail(5L, "s@x.com")).thenReturn(Optional.of(enrollment));

        EnrollmentResponse response = enrollmentService.updateProgress(5L, "s@x.com", 60);

        assertThat(response.progressPercent()).isEqualTo(60);
    }
}
