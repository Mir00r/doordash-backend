package com.doordash.user_service.repositories;

import com.doordash.user_service.domain.entities.UserAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserAddress entity.
 * 
 * Provides comprehensive data access methods for user address management
 * including location-based queries and geocoding support.
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    /**
     * Find all active addresses for a user.
     * 
     * @param userId The user ID
     * @return List of active addresses
     */
    List<UserAddress> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find all addresses for a user with pagination.
     * 
     * @param userId The user ID
     * @param pageable Pagination parameters
     * @return Page of addresses
     */
    Page<UserAddress> findByUserIdAndIsActiveTrue(UUID userId, Pageable pageable);

    /**
     * Find user's default address.
     * 
     * @param userId The user ID
     * @return Optional default address
     */
    Optional<UserAddress> findByUserIdAndIsDefaultTrueAndIsActiveTrue(UUID userId);

    /**
     * Find addresses by label for a user.
     * 
     * @param userId The user ID
     * @param label Address label
     * @return List of addresses with the specified label
     */
    List<UserAddress> findByUserIdAndLabelAndIsActiveTrue(UUID userId, UserAddress.AddressLabel label);

    /**
     * Find addresses within a geographical area.
     * 
     * @param minLat Minimum latitude
     * @param maxLat Maximum latitude
     * @param minLon Minimum longitude
     * @param maxLon Maximum longitude
     * @param pageable Pagination parameters
     * @return Page of addresses within the area
     */
    @Query("SELECT ua FROM UserAddress ua WHERE ua.isActive = true AND " +
           "ua.latitude BETWEEN :minLat AND :maxLat AND " +
           "ua.longitude BETWEEN :minLon AND :maxLon")
    Page<UserAddress> findAddressesInArea(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon,
            Pageable pageable);

    /**
     * Find addresses by city and state.
     * 
     * @param city The city
     * @param state The state
     * @param pageable Pagination parameters
     * @return Page of addresses in the specified city and state
     */
    Page<UserAddress> findByCityAndStateAndIsActiveTrue(String city, String state, Pageable pageable);

    /**
     * Find addresses by postal code.
     * 
     * @param postalCode The postal code
     * @param pageable Pagination parameters
     * @return Page of addresses with the specified postal code
     */
    Page<UserAddress> findByPostalCodeAndIsActiveTrue(String postalCode, Pageable pageable);

    /**
     * Count total addresses for a user.
     * 
     * @param userId The user ID
     * @return Total count of active addresses
     */
    long countByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Check if user has any addresses.
     * 
     * @param userId The user ID
     * @return true if user has at least one active address
     */
    boolean existsByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Check if user has a default address.
     * 
     * @param userId The user ID
     * @return true if user has a default address
     */
    boolean existsByUserIdAndIsDefaultTrueAndIsActiveTrue(UUID userId);

    /**
     * Find addresses without coordinates (need geocoding).
     * 
     * @param pageable Pagination parameters
     * @return Page of addresses without coordinates
     */
    @Query("SELECT ua FROM UserAddress ua WHERE ua.isActive = true AND " +
           "(ua.latitude IS NULL OR ua.longitude IS NULL)")
    Page<UserAddress> findAddressesNeedingGeocoding(Pageable pageable);

    /**
     * Find nearby addresses using Haversine formula.
     * Note: This is an approximate calculation suitable for small distances.
     * 
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusMiles Radius in miles
     * @param pageable Pagination parameters
     * @return Page of nearby addresses
     */
    @Query(value = "SELECT ua.* FROM user_addresses ua WHERE ua.is_active = true AND " +
           "ua.latitude IS NOT NULL AND ua.longitude IS NOT NULL AND " +
           "(3958.756 * acos(cos(radians(:latitude)) * cos(radians(ua.latitude)) * " +
           "cos(radians(ua.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(ua.latitude)))) <= :radiusMiles " +
           "ORDER BY (3958.756 * acos(cos(radians(:latitude)) * cos(radians(ua.latitude)) * " +
           "cos(radians(ua.longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(ua.latitude))))",
           nativeQuery = true)
    Page<UserAddress> findNearbyAddresses(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMiles") Double radiusMiles,
            Pageable pageable);

    /**
     * Set all addresses for a user as non-default.
     * Used before setting a new default address.
     * 
     * @param userId The user ID
     * @param updatedBy Who performed the update
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false, ua.updatedAt = CURRENT_TIMESTAMP, " +
           "ua.updatedBy = :updatedBy WHERE ua.userId = :userId AND ua.isActive = true")
    int clearDefaultAddresses(@Param("userId") UUID userId, @Param("updatedBy") String updatedBy);

    /**
     * Set a specific address as default for a user.
     * 
     * @param addressId The address ID
     * @param userId The user ID
     * @param updatedBy Who performed the update
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = true, ua.updatedAt = CURRENT_TIMESTAMP, " +
           "ua.updatedBy = :updatedBy WHERE ua.id = :addressId AND ua.userId = :userId AND ua.isActive = true")
    int setDefaultAddress(
            @Param("addressId") UUID addressId, 
            @Param("userId") UUID userId, 
            @Param("updatedBy") String updatedBy);

    /**
     * Soft delete an address.
     * 
     * @param addressId The address ID
     * @param userId The user ID
     * @param updatedBy Who performed the update
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isActive = false, ua.updatedAt = CURRENT_TIMESTAMP, " +
           "ua.updatedBy = :updatedBy WHERE ua.id = :addressId AND ua.userId = :userId")
    int softDeleteAddress(
            @Param("addressId") UUID addressId, 
            @Param("userId") UUID userId, 
            @Param("updatedBy") String updatedBy);

    /**
     * Soft delete all addresses for a user.
     * 
     * @param userId The user ID
     * @param updatedBy Who performed the update
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isActive = false, ua.updatedAt = CURRENT_TIMESTAMP, " +
           "ua.updatedBy = :updatedBy WHERE ua.userId = :userId")
    int softDeleteAllUserAddresses(@Param("userId") UUID userId, @Param("updatedBy") String updatedBy);

    /**
     * Update address coordinates.
     * 
     * @param addressId The address ID
     * @param latitude The latitude
     * @param longitude The longitude
     * @param updatedBy Who performed the update
     * @return Number of affected rows
     */
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.latitude = :latitude, ua.longitude = :longitude, " +
           "ua.updatedAt = CURRENT_TIMESTAMP, ua.updatedBy = :updatedBy " +
           "WHERE ua.id = :addressId AND ua.isActive = true")
    int updateCoordinates(
            @Param("addressId") UUID addressId,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("updatedBy") String updatedBy);

    /**
     * Find addresses by user IDs (for batch operations).
     * 
     * @param userIds List of user IDs
     * @return List of addresses
     */
    List<UserAddress> findByUserIdInAndIsActiveTrue(List<UUID> userIds);

    /**
     * Get address statistics.
     * 
     * @return Address statistics
     */
    @Query("SELECT " +
           "COUNT(*) as totalAddresses, " +
           "COUNT(CASE WHEN ua.latitude IS NOT NULL AND ua.longitude IS NOT NULL THEN 1 END) as geocodedAddresses, " +
           "COUNT(CASE WHEN ua.isDefault = true THEN 1 END) as defaultAddresses, " +
           "COUNT(DISTINCT ua.userId) as usersWithAddresses " +
           "FROM UserAddress ua WHERE ua.isActive = true")
    Object[] getAddressStatistics();
}
