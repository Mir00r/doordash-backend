package com.doordash.ordering_service.messaging;

import com.doordash.ordering_service.models.events.OrderEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    
    @Value("${kafka.topics.order-placed}")
    private String orderPlacedTopic;
    
    @Value("${kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    /**
     * Send an order placed event to Kafka
     * @param orderEvent the order event to send
     */
    public void sendOrderPlacedEvent(OrderEvent orderEvent) {
        orderEvent.setEventType("ORDER_PLACED");
        orderEvent.setEventTime(Instant.now());
        sendOrderEvent(orderPlacedTopic, orderEvent.getOrderId().toString(), orderEvent);
    }

    /**
     * Send an order cancelled event to Kafka
     * @param orderEvent the order event to send
     */
    public void sendOrderCancelledEvent(OrderEvent orderEvent) {
        orderEvent.setEventType("ORDER_CANCELLED");
        orderEvent.setEventTime(Instant.now());
        sendOrderEvent(orderCancelledTopic, orderEvent.getOrderId().toString(), orderEvent);
    }

    /**
     * Send an order event to Kafka
     * @param topic the topic to send to
     * @param key the message key
     * @param orderEvent the order event to send
     */
    private void sendOrderEvent(String topic, String key, OrderEvent orderEvent) {
        log.info("Sending order event to topic: {}, orderId: {}, eventType: {}", 
                topic, orderEvent.getOrderId(), orderEvent.getEventType());
        
        try {
            CompletableFuture<SendResult<String, OrderEvent>> future = kafkaTemplate.send(topic, key, orderEvent);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Order event sent successfully, orderId: {}, topic: {}, partition: {}, offset: {}", 
                            orderEvent.getOrderId(), topic, result.getRecordMetadata().partition(), 
                            result.getRecordMetadata().offset());
                    
                    meterRegistry.counter("order.event.sent", "eventType", orderEvent.getEventType()).increment();
                } else {
                    log.error("Failed to send order event, orderId: {}, topic: {}, error: {}", 
                            orderEvent.getOrderId(), topic, ex.getMessage());
                    
                    meterRegistry.counter("order.event.error", "eventType", orderEvent.getEventType()).increment();
                }
            });
        } catch (Exception e) {
            log.error("Error sending order event: {}", e.getMessage());
            meterRegistry.counter("order.event.error", "eventType", orderEvent.getEventType()).increment();
        }
    }
}