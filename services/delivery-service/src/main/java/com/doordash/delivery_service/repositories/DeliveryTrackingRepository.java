package com.doordash.delivery_service.repositories;

import com.doordash.delivery_service.domain.entities.DeliveryTracking;
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
 * Repository interface for DeliveryTracking entity operations.
 */
@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, UUID> {

    /**
     * Find tracking records by delivery ID
     */
    List<DeliveryTracking> findByDeliveryId(UUID deliveryId);

    /**
     * Find latest tracking record by delivery ID
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.deliveryId = :deliveryId " +
           "ORDER BY dt.trackingTimestamp DESC LIMIT 1")
    Optional<DeliveryTracking> findLatestByDeliveryId(@Param("deliveryId") UUID deliveryId);

    /**
     * Find tracking records by driver ID
     */
    List<DeliveryTracking> findByDriverId(UUID driverId);

    /**
     * Find latest tracking record by driver ID
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.driverId = :driverId " +
           "ORDER BY dt.trackingTimestamp DESC LIMIT 1")
    Optional<DeliveryTracking> findLatestByDriverId(@Param("driverId") UUID driverId);

    /**
     * Find tracking records by status
     */
    List<DeliveryTracking> findByTrackingStatus(DeliveryTracking.TrackingStatus status);

    /**
     * Find stale tracking records
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.trackingTimestamp < :threshold")
    List<DeliveryTracking> findStaleTrackingRecords(@Param("threshold") LocalDateTime threshold);

    /**
     * Find tracking records requiring attention
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE " +
           "dt.trackingTimestamp < :staleThreshold OR " +
           "dt.routeDeviation > :deviationThreshold OR " +
           "dt.batteryLevel < :batteryThreshold OR " +
           "dt.networkStrength < :networkThreshold OR " +
           "dt.trackingStatus = 'DELIVERY_FAILED'")
    List<DeliveryTracking> findTrackingRequiringAttention(
            @Param("staleThreshold") LocalDateTime staleThreshold,
            @Param("deviationThreshold") Double deviationThreshold,
            @Param("batteryThreshold") Integer batteryThreshold,
            @Param("networkThreshold") Integer networkThreshold);

    /**
     * Find tracking records by time range
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.trackingTimestamp BETWEEN :startTime AND :endTime")
    List<DeliveryTracking> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find tracking records with route deviation
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.routeDeviation > :threshold")
    List<DeliveryTracking> findWithRouteDeviation(@Param("threshold") Double threshold);

    /**
     * Find tracking records with low battery
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.batteryLevel < :threshold")
    List<DeliveryTracking> findWithLowBattery(@Param("threshold") Integer threshold);

    /**
     * Find active delivery trackings
     */
    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.trackingStatus IN " +
           "('DRIVER_ASSIGNED', 'EN_ROUTE_TO_RESTAURANT', 'ARRIVED_AT_RESTAURANT', " +
           "'WAITING_FOR_ORDER', 'ORDER_PICKED_UP', 'EN_ROUTE_TO_CUSTOMER', " +
           "'ARRIVED_AT_DELIVERY_LOCATION', 'DELIVERY_ATTEMPTED')")
    List<DeliveryTracking> findActiveDeliveryTrackings();

    /**
     * Delete old tracking records
     */
    @Query("DELETE FROM DeliveryTracking dt WHERE dt.trackingTimestamp < :cutoffDate")
    void deleteOldTrackingRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}
