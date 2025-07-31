package com.doordash.delivery_service.controllers;

import com.doordash.delivery_service.domain.dtos.driver.CreateDriverRequest;
import com.doordash.delivery_service.domain.dtos.driver.DriverResponse;
import com.doordash.delivery_service.domain.entities.Driver;
import com.doordash.delivery_service.services.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Driver management operations.
 * 
 * Provides endpoints for driver registration, verification, location tracking,
 * and performance analytics for delivery drivers.
 */
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver Management", description = "Operations related to delivery driver management")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    private final DriverService driverService;

    @Operation(summary = "Register a new driver", description = "Register a new delivery driver with required documentation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Driver registered successfully",
                content = @Content(schema = @Schema(implementation = DriverResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Driver already exists")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER')")
    public ResponseEntity<DriverResponse> registerDriver(
            @Valid @RequestBody CreateDriverRequest request) {
        log.info("Registering new driver for user ID: {}", request.getUserId());
        
        // Convert request to entity
        Driver driver = mapRequestToEntity(request);
        Driver savedDriver = driverService.registerDriver(driver);
        DriverResponse response = mapEntityToResponse(savedDriver);
        
        log.info("Driver registered successfully with ID: {}", savedDriver.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get driver by ID", description = "Retrieve driver information by driver ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver found",
                content = @Content(schema = @Schema(implementation = DriverResponse.class))),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/{driverId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER') or (hasRole('DRIVER') and @driverAuthService.isCurrentDriver(#driverId))")
    public ResponseEntity<DriverResponse> getDriverById(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        log.info("Fetching driver with ID: {}", driverId);
        
        return driverService.getDriverById(driverId)
                .map(this::mapEntityToResponse)
                .map(response -> ResponseEntity.ok(response))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all drivers", description = "Retrieve all drivers with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Drivers retrieved successfully")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER')")
    public ResponseEntity<Page<DriverResponse>> getAllDrivers(Pageable pageable) {
        log.info("Fetching all drivers with pagination: {}", pageable);
        
        Page<Driver> drivers = driverService.getAllDrivers(pageable);
        Page<DriverResponse> response = drivers.map(this::mapEntityToResponse);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update driver information", description = "Update driver profile and settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver updated successfully",
                content = @Content(schema = @Schema(implementation = DriverResponse.class))),
        @ApiResponse(responseCode = "404", description = "Driver not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PutMapping("/{driverId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER') or (hasRole('DRIVER') and @driverAuthService.isCurrentDriver(#driverId))")
    public ResponseEntity<DriverResponse> updateDriver(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId,
            @Valid @RequestBody CreateDriverRequest request) {
        log.info("Updating driver with ID: {}", driverId);
        
        Driver driverUpdate = mapRequestToEntity(request);
        Driver updatedDriver = driverService.updateDriver(driverId, driverUpdate);
        DriverResponse response = mapEntityToResponse(updatedDriver);
        
        log.info("Driver updated successfully: {}", driverId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify driver", description = "Verify driver documents and activate account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver verified successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PostMapping("/{driverId}/verify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER')")
    public ResponseEntity<DriverResponse> verifyDriver(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId,
            @Parameter(description = "Verifier ID") @RequestParam UUID verifierId) {
        log.info("Verifying driver: {} by verifier: {}", driverId, verifierId);
        
        Driver verifiedDriver = driverService.verifyDriver(driverId, verifierId);
        DriverResponse response = mapEntityToResponse(verifiedDriver);
        
        log.info("Driver verified successfully: {}", driverId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update driver availability", description = "Update driver availability status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability updated successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PutMapping("/{driverId}/availability")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER') or (hasRole('DRIVER') and @driverAuthService.isCurrentDriver(#driverId))")
    public ResponseEntity<DriverResponse> updateDriverAvailability(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId,
            @Parameter(description = "Availability Status") @RequestParam String status) {
        log.info("Updating driver availability: {} to status: {}", driverId, status);
        
        Driver.AvailabilityStatus availabilityStatus = Driver.AvailabilityStatus.valueOf(status.toUpperCase());
        Driver updatedDriver = driverService.updateDriverAvailability(driverId, availabilityStatus);
        DriverResponse response = mapEntityToResponse(updatedDriver);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update driver location", description = "Update driver's current location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PutMapping("/{driverId}/location")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER') or (hasRole('DRIVER') and @driverAuthService.isCurrentDriver(#driverId))")
    public ResponseEntity<Void> updateDriverLocation(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId,
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude) {
        log.info("Updating driver location: {} to lat: {}, lon: {}", driverId, latitude, longitude);
        
        // Create Point from coordinates (simplified - in real implementation use PostGIS utilities)
        // Point location = GeometryUtils.createPoint(longitude, latitude);
        // driverService.updateDriverLocation(driverId, location);
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Find available drivers near location", description = "Find available drivers within radius of location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Drivers found successfully")
    })
    @GetMapping("/available/near")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER') or hasRole('DISPATCHER')")
    public ResponseEntity<List<DriverResponse>> findAvailableDriversNearLocation(
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude,
            @Parameter(description = "Radius in kilometers") @RequestParam(defaultValue = "10.0") Double radiusKm) {
        log.info("Finding available drivers near lat: {}, lon: {} within {} km", latitude, longitude, radiusKm);
        
        List<Driver> drivers = driverService.findAvailableDriversNearLocation(latitude, longitude, radiusKm);
        List<DriverResponse> response = drivers.stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Find drivers in delivery zone", description = "Find available drivers in specific delivery zone")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Drivers found successfully")
    })
    @GetMapping("/available/zone/{zoneId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER') or hasRole('DISPATCHER')")
    public ResponseEntity<List<DriverResponse>> findAvailableDriversInZone(
            @Parameter(description = "Zone ID") @PathVariable UUID zoneId) {
        log.info("Finding available drivers in zone: {}", zoneId);
        
        List<Driver> drivers = driverService.findAvailableDriversInZone(zoneId);
        List<DriverResponse> response = drivers.stream()
                .map(this::mapEntityToResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get driver performance metrics", description = "Get driver performance and analytics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @GetMapping("/{driverId}/metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER') or (hasRole('DRIVER') and @driverAuthService.isCurrentDriver(#driverId))")
    public ResponseEntity<Driver.PerformanceMetrics> getDriverPerformanceMetrics(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        log.info("Fetching performance metrics for driver: {}", driverId);
        
        Driver.PerformanceMetrics metrics = driverService.getDriverPerformanceMetrics(driverId);
        return ResponseEntity.ok(metrics);
    }

    @Operation(summary = "Suspend driver", description = "Suspend driver account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver suspended successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PostMapping("/{driverId}/suspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER')")
    public ResponseEntity<DriverResponse> suspendDriver(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId,
            @Parameter(description = "Suspension reason") @RequestParam String reason) {
        log.info("Suspending driver: {} with reason: {}", driverId, reason);
        
        Driver suspendedDriver = driverService.suspendDriver(driverId, reason);
        DriverResponse response = mapEntityToResponse(suspendedDriver);
        
        log.info("Driver suspended successfully: {}", driverId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate driver", description = "Activate suspended driver account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver activated successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @PostMapping("/{driverId}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DRIVER_MANAGER')")
    public ResponseEntity<DriverResponse> activateDriver(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        log.info("Activating driver: {}", driverId);
        
        Driver activatedDriver = driverService.activateDriver(driverId);
        DriverResponse response = mapEntityToResponse(activatedDriver);
        
        log.info("Driver activated successfully: {}", driverId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete driver", description = "Soft delete driver account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Driver deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Driver not found")
    })
    @DeleteMapping("/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDriver(
            @Parameter(description = "Driver ID") @PathVariable UUID driverId) {
        log.info("Deleting driver: {}", driverId);
        
        driverService.deleteDriver(driverId);
        
        log.info("Driver deleted successfully: {}", driverId);
        return ResponseEntity.noContent().build();
    }

    // Utility methods for mapping between DTOs and entities
    private Driver mapRequestToEntity(CreateDriverRequest request) {
        return Driver.builder()
                .userId(request.getUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profilePictureUrl(request.getProfilePictureUrl())
                .driverLicenseNumber(request.getDriverLicenseNumber())
                .driverLicenseExpiry(request.getDriverLicenseExpiry())
                .driverLicenseState(request.getDriverLicenseState())
                .dateOfBirth(request.getDateOfBirth())
                .ssn(request.getSsn())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .emergencyContactRelationship(request.getEmergencyContactRelationship())
                .build();
    }

    private DriverResponse mapEntityToResponse(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .email(driver.getEmail())
                .phoneNumber(driver.getPhoneNumber())
                .profilePictureUrl(driver.getProfilePictureUrl())
                .status(driver.getStatus() != null ? driver.getStatus().name() : null)
                .availabilityStatus(driver.getAvailabilityStatus() != null ? driver.getAvailabilityStatus().name() : null)
                .isActive(driver.getIsActive())
                .isVerified(driver.getIsVerified())
                .averageRating(driver.getAverageRating())
                .totalDeliveries(driver.getTotalDeliveries())
                .completionRate(driver.getCompletionRate())
                .totalEarnings(driver.getTotalEarnings())
                .deliveryZoneId(driver.getDeliveryZoneId())
                .lastLocationUpdate(driver.getLastLocationUpdate())
                .createdAt(driver.getCreatedAt())
                // Add current location if available
                .currentLocation(driver.getCurrentLocation() != null ? 
                    DriverResponse.LocationInfo.builder()
                        .latitude(driver.getCurrentLocation().getY())
                        .longitude(driver.getCurrentLocation().getX())
                        .build() : null)
                .build();
    }
}
