package com.doordash.ordering_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {
    private Long id;
    private String paymentProvider;
    private String cardLastFour;
    private String cardType;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}