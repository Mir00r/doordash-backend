package com.doordash.order_service.models.dtos;

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

    private UUID customerId; // Set by the controller from JWT

    @NotNull(message = "Payment method ID is required")
    private UUID paymentMethodId;

    @NotNull(message = "Cart ID is required")
    private UUID cartId;
}