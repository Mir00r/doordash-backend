package com.doordash.ordering_service.services;


import com.stripe.exception.StripeException;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartService cartService;
  private final PaymentService paymentService;
  private final RestaurantService restaurantService;
  private final OrderEventProducer orderEventProducer;
  private final RedisTemplate<String, Object> redisTemplate;

  @Transactional
  @Retryable(retryFor = StripeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
  public OrderResponse placeOrder(OrderRequest orderRequest) {
    // 1. Validate and get cart
    Cart cart = cartService.validateAndGetCart(orderRequest.getCartId(), orderRequest.getCustomerId());

    // 2. Validate restaurant
    Restaurant restaurant = restaurantService.getRestaurant(cart.getRestaurantId());

    // 3. Process payment
    PaymentResult paymentResult = paymentService.processPayment(
      orderRequest.getCustomerId(),
      orderRequest.getPaymentMethodId(),
      cart.getTotalAmount()
    );

    // 4. Create order
    Order order = Order.builder()
      .customerId(orderRequest.getCustomerId())
      .restaurantId(cart.getRestaurantId())
      .orderTime(Instant.now())
      .status(Order.OrderStatus.PENDING)
      .totalAmount(cart.getTotalAmount())
      .items(new OrderItems(cart.getItems().stream()
        .map(item -> new OrderItems.OrderItem(
          item.getMenuItemId(),
          item.getName(),
          item.getQuantity(),
          item.getPrice()))
        .collect(Collectors.toList())))
      .paymentStatus(PaymentStatus.COMPLETED)
      .paymentMethodId(orderRequest.getPaymentMethodId())
      .build();

    Order savedOrder = orderRepository.save(order);

    // 5. Clear cart
    cartService.clearCart(orderRequest.getCartId());

    // 6. Publish order placed event
    orderEventProducer.sendOrderPlacedEvent(createOrderEvent(savedOrder, restaurant));

    return mapToOrderResponse(savedOrder);
  }

  @Cacheable(value = "order", key = "#orderId")
  public OrderResponse getOrder(UUID customerId, UUID orderId) {
    Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
      .orElseThrow(() -> new OrderNotFoundException(orderId));
    return mapToOrderResponse(order);
  }

  @Transactional
  @CacheEvict(value = "order", key = "#orderId")
  public OrderResponse cancelOrder(UUID customerId, UUID orderId) {
    Order order = orderRepository.findByCustomerIdAndOrderId(customerId, orderId)
      .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!order.getStatus().equals(Order.OrderStatus.PENDING)) {
      throw new OrderCancellationException("Order cannot be cancelled as it's already being processed");
    }

    order.setStatus(Order.OrderStatus.CANCELLED);
    Order updatedOrder = orderRepository.save(order);

    // Publish order cancelled event
    orderEventProducer.sendOrderCancelledEvent(createOrderEvent(updatedOrder, null));

    return mapToOrderResponse(updatedOrder);
  }

  public List<OrderResponse> getOrderHistory(UUID customerId) {
    return orderRepository.findByCustomerId(customerId).stream()
      .map(this::mapToOrderResponse)
      .collect(Collectors.toList());
  }

  private OrderResponse mapToOrderResponse(Order order) {
    return OrderResponse.builder()
      .orderId(order.getId())
      .customerId(order.getCustomerId())
      .restaurantId(order.getRestaurantId())
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
          .build())
        .collect(Collectors.toList()))
      .paymentStatus(order.getPaymentStatus().name())
      .build();
  }

  private OrderEvent createOrderEvent(Order order, Restaurant restaurant) {
    return OrderEvent.builder()
      .orderId(order.getId())
      .customerId(order.getCustomerId())
      .restaurantId(order.getRestaurantId())
      .restaurantName(restaurant != null ? restaurant.getName() : null)
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
