package com.learntrack.notification.event;

import java.time.Instant;

/** This service's own copy of the event contract; it shares no classes with course-service. */
public record EnrollmentCreatedEvent(
        String eventId,
        Long enrollmentId,
        Long courseId,
        String courseTitle,
        String studentEmail,
        Instant occurredAt
) {}
