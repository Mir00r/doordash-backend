package com.doordash.delivery_service.domain.dtos.driver;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Driver information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private String status;
    private String availabilityStatus;
    private Boolean isActive;
    private Boolean isVerified;
    private Double averageRating;
    private Integer totalDeliveries;
    private Double completionRate;
    private Double totalEarnings;
    private UUID deliveryZoneId;
    private String deliveryZoneName;
    private LocationInfo currentLocation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastLocationUpdate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private Double latitude;
        private Double longitude;
        private String address;
    }
}
