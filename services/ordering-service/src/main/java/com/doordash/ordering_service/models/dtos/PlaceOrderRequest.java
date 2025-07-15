package com.doordash.ordering_service.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;
    
    @NotNull(message = "Delivery address ID is required")
    private Long deliveryAddressId;
    
    private String specialInstructions;
}