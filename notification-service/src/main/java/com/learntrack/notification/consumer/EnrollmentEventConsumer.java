package com.learntrack.notification.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntrack.notification.event.EnrollmentCreatedEvent;
import com.learntrack.notification.event.InvalidEventException;
import com.learntrack.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentEventConsumer {

    private final ObjectMapper mapper;
    private final NotificationService notificationService;

    public EnrollmentEventConsumer(ObjectMapper mapper, NotificationService notificationService) {
        this.mapper = mapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topics.enrollment-created}")
    public void onMessage(String payload) {
        notificationService.handleEnrollmentCreated(parse(payload));
    }

    EnrollmentCreatedEvent parse(String payload) {
        try {
            EnrollmentCreatedEvent event = mapper.readValue(payload, EnrollmentCreatedEvent.class);
            if (event.eventId() == null || event.studentEmail() == null) {
                throw new InvalidEventException("Event is missing eventId or studentEmail", null);
            }
            return event;
        } catch (JsonProcessingException e) {
            throw new InvalidEventException("Malformed event JSON", e);
        }
    }
}
