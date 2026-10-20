package com.learntrack.course.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_enrollment_course_student",
               columnNames = {"course_id", "student_email"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(nullable = false)
    private int progressPercent;

    @Column(nullable = false, updatable = false)
    private Instant enrolledAt;

    protected Enrollment() {
        // required by JPA
    }

    public Enrollment(Course course, String studentEmail) {
        this.course = course;
        this.studentEmail = studentEmail;
        this.progressPercent = 0;
    }

    public void updateProgress(int progressPercent) {
        if (progressPercent < 0 || progressPercent > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        this.progressPercent = progressPercent;
    }

    @PrePersist
    void onCreate() {
        this.enrolledAt = Instant.now();
    }

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public String getStudentEmail() { return studentEmail; }
    public int getProgressPercent() { return progressPercent; }
    public Instant getEnrolledAt() { return enrolledAt; }
}
