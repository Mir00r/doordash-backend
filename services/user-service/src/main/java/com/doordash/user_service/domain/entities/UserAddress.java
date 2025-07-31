package com.doordash.user_service.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserAddress entity representing user delivery and billing addresses.
 * 
 * This entity stores comprehensive address information with geocoding support
 * for precise location-based services and delivery optimization.
 * 
 * Features:
 * - Multiple address types (HOME, WORK, OTHER)
 * - Geocoding with latitude/longitude coordinates
 * - Default address management
 * - Delivery instructions and preferences
 * - Address validation and formatting
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Entity
@Table(name = "user_addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "label", nullable = false, length = 50)
    private AddressLabel label;

    @Column(name = "street_address", nullable = false)
    private String streetAddress;

    @Column(name = "apartment_number", length = 50)
    private String apartmentNumber;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Builder.Default
    @Column(name = "country", nullable = false, length = 100)
    private String country = "US";

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Enumeration for address types/labels.
     */
    public enum AddressLabel {
        HOME("Home"),
        WORK("Work"),
        OTHER("Other");

        private final String displayName;

        AddressLabel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Get the formatted address string.
     * 
     * @return A formatted address string for display
     */
    public String getFormattedAddress() {
        StringBuilder address = new StringBuilder();
        address.append(streetAddress);
        
        if (apartmentNumber != null && !apartmentNumber.trim().isEmpty()) {
            address.append(", ").append(apartmentNumber);
        }
        
        address.append(", ").append(city)
               .append(", ").append(state)
               .append(" ").append(postalCode);
        
        if (!"US".equalsIgnoreCase(country)) {
            address.append(", ").append(country);
        }
        
        return address.toString();
    }

    /**
     * Get the short formatted address for display in lists.
     * 
     * @return A short formatted address string
     */
    public String getShortFormattedAddress() {
        StringBuilder address = new StringBuilder();
        address.append(streetAddress);
        
        if (apartmentNumber != null && !apartmentNumber.trim().isEmpty()) {
            address.append(", ").append(apartmentNumber);
        }
        
        return address.toString();
    }

    /**
     * Check if the address has valid coordinates.
     * 
     * @return true if both latitude and longitude are set
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * Check if the address is complete and valid.
     * 
     * @return true if all required fields are filled
     */
    public boolean isComplete() {
        return streetAddress != null && !streetAddress.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               postalCode != null && !postalCode.trim().isEmpty() &&
               country != null && !country.trim().isEmpty();
    }

    /**
     * Calculate approximate distance to another address (if both have coordinates).
     * Uses Haversine formula for great-circle distance calculation.
     * 
     * @param other The other address to calculate distance to
     * @return Distance in miles, or null if coordinates are missing
     */
    public Double calculateDistanceToMiles(UserAddress other) {
        if (!this.hasCoordinates() || !other.hasCoordinates()) {
            return null;
        }

        double lat1 = this.latitude.doubleValue();
        double lon1 = this.longitude.doubleValue();
        double lat2 = other.latitude.doubleValue();
        double lon2 = other.longitude.doubleValue();

        final double R = 3958.756; // Earth's radius in miles

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
