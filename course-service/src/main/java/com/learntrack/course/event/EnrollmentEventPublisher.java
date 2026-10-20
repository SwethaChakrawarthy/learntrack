package com.learntrack.course.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Sends enrollment events to Kafka as plain JSON strings.
 * Using plain JSON (not Java type headers) means other services don't need this service's classes.
 */
@Component
public class EnrollmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;
    private final String topic;

    public EnrollmentEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                    ObjectMapper mapper,
                                    @Value("${app.kafka.topics.enrollment-created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
        this.topic = topic;
    }

    public void publish(EnrollmentCreatedEvent event) {
        String json;
        try {
            json = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize event {}", event.eventId(), e);
            return;
        }
        // Keyed by student email so all of one student's events land on the same partition, in order.
        kafkaTemplate.send(topic, event.studentEmail(), json)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} for enrollment {}",
                                event.eventId(), event.enrollmentId(), ex);
                    } else {
                        log.info("Published event {} to {}-{}@{}", event.eventId(), topic,
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }
}
