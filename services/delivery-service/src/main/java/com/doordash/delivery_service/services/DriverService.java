package com.doordash.delivery_service.services;

import com.doordash.delivery_service.domain.entities.Driver;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Driver operations.
 * 
 * Provides business logic for driver management including registration,
 * verification, location tracking, and performance analytics.
 */
public interface DriverService {

    /**
     * Register a new driver
     */
    Driver registerDriver(Driver driver);

    /**
     * Update driver information
     */
    Driver updateDriver(UUID driverId, Driver driverUpdate);

    /**
     * Get driver by ID
     */
    Optional<Driver> getDriverById(UUID driverId);

    /**
     * Get driver by user ID
     */
    Optional<Driver> getDriverByUserId(UUID userId);

    /**
     * Verify driver documents and activate account
     */
    Driver verifyDriver(UUID driverId, UUID verifierId);

    /**
     * Suspend driver account
     */
    Driver suspendDriver(UUID driverId, String reason);

    /**
     * Activate driver account
     */
    Driver activateDriver(UUID driverId);

    /**
     * Update driver location
     */
    void updateDriverLocation(UUID driverId, Point location);

    /**
     * Update driver availability status
     */
    Driver updateDriverAvailability(UUID driverId, Driver.AvailabilityStatus status);

    /**
     * Find available drivers near location
     */
    List<Driver> findAvailableDriversNearLocation(double latitude, double longitude, double radiusKm);

    /**
     * Find nearest available driver
     */
    Optional<Driver> findNearestAvailableDriver(double latitude, double longitude);

    /**
     * Find available drivers in zone
     */
    List<Driver> findAvailableDriversInZone(UUID zoneId);

    /**
     * Find available drivers for delivery type
     */
    List<Driver> findAvailableDriversForDeliveryType(String deliveryType);

    /**
     * Get driver performance metrics
     */
    Driver.PerformanceMetrics getDriverPerformanceMetrics(UUID driverId);

    /**
     * Update driver rating
     */
    Driver updateDriverRating(UUID driverId, Double newRating);

    /**
     * Complete delivery and update driver stats
     */
    Driver completeDelivery(UUID driverId, UUID deliveryId, boolean successful, Double deliveryTimeMinutes);

    /**
     * Get drivers with expiring documents
     */
    List<Driver> getDriversWithExpiringDocuments(int daysAhead);

    /**
     * Get top performing drivers
     */
    Page<Driver> getTopPerformingDrivers(Double minRating, Pageable pageable);

    /**
     * Get drivers with low completion rate
     */
    List<Driver> getDriversWithLowCompletionRate(Double threshold);

    /**
     * Get drivers requiring attention
     */
    List<Driver> getDriversRequiringAttention();

    /**
     * Check if driver can accept delivery
     */
    boolean canDriverAcceptDelivery(UUID driverId, String deliveryType);

    /**
     * Assign driver to delivery zone
     */
    Driver assignDriverToZone(UUID driverId, UUID zoneId);

    /**
     * Get driver earnings for period
     */
    Double getDriverEarningsForPeriod(UUID driverId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculate driver efficiency score
     */
    Double calculateDriverEfficiencyScore(UUID driverId);

    /**
     * Get all drivers with pagination
     */
    Page<Driver> getAllDrivers(Pageable pageable);

    /**
     * Search drivers by criteria
     */
    List<Driver> searchDrivers(String searchTerm);

    /**
     * Get driver statistics
     */
    Driver.Statistics getDriverStatistics(UUID driverId);

    /**
     * Validate driver for delivery
     */
    boolean validateDriverForDelivery(UUID driverId);

    /**
     * Update driver earnings
     */
    Driver updateDriverEarnings(UUID driverId, Double amount);

    /**
     * Get drivers by status
     */
    List<Driver> getDriversByStatus(Driver.DriverStatus status);

    /**
     * Get drivers by availability
     */
    List<Driver> getDriversByAvailability(Driver.AvailabilityStatus availability);

    /**
     * Delete driver (soft delete)
     */
    void deleteDriver(UUID driverId);

    /**
     * Count active drivers in zone
     */
    Long countActiveDriversInZone(UUID zoneId);

    /**
     * Find optimal driver for delivery
     */
    Optional<Driver> findOptimalDriverForDelivery(
        double pickupLatitude, double pickupLongitude,
        double deliveryLatitude, double deliveryLongitude,
        String deliveryType, UUID zoneId);
}
