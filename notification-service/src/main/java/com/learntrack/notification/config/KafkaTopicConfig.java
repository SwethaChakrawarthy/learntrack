package com.learntrack.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declares the topics this service reads from and writes to; creating an existing topic is a no-op. */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic enrollmentCreatedTopic(@Value("${app.kafka.topics.enrollment-created}") String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic enrollmentCreatedDeadLetterTopic(@Value("${app.kafka.topics.enrollment-created}") String name) {
        // Same partition count as the main topic: failed records keep their original partition number.
        return TopicBuilder.name(name + ".DLT").partitions(3).replicas(1).build();
    }
}
