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
public class CreatePaymentMethodRequest {
    @NotBlank(message = "Payment provider is required")
    private String paymentProvider;
    
    @NotBlank(message = "Payment token is required")
    private String paymentToken;
    
    @NotNull(message = "Expiry month is required")
    @Min(value = 1, message = "Expiry month must be between 1 and 12")
    @Max(value = 12, message = "Expiry month must be between 1 and 12")
    private Integer expiryMonth;
    
    @NotNull(message = "Expiry year is required")
    @Min(value = 2023, message = "Expiry year must be current year or later")
    private Integer expiryYear;
    
    private Boolean isDefault;
}