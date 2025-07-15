package com.doordash.order_service.messaging;

import com.doordash.order_service.models.events.OrderEvent;
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

    @Value("${app.kafka.topics.order-placed}")
    private String orderPlacedTopic;

    @Value("${app.kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    public void sendOrderPlacedEvent(OrderEvent orderEvent) {
        orderEvent.setEventType("ORDER_PLACED");
        orderEvent.setTimestamp(Instant.now());
        sendOrderEvent(orderPlacedTopic, orderEvent);
    }

    public void sendOrderCancelledEvent(OrderEvent orderEvent) {
        orderEvent.setEventType("ORDER_CANCELLED");
        orderEvent.setTimestamp(Instant.now());
        sendOrderEvent(orderCancelledTopic, orderEvent);
    }

    private void sendOrderEvent(String topic, OrderEvent orderEvent) {
        log.info("Sending order event to topic {}: {}", topic, orderEvent);
        meterRegistry.counter("order.events.sent", "topic", topic).increment();

        CompletableFuture<SendResult<String, OrderEvent>> future = kafkaTemplate.send(topic, orderEvent.getOrderId().toString(), orderEvent);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Order event sent successfully to topic {}: {}", topic, result.getRecordMetadata());
                meterRegistry.counter("order.events.sent.success", "topic", topic).increment();
            } else {
                log.error("Failed to send order event to topic {}", topic, ex);
                meterRegistry.counter("order.events.sent.failure", "topic", topic).increment();
            }
        });
    }
}