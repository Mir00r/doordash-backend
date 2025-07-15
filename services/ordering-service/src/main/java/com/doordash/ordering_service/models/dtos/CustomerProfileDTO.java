package com.doordash.ordering_service.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private List<CustomerAddressDTO> addresses;
    private List<PaymentMethodDTO> paymentMethods;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}