package com.doordash.delivery_service.repositories;

import com.doordash.delivery_service.domain.entities.Driver;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Driver entity operations.
 * 
 * Provides data access methods for driver management including
 * geospatial queries, availability tracking, and performance analytics.
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    /**
     * Find driver by user ID (auth service reference)
     */
    Optional<Driver> findByUserId(UUID userId);

    /**
     * Find driver by phone number
     */
    Optional<Driver> findByPhoneNumber(String phoneNumber);

    /**
     * Find driver by driver license number
     */
    Optional<Driver> findByDriverLicenseNumber(String driverLicenseNumber);

    /**
     * Find all active drivers
     */
    List<Driver> findByIsActiveTrue();

    /**
     * Find all verified drivers
     */
    List<Driver> findByIsVerifiedTrue();

    /**
     * Find drivers by status
     */
    List<Driver> findByStatus(Driver.DriverStatus status);

    /**
     * Find drivers by availability status
     */
    List<Driver> findByAvailabilityStatus(Driver.AvailabilityStatus availabilityStatus);

    /**
     * Find available drivers in a specific zone
     */
    @Query("SELECT d FROM Driver d WHERE d.isActive = true AND d.isVerified = true " +
           "AND d.availabilityStatus = 'AVAILABLE' AND d.deliveryZoneId = :zoneId")
    List<Driver> findAvailableDriversInZone(@Param("zoneId") UUID zoneId);

    /**
     * Find drivers within a radius of a location using PostGIS
     */
    @Query(value = "SELECT * FROM drivers d WHERE d.is_active = true AND d.is_verified = true " +
           "AND d.availability_status = 'AVAILABLE' " +
           "AND ST_DWithin(d.current_location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusMeters) " +
           "ORDER BY ST_Distance(d.current_location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))",
           nativeQuery = true)
    List<Driver> findAvailableDriversWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters);

    /**
     * Find nearest available driver
     */
    @Query(value = "SELECT * FROM drivers d WHERE d.is_active = true AND d.is_verified = true " +
           "AND d.availability_status = 'AVAILABLE' " +
           "ORDER BY ST_Distance(d.current_location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) " +
           "LIMIT 1",
           nativeQuery = true)
    Optional<Driver> findNearestAvailableDriver(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude);

    /**
     * Find drivers with expiring documents
     */
    @Query("SELECT d FROM Driver d WHERE d.driverLicenseExpiry <= :expiryDate OR " +
           "d.backgroundCheckExpiry <= :expiryDate")
    List<Driver> findDriversWithExpiringDocuments(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find drivers by vehicle type
     */
    @Query("SELECT d FROM Driver d JOIN Vehicle v ON d.id = v.driverId " +
           "WHERE v.vehicleType = :vehicleType AND v.isActive = true")
    List<Driver> findDriversByVehicleType(@Param("vehicleType") String vehicleType);

    /**
     * Find top performing drivers by rating
     */
    @Query("SELECT d FROM Driver d WHERE d.averageRating >= :minRating " +
           "ORDER BY d.averageRating DESC")
    Page<Driver> findTopPerformingDrivers(@Param("minRating") Double minRating, Pageable pageable);

    /**
     * Find drivers with low completion rate
     */
    @Query("SELECT d FROM Driver d WHERE d.completionRate < :threshold")
    List<Driver> findDriversWithLowCompletionRate(@Param("threshold") Double threshold);

    /**
     * Count active drivers in zone
     */
    @Query("SELECT COUNT(d) FROM Driver d WHERE d.deliveryZoneId = :zoneId " +
           "AND d.isActive = true AND d.availabilityStatus = 'AVAILABLE'")
    Long countActiveDriversInZone(@Param("zoneId") UUID zoneId);

    /**
     * Find drivers who haven't updated location recently
     */
    @Query("SELECT d FROM Driver d WHERE d.lastLocationUpdate < :threshold AND d.isActive = true")
    List<Driver> findDriversWithStaleLocation(@Param("threshold") LocalDateTime threshold);

    /**
     * Find drivers by earnings range
     */
    @Query("SELECT d FROM Driver d WHERE d.totalEarnings BETWEEN :minEarnings AND :maxEarnings")
    List<Driver> findDriversByEarningsRange(
            @Param("minEarnings") Double minEarnings,
            @Param("maxEarnings") Double maxEarnings);

    /**
     * Find drivers available for specific delivery type
     */
    @Query("SELECT d FROM Driver d JOIN Vehicle v ON d.id = v.driverId " +
           "WHERE d.isActive = true AND d.isVerified = true AND d.availabilityStatus = 'AVAILABLE' " +
           "AND v.isActive = true AND v.status = 'ACTIVE' " +
           "AND ((:deliveryType = 'ALCOHOL' AND v.vehicleType IN ('CAR', 'VAN', 'TRUCK')) OR " +
           "(:deliveryType = 'LARGE_ORDER' AND v.vehicleType IN ('CAR', 'VAN', 'TRUCK')) OR " +
           "(:deliveryType = 'EXPRESS' AND v.vehicleType IN ('MOTORCYCLE', 'SCOOTER', 'BICYCLE')) OR " +
           "(:deliveryType = 'STANDARD'))")
    List<Driver> findAvailableDriversForDeliveryType(@Param("deliveryType") String deliveryType);

    /**
     * Update driver location
     */
    @Query("UPDATE Driver d SET d.currentLocation = :location, d.lastLocationUpdate = :timestamp " +
           "WHERE d.id = :driverId")
    void updateDriverLocation(
            @Param("driverId") UUID driverId,
            @Param("location") Point location,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * Update driver availability status
     */
    @Query("UPDATE Driver d SET d.availabilityStatus = :status WHERE d.id = :driverId")
    void updateDriverAvailability(@Param("driverId") UUID driverId, @Param("status") Driver.AvailabilityStatus status);

    /**
     * Find drivers by delivery zone with pagination
     */
    Page<Driver> findByDeliveryZoneId(UUID zoneId, Pageable pageable);

    /**
     * Find drivers by rating range
     */
    List<Driver> findByAverageRatingBetween(Double minRating, Double maxRating);

    /**
     * Check if driver exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Check if driver exists by phone number
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check if driver exists by license number
     */
    boolean existsByDriverLicenseNumber(String driverLicenseNumber);
}
