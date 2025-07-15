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
public class SearchRequest {
    private String terms;
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    @Builder.Default
    private Double radiusMiles = 10.0;
    
    private String cuisineType;
    
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
}