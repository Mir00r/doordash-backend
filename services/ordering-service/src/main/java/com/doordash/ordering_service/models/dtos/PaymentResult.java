package com.doordash.ordering_service.models.dtos;

import com.doordash.ordering_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private String paymentId;
    private PaymentStatus status;
    private Double amount;
    private String currency;
    private String receiptUrl;
    private String errorMessage;
}