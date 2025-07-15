package com.doordash.ordering_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {
    private UUID id;
    private String paymentProvider;
    private String cardLastFour;
    private String cardType;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
}