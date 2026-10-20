package com.learntrack.course.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String instructorEmail;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    protected Course() {
        // required by JPA
    }

    public Course(String title, String description, String instructorEmail) {
        this.title = title;
        this.description = description;
        this.instructorEmail = instructorEmail;
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstructorEmail() { return instructorEmail; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
