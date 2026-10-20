package com.learntrack.course.event;

import java.time.Instant;

/**
 * Published after a student's enrollment is committed.
 * eventId is unique per event so consumers can safely ignore duplicates.
 */
public record EnrollmentCreatedEvent(
        String eventId,
        Long enrollmentId,
        Long courseId,
        String courseTitle,
        String studentEmail,
        Instant occurredAt
) {}
