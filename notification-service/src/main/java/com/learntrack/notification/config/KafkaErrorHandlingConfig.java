package com.learntrack.notification.config;

import com.learntrack.notification.event.InvalidEventException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

    /**
     * Retries a failed message 3 times, 1 second apart (e.g. the database is briefly down).
     * If it still fails, it goes to "<topic>.DLT" (dead-letter topic) for inspection
     * instead of blocking the partition forever.
     * Messages that can never succeed (InvalidEventException) skip retries and go straight to the DLT.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        handler.addNotRetryableExceptions(InvalidEventException.class);
        return handler;
    }
}
