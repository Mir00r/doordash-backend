package com.doordash.notification_service.listener;

import com.doordash.notification_service.dto.NotificationRequestDTO;
import com.doordash.notification_service.entity.Notification;
import com.doordash.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Kafka event listeners for processing events from other microservices.
 * Automatically creates and sends relevant notifications based on events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Process order events and send relevant notifications
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR,
            include = {Exception.class}
    )
    @KafkaListener(topics = "${kafka.topics.order-events}", groupId = "notification-service")
    public void handleOrderEvent(
            @Payload Map<String, Object> orderEvent,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing order event: {} with key: {}", orderEvent.get("eventType"), key);
            
            String eventType = (String) orderEvent.get("eventType");
            Long userId = Long.valueOf(orderEvent.get("userId").toString());
            String orderNumber = (String) orderEvent.get("orderNumber");
            
            switch (eventType) {
                case "ORDER_CONFIRMED" -> sendOrderConfirmedNotifications(userId, orderEvent);
                case "ORDER_PREPARING" -> sendOrderPreparingNotifications(userId, orderEvent);
                case "ORDER_READY" -> sendOrderReadyNotifications(userId, orderEvent);
                case "ORDER_OUT_FOR_DELIVERY" -> sendOrderOutForDeliveryNotifications(userId, orderEvent);
                case "ORDER_DELIVERED" -> sendOrderDeliveredNotifications(userId, orderEvent);
                case "ORDER_CANCELLED" -> sendOrderCancelledNotifications(userId, orderEvent);
                default -> log.debug("Unhandled order event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed order event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error processing order event: {}", orderEvent, e);
            throw e; // Trigger retry mechanism
        }
    }

    /**
     * Process delivery events and send relevant notifications
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR,
            include = {Exception.class}
    )
    @KafkaListener(topics = "${kafka.topics.delivery-events}", groupId = "notification-service")
    public void handleDeliveryEvent(
            @Payload Map<String, Object> deliveryEvent,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing delivery event: {} with key: {}", deliveryEvent.get("eventType"), key);
            
            String eventType = (String) deliveryEvent.get("eventType");
            
            switch (eventType) {
                case "DRIVER_ASSIGNED" -> sendDriverAssignedNotifications(deliveryEvent);
                case "DRIVER_ARRIVED" -> sendDriverArrivedNotifications(deliveryEvent);
                case "DELIVERY_IN_PROGRESS" -> sendDeliveryInProgressNotifications(deliveryEvent);
                case "DELIVERY_COMPLETED" -> sendDeliveryCompletedNotifications(deliveryEvent);
                default -> log.debug("Unhandled delivery event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed delivery event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error processing delivery event: {}", deliveryEvent, e);
            throw e;
        }
    }

    /**
     * Process payment events and send relevant notifications
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR,
            include = {Exception.class}
    )
    @KafkaListener(topics = "${kafka.topics.payment-events}", groupId = "notification-service")
    public void handlePaymentEvent(
            @Payload Map<String, Object> paymentEvent,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing payment event: {} with key: {}", paymentEvent.get("eventType"), key);
            
            String eventType = (String) paymentEvent.get("eventType");
            Long userId = Long.valueOf(paymentEvent.get("userId").toString());
            
            switch (eventType) {
                case "PAYMENT_PROCESSED" -> sendPaymentProcessedNotifications(userId, paymentEvent);
                case "PAYMENT_FAILED" -> sendPaymentFailedNotifications(userId, paymentEvent);
                case "REFUND_PROCESSED" -> sendRefundProcessedNotifications(userId, paymentEvent);
                default -> log.debug("Unhandled payment event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed payment event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error processing payment event: {}", paymentEvent, e);
            throw e;
        }
    }

    /**
     * Process user events and send relevant notifications
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR,
            include = {Exception.class}
    )
    @KafkaListener(topics = "${kafka.topics.user-events}", groupId = "notification-service")
    public void handleUserEvent(
            @Payload Map<String, Object> userEvent,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Processing user event: {} with key: {}", userEvent.get("eventType"), key);
            
            String eventType = (String) userEvent.get("eventType");
            Long userId = Long.valueOf(userEvent.get("userId").toString());
            
            switch (eventType) {
                case "USER_REGISTERED" -> sendWelcomeNotifications(userId, userEvent);
                case "PASSWORD_RESET_REQUESTED" -> sendPasswordResetNotifications(userId, userEvent);
                case "EMAIL_VERIFICATION_REQUESTED" -> sendEmailVerificationNotifications(userId, userEvent);
                default -> log.debug("Unhandled user event type: {}", eventType);
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed user event: {}", eventType);
            
        } catch (Exception e) {
            log.error("Error processing user event: {}", userEvent, e);
            throw e;
        }
    }

    // Private helper methods for sending specific notifications

    private void sendOrderConfirmedNotifications(Long userId, Map<String, Object> orderEvent) {
        notificationService.createNotificationFromTemplate(
                "order-confirmed-email",
                userId,
                (String) orderEvent.get("customerEmail"),
                orderEvent
        );
        
        if (orderEvent.get("customerPhone") != null) {
            notificationService.createNotificationFromTemplate(
                    "order-confirmed-sms",
                    userId,
                    (String) orderEvent.get("customerPhone"),
                    orderEvent
            );
        }
        
        notificationService.createNotificationFromTemplate(
                "order-confirmed-push",
                userId,
                userId.toString(),
                orderEvent
        );
    }

    private void sendOrderPreparingNotifications(Long userId, Map<String, Object> orderEvent) {
        notificationService.createNotificationFromTemplate(
                "order-preparing-push",
                userId,
                userId.toString(),
                orderEvent
        );
    }

    private void sendOrderReadyNotifications(Long userId, Map<String, Object> orderEvent) {
        notificationService.createNotificationFromTemplate(
                "order-ready-push",
                userId,
                userId.toString(),
                orderEvent
        );
    }

    private void sendOrderOutForDeliveryNotifications(Long userId, Map<String, Object> orderEvent) {
        notificationService.createNotificationFromTemplate(
                "order-out-for-delivery-email",
                userId,
                (String) orderEvent.get("customerEmail"),
                orderEvent
        );
        
        notificationService.createNotificationFromTemplate(
                "order-out-for-delivery-push",
                userId,
                userId.toString(),
                orderEvent
        );
    }

    private void sendOrderDeliveredNotifications(Long userId, Map<String, Object> orderEvent) {
        notificationService.createNotificationFromTemplate(
                "order-delivered-email",
                userId,
                (String) orderEvent.get("customerEmail"),
                orderEvent
        );
        
        notificationService.createNotificationFromTemplate(
                "order-delivered-push",
                userId,
                userId.toString(),
                orderEvent
        );
    }

    private void sendOrderCancelledNotifications(Long userId, Map<String, Object> orderEvent) {
        // Implementation for order cancelled notifications
        log.info("Sending order cancelled notifications for user: {}", userId);
    }

    private void sendDriverAssignedNotifications(Map<String, Object> deliveryEvent) {
        Long driverId = Long.valueOf(deliveryEvent.get("driverId").toString());
        notificationService.createNotificationFromTemplate(
                "delivery-assignment-push",
                driverId,
                driverId.toString(),
                deliveryEvent
        );
    }

    private void sendDriverArrivedNotifications(Map<String, Object> deliveryEvent) {
        // Implementation for driver arrived notifications
        log.info("Sending driver arrived notifications");
    }

    private void sendDeliveryInProgressNotifications(Map<String, Object> deliveryEvent) {
        // Implementation for delivery in progress notifications
        log.info("Sending delivery in progress notifications");
    }

    private void sendDeliveryCompletedNotifications(Map<String, Object> deliveryEvent) {
        // Implementation for delivery completed notifications
        log.info("Sending delivery completed notifications");
    }

    private void sendPaymentProcessedNotifications(Long userId, Map<String, Object> paymentEvent) {
        notificationService.createNotificationFromTemplate(
                "payment-processed-email",
                userId,
                (String) paymentEvent.get("customerEmail"),
                paymentEvent
        );
    }

    private void sendPaymentFailedNotifications(Long userId, Map<String, Object> paymentEvent) {
        notificationService.createNotificationFromTemplate(
                "payment-failed-email",
                userId,
                (String) paymentEvent.get("customerEmail"),
                paymentEvent
        );
        
        notificationService.createNotificationFromTemplate(
                "payment-failed-push",
                userId,
                userId.toString(),
                paymentEvent
        );
    }

    private void sendRefundProcessedNotifications(Long userId, Map<String, Object> paymentEvent) {
        // Implementation for refund processed notifications
        log.info("Sending refund processed notifications for user: {}", userId);
    }

    private void sendWelcomeNotifications(Long userId, Map<String, Object> userEvent) {
        notificationService.createNotificationFromTemplate(
                "welcome-email",
                userId,
                (String) userEvent.get("email"),
                userEvent
        );
    }

    private void sendPasswordResetNotifications(Long userId, Map<String, Object> userEvent) {
        notificationService.createNotificationFromTemplate(
                "password-reset-email",
                userId,
                (String) userEvent.get("email"),
                userEvent
        );
    }

    private void sendEmailVerificationNotifications(Long userId, Map<String, Object> userEvent) {
        notificationService.createNotificationFromTemplate(
                "email-verification-email",
                userId,
                (String) userEvent.get("email"),
                userEvent
        );
    }
}
