package com.learntrack.notification.service;

import com.learntrack.notification.event.EnrollmentCreatedEvent;
import com.learntrack.notification.model.Notification;
import com.learntrack.notification.model.ProcessedEvent;
import com.learntrack.notification.repository.NotificationRepository;
import com.learntrack.notification.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               ProcessedEventRepository processedEventRepository) {
        this.notificationRepository = notificationRepository;
        this.processedEventRepository = processedEventRepository;
    }

    /**
     * Idempotent: Kafka guarantees at-least-once delivery, so the same event can arrive twice.
     * The notification and the processed-event record are saved in one transaction, so either
     * both exist or neither does.
     */
    @Transactional
    public boolean handleEnrollmentCreated(EnrollmentCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Skipping duplicate event {}", event.eventId());
            return false;
        }
        String message = "You're enrolled in \"" + event.courseTitle() + "\". Happy learning!";
        notificationRepository.save(new Notification(event.studentEmail(), message, event.eventId()));
        processedEventRepository.save(new ProcessedEvent(event.eventId()));
        // Simulated delivery: a real system would call an email provider here.
        log.info("Notification sent to {}: {}", event.studentEmail(), message);
        return true;
    }
}
