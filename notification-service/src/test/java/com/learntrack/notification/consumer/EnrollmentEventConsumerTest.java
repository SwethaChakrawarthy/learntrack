package com.learntrack.notification.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learntrack.notification.event.EnrollmentCreatedEvent;
import com.learntrack.notification.event.InvalidEventException;
import com.learntrack.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentEventConsumerTest {

    @Mock NotificationService notificationService;
    private EnrollmentEventConsumer consumer;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        consumer = new EnrollmentEventConsumer(mapper, notificationService);
    }

    @Test
    void validMessage_isPassedToService_evenWithExtraFields() {
        String json = """
                {"eventId":"evt-1","enrollmentId":5,"courseId":1,"courseTitle":"Java Basics",
                 "studentEmail":"s@x.com","occurredAt":"2026-01-01T00:00:00Z","newFieldFromFuture":"ok"}
                """;

        consumer.onMessage(json);

        ArgumentCaptor<EnrollmentCreatedEvent> event = ArgumentCaptor.forClass(EnrollmentCreatedEvent.class);
        verify(notificationService).handleEnrollmentCreated(event.capture());
        assertThat(event.getValue().eventId()).isEqualTo("evt-1");
        assertThat(event.getValue().courseTitle()).isEqualTo("Java Basics");
    }

    @Test
    void malformedJson_throwsInvalidEvent_andNeverReachesService() {
        assertThatThrownBy(() -> consumer.onMessage("{not json"))
                .isInstanceOf(InvalidEventException.class);
        verifyNoInteractions(notificationService);
    }

    @Test
    void missingEventId_isRejected() {
        assertThatThrownBy(() -> consumer.onMessage("{\"studentEmail\":\"s@x.com\"}"))
                .isInstanceOf(InvalidEventException.class);
        verifyNoInteractions(notificationService);
    }
}
