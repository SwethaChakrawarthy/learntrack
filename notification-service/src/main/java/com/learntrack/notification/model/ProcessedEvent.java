package com.learntrack.notification.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Records every event id this service has handled.
 * The primary key guarantees an event is processed at most once, even if Kafka delivers it twice.
 */
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    private String eventId;

    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
        // required by JPA
    }

    public ProcessedEvent(String eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }

    public String getEventId() { return eventId; }
    public Instant getProcessedAt() { return processedAt; }
}
