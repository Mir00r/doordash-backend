package com.doordash.delivery_service.domain.dtos.driver;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a new driver.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phoneNumber;

    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String profilePictureUrl;

    @NotBlank(message = "Driver license number is required")
    @Size(max = 50, message = "Driver license number must not exceed 50 characters")
    private String driverLicenseNumber;

    @NotNull(message = "Driver license expiry date is required")
    @Future(message = "Driver license expiry date must be in the future")
    private LocalDate driverLicenseExpiry;

    @Size(max = 100, message = "Driver license state must not exceed 100 characters")
    private String driverLicenseState;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 20, message = "SSN must not exceed 20 characters")
    private String ssn;

    @Size(max = 200, message = "Address line 1 must not exceed 200 characters")
    private String addressLine1;

    @Size(max = 200, message = "Address line 2 must not exceed 200 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Emergency contact phone must be valid")
    private String emergencyContactPhone;

    @Size(max = 100, message = "Emergency contact relationship must not exceed 100 characters")
    private String emergencyContactRelationship;

    // Vehicle Information (optional during driver registration)
    private VehicleInfo vehicleInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleInfo {
        @NotBlank(message = "Vehicle type is required")
        private String vehicleType;

        @NotBlank(message = "Vehicle make is required")
        @Size(max = 50, message = "Vehicle make must not exceed 50 characters")
        private String make;

        @NotBlank(message = "Vehicle model is required")
        @Size(max = 50, message = "Vehicle model must not exceed 50 characters")
        private String model;

        @NotNull(message = "Vehicle year is required")
        @Min(value = 1900, message = "Vehicle year must be 1900 or later")
        @Max(value = 2030, message = "Vehicle year must be 2030 or earlier")
        private Integer year;

        @Size(max = 30, message = "Vehicle color must not exceed 30 characters")
        private String color;

        @NotBlank(message = "License plate is required")
        @Size(max = 20, message = "License plate must not exceed 20 characters")
        private String licensePlate;

        @Size(max = 17, message = "VIN must not exceed 17 characters")
        private String vin;

        @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
        private String insuranceProvider;

        @Size(max = 50, message = "Insurance policy number must not exceed 50 characters")
        private String insurancePolicyNumber;

        @Future(message = "Insurance expiry date must be in the future")
        private LocalDate insuranceExpiryDate;
    }
}
