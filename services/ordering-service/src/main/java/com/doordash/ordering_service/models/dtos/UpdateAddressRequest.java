package com.doordash.ordering_service.models.dtos;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressRequest {
    private String addressLine1;
    
    private String addressLine2;
    
    private String city;
    
    private String state;
    
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zip code format")
    private String zipCode;
    
    private String country;
    
    private Double latitude;
    
    private Double longitude;
    
    private Boolean isDefault;
}