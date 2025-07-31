package com.doordash.notification_service.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration for event-driven communication.
 * Configures topics and message serialization/deserialization.
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic;

    @Value("${kafka.topics.user-events:user-events}")
    private String userEventsTopic;

    @Value("${kafka.topics.delivery-events:delivery-events}")
    private String deliveryEventsTopic;

    @Value("${kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    @Value("${kafka.topics.notification-events:notification-events}")
    private String notificationEventsTopic;

    @Value("${kafka.topics.notification-dlq:notification-dlq}")
    private String notificationDlqTopic;

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(orderEventsTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(userEventsTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic deliveryEventsTopic() {
        return TopicBuilder.name(deliveryEventsTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(paymentEventsTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(notificationEventsTopic)
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    @Bean
    public NewTopic notificationDlqTopic() {
        return TopicBuilder.name(notificationDlqTopic)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }
}
