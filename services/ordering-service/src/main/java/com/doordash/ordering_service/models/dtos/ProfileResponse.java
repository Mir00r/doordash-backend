package com.doordash.ordering_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private UUID customerId;
    private String name;
    private String email;
    private String phone;
    private List<DeliveryAddressResponse> addresses;
    private List<PaymentMethodResponse> paymentMethods;
}