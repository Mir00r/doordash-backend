package com.doordash.restaurant_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantHoursRequest {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Open time is required")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    private LocalTime closeTime;

    @NotNull(message = "Closed status is required")
    private Boolean isClosed;
}