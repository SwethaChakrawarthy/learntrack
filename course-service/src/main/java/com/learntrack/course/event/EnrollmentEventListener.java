package com.learntrack.course.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Forwards enrollment events to Kafka only after the database transaction commits,
 * so a rolled-back enrollment never produces an event.
 */
@Component
public class EnrollmentEventListener {

    private final EnrollmentEventPublisher publisher;

    public EnrollmentEventListener(EnrollmentEventPublisher publisher) {
        this.publisher = publisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEnrollmentCreated(EnrollmentCreatedEvent event) {
        publisher.publish(event);
    }
}
