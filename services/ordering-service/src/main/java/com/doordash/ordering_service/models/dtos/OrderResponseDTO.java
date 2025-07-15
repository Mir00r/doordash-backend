package com.doordash.ordering_service.models.dtos;

import com.doordash.ordering_service.enums.OrderStatus;
import com.doordash.ordering_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private String restaurantName;
    private Long dasherId;
    private LocalDateTime orderTime;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private PaymentStatus paymentStatus;
    private Long paymentMethodId;
    private CustomerAddressDTO deliveryAddress;
    private String specialInstructions;
    private LocalDateTime estimatedDeliveryTime;
}