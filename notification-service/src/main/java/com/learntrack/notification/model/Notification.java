package com.learntrack.notification.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private String sourceEventId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
        // required by JPA
    }

    public Notification(String recipientEmail, String message, String sourceEventId) {
        this.recipientEmail = recipientEmail;
        this.message = message;
        this.sourceEventId = sourceEventId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getMessage() { return message; }
    public String getSourceEventId() { return sourceEventId; }
    public Instant getCreatedAt() { return createdAt; }
}
