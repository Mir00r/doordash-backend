package com.doordash.ordering_service.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Cart ID is required")
    private UUID cartId;
    
    @NotBlank(message = "Payment method ID is required")
    private String paymentMethodId;
    
    private String deliveryInstructions;
    
    @NotNull(message = "Delivery address ID is required")
    private UUID deliveryAddressId;
}