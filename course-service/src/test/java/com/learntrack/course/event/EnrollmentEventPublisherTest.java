package com.learntrack.course.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentEventPublisherTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void publish_sendsJsonKeyedByStudentEmail() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        EnrollmentEventPublisher publisher = new EnrollmentEventPublisher(kafkaTemplate, mapper, "test-topic");
        CompletableFuture<SendResult<String, String>> pending = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("test-topic"), eq("s@x.com"), anyString())).thenReturn(pending);

        publisher.publish(new EnrollmentCreatedEvent("evt-1", 5L, 1L, "Java Basics", "s@x.com",
                Instant.parse("2026-01-01T00:00:00Z")));

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("test-topic"), eq("s@x.com"), json.capture());
        assertThat(json.getValue()).contains("\"eventId\":\"evt-1\"").contains("\"courseTitle\":\"Java Basics\"");
    }
}
