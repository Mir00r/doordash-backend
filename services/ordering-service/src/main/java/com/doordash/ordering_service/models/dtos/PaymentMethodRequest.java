package com.doordash.ordering_service.models.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {
    @NotBlank(message = "Payment token is required")
    private String paymentToken;
    
    @NotBlank(message = "Payment provider is required")
    private String paymentProvider;
    
    private Boolean isDefault;
    
    // These fields are needed for display purposes but actual payment processing
    // will be handled by the payment provider using the token
    @NotBlank(message = "Card last four digits are required")
    private String cardLastFour;
    
    @NotBlank(message = "Card type is required")
    private String cardType;
    
    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;
    
    @NotNull(message = "Expiry year is required")
    @Min(value = 2023, message = "Expiry year must be current year or later")
    private Integer expiryYear;
}