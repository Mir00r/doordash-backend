package com.doordash.order_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private UUID customerId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String currency;
    private String description;
}