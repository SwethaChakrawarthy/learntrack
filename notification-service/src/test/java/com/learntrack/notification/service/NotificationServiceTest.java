package com.learntrack.notification.service;

import com.learntrack.notification.event.EnrollmentCreatedEvent;
import com.learntrack.notification.model.Notification;
import com.learntrack.notification.model.ProcessedEvent;
import com.learntrack.notification.repository.NotificationRepository;
import com.learntrack.notification.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock ProcessedEventRepository processedEventRepository;
    @InjectMocks NotificationService notificationService;

    private final EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
            "evt-1", 5L, 1L, "Java Basics", "s@x.com", Instant.parse("2026-01-01T00:00:00Z"));

    @Test
    void newEvent_savesNotification_andMarksEventProcessed() {
        when(processedEventRepository.existsById("evt-1")).thenReturn(false);

        boolean handled = notificationService.handleEnrollmentCreated(event);

        assertThat(handled).isTrue();
        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getRecipientEmail()).isEqualTo("s@x.com");
        assertThat(saved.getValue().getMessage()).contains("Java Basics");
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void duplicateEvent_isSkipped() {
        when(processedEventRepository.existsById("evt-1")).thenReturn(true);

        boolean handled = notificationService.handleEnrollmentCreated(event);

        assertThat(handled).isFalse();
        verifyNoInteractions(notificationRepository);
        verify(processedEventRepository, never()).save(any());
    }
}
