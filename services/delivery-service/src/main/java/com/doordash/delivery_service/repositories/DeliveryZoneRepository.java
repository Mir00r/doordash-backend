package com.doordash.delivery_service.repositories;

import com.doordash.delivery_service.domain.entities.DeliveryZone;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for DeliveryZone entity operations.
 */
@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, UUID> {

    /**
     * Find delivery zone by code
     */
    Optional<DeliveryZone> findByCode(String code);

    /**
     * Find delivery zones by city
     */
    List<DeliveryZone> findByCity(String city);

    /**
     * Find delivery zones by state
     */
    List<DeliveryZone> findByState(String state);

    /**
     * Find active delivery zones
     */
    List<DeliveryZone> findByIsActiveTrue();

    /**
     * Find delivery zones by status
     */
    List<DeliveryZone> findByStatus(DeliveryZone.ZoneStatus status);

    /**
     * Find operational delivery zones
     */
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true AND dz.status = 'ACTIVE'")
    List<DeliveryZone> findOperationalZones();

    /**
     * Find delivery zone containing a point using PostGIS
     */
    @Query(value = "SELECT * FROM delivery_zones dz WHERE dz.is_active = true " +
           "AND ST_Contains(dz.boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))",
           nativeQuery = true)
    Optional<DeliveryZone> findZoneContainingPoint(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude);

    /**
     * Find delivery zones within distance of a point
     */
    @Query(value = "SELECT * FROM delivery_zones dz WHERE dz.is_active = true " +
           "AND ST_DWithin(dz.boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :distanceMeters)",
           nativeQuery = true)
    List<DeliveryZone> findZonesWithinDistance(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("distanceMeters") double distanceMeters);

    /**
     * Find zones with capacity for more drivers
     */
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true " +
           "AND (dz.maxDriversCapacity IS NULL OR dz.currentActiveDrivers < dz.maxDriversCapacity)")
    List<DeliveryZone> findZonesWithCapacity();

    /**
     * Find zones by priority order
     */
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true ORDER BY dz.priority ASC")
    List<DeliveryZone> findZonesByPriority();

    /**
     * Check if code exists
     */
    boolean existsByCode(String code);
}
