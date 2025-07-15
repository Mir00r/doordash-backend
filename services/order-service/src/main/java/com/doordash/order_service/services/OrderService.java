package com.doordash.order_service.services;

import com.doordash.order_service.enums.PaymentStatus;
import com.doordash.order_service.messaging.OrderEventProducer;
import com.doordash.order_service.models.dtos.OrderRequest;
import com.doordash.order_service.models.dtos.OrderResponse;
import com.doordash.order_service.models.entities.Order;
import com.doordash.order_service.models.entities.OrderItems;
import com.doordash.order_service.models.events.OrderEvent;
import com.doordash.order_service.repositories.OrderRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final PaymentClient paymentClient;
    private final RestaurantClient restaurantClient;
    private final OrderEventProducer orderEventProducer;
    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        log.info("Placing order for customer: {}", orderRequest.getCustomerId());
        meterRegistry.counter("order.place").increment();
        
        // 1. Get cart from Cart Service
        var cartResponse = cartClient.getCart(orderRequest.getCartId(), orderRequest.getCustomerId());
        
        // 2. Get restaurant info from Restaurant Service
        var restaurant = restaurantClient.getRestaurantInfo(cartResponse.getRestaurantId());
        
        // 3. Process payment via Payment Service
        var paymentResult = paymentClient.processPayment(
            orderRequest.getCustomerId(),
            orderRequest.getPaymentMethodId(),
            cartResponse.getTotalAmount()
        );
        
        // 4. Create order
        Order order = Order.builder()
            .customerId(orderRequest.getCustomerId())
            .restaurantId(cartResponse.getRestaurantId())
            .orderTime(Instant.now())
            .status(Order.OrderStatus.PENDING)
            .totalAmount(cartResponse.getTotalAmount())
            .items(new OrderItems(cartResponse.getItems().stream()
                .map(item -> new OrderItems.OrderItem(
                    item.getMenuItemId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getPrice()))
                .collect(Collectors.toList())))
            .paymentStatus(PaymentStatus.COMPLETED)
            .paymentMethodId(orderRequest.getPaymentMethodId().toString())
            .build();

        Order savedOrder = orderRepository.save(order);
        
        // 5. Clear cart
        cartClient.clearCart(orderRequest.getCartId(), orderRequest.getCustomerId());
        
        // 6. Publish order placed event
        orderEventProducer.sendOrderPlacedEvent(createOrderEvent(savedOrder, restaurant));
        
        return mapToOrderResponse(savedOrder, restaurant.getName());
    }

    @Cacheable(value = "order", key = "#orderId")
    public OrderResponse getOrder(UUID customerId, UUID orderId) {
        log.info("Getting order {} for customer: {}", orderId, customerId);
        meterRegistry.counter("order.get").increment();
        
        Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            
        // Get restaurant name
        var restaurant = restaurantClient.getRestaurantInfo(order.getRestaurantId());
        
        return mapToOrderResponse(order, restaurant.getName());
    }

    @Transactional
    @CacheEvict(value = "order", key = "#orderId")
    public OrderResponse cancelOrder(UUID customerId, UUID orderId) {
        log.info("Cancelling order {} for customer: {}", orderId, customerId);
        meterRegistry.counter("order.cancel").increment();
        
        Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!order.getStatus().equals(Order.OrderStatus.PENDING)) {
            throw new RuntimeException("Order cannot be cancelled as it's already being processed");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        
        // Get restaurant name
        var restaurant = restaurantClient.getRestaurantInfo(order.getRestaurantId());

        // Publish order cancelled event
        orderEventProducer.sendOrderCancelledEvent(createOrderEvent(updatedOrder, restaurant));

        return mapToOrderResponse(updatedOrder, restaurant.getName());
    }

    public List<OrderResponse> getOrderHistory(UUID customerId) {
        log.info("Getting order history for customer: {}", customerId);
        meterRegistry.counter("order.history").increment();
        
        return orderRepository.findByCustomerId(customerId).stream()
            .map(order -> {
                // Get restaurant name
                var restaurant = restaurantClient.getRestaurantInfo(order.getRestaurantId());
                return mapToOrderResponse(order, restaurant.getName());
            })
            .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order, String restaurantName) {
        return OrderResponse.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .restaurantId(order.getRestaurantId())
            .restaurantName(restaurantName)
            .dasherId(order.getDasherId())
            .orderTime(order.getOrderTime())
            .status(order.getStatus().name())
            .totalAmount(order.getTotalAmount())
            .items(order.getItems().getItems().stream()
                .map(item -> OrderItemResponse.builder()
                    .menuItemId(item.getMenuItemId())
                    .name(item.getName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .subtotal(item.getPrice() * item.getQuantity())
                    .build())
                .collect(Collectors.toList()))
            .paymentStatus(order.getPaymentStatus().name())
            .build();
    }

    private OrderEvent createOrderEvent(Order order, RestaurantResponse restaurant) {
        return OrderEvent.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .restaurantId(order.getRestaurantId())
            .restaurantName(restaurant.getName())
            .orderTime(order.getOrderTime())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .items(order.getItems().getItems().stream()
                .map(item -> OrderEvent.OrderItem.builder()
                    .menuItemId(item.getMenuItemId())
                    .name(item.getName())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}