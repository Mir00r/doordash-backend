package com.doordash.ordering_service.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAddressRequest {
    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;
    
    private String addressLine2;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "Zip code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zip code format")
    private String zipCode;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    private Double latitude;
    
    private Double longitude;
    
    private Boolean isDefault;
}