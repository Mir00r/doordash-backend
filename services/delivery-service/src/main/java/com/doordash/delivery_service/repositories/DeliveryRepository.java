package com.doordash.delivery_service.repositories;

import com.doordash.delivery_service.domain.entities.Delivery;
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
 * Repository interface for Delivery entity operations.
 * 
 * Provides data access methods for delivery management including
 * status tracking, driver assignments, and delivery analytics.
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    /**
     * Find delivery by order ID
     */
    Optional<Delivery> findByOrderId(UUID orderId);

    /**
     * Find deliveries by customer ID
     */
    List<Delivery> findByCustomerId(UUID customerId);

    /**
     * Find deliveries by driver ID
     */
    List<Delivery> findByDriverId(UUID driverId);

    /**
     * Find deliveries by restaurant ID
     */
    List<Delivery> findByRestaurantId(UUID restaurantId);

    /**
     * Find deliveries by status
     */
    List<Delivery> findByStatus(Delivery.DeliveryStatus status);

    /**
     * Find deliveries by delivery zone
     */
    List<Delivery> findByDeliveryZoneId(UUID deliveryZoneId);

    /**
     * Find active deliveries for a driver
     */
    @Query("SELECT d FROM Delivery d WHERE d.driverId = :driverId AND d.status IN " +
           "('ASSIGNED', 'PICKUP_IN_PROGRESS', 'PICKED_UP', 'EN_ROUTE')")
    List<Delivery> findActiveDeliveriesForDriver(@Param("driverId") UUID driverId);

    /**
     * Find pending deliveries (waiting for driver assignment)
     */
    @Query("SELECT d FROM Delivery d WHERE d.status = 'PENDING' AND d.driverId IS NULL " +
           "ORDER BY d.requestedDeliveryTime ASC")
    List<Delivery> findPendingDeliveries();

    /**
     * Find pending deliveries in a specific zone
     */
    @Query("SELECT d FROM Delivery d WHERE d.status = 'PENDING' AND d.driverId IS NULL " +
           "AND d.deliveryZoneId = :zoneId ORDER BY d.requestedDeliveryTime ASC")
    List<Delivery> findPendingDeliveriesInZone(@Param("zoneId") UUID zoneId);

    /**
     * Find overdue deliveries
     */
    @Query("SELECT d FROM Delivery d WHERE d.estimatedDeliveryTime < :currentTime " +
           "AND d.status NOT IN ('DELIVERED', 'CANCELLED', 'FAILED')")
    List<Delivery> findOverdueDeliveries(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find deliveries in progress
     */
    @Query("SELECT d FROM Delivery d WHERE d.status IN " +
           "('ASSIGNED', 'PICKUP_IN_PROGRESS', 'PICKED_UP', 'EN_ROUTE', 'ARRIVED')")
    List<Delivery> findDeliveriesInProgress();

    /**
     * Find deliveries by date range
     */
    @Query("SELECT d FROM Delivery d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<Delivery> findDeliveriesByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find deliveries with customer rating
     */
    @Query("SELECT d FROM Delivery d WHERE d.customerRating IS NOT NULL")
    List<Delivery> findDeliveriesWithCustomerRating();

    /**
     * Find deliveries by customer rating range
     */
    @Query("SELECT d FROM Delivery d WHERE d.customerRating BETWEEN :minRating AND :maxRating")
    List<Delivery> findDeliveriesByRatingRange(
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating);

    /**
     * Find high-priority deliveries
     */
    @Query("SELECT d FROM Delivery d WHERE d.priority = 'HIGH' AND d.status NOT IN ('DELIVERED', 'CANCELLED', 'FAILED')")
    List<Delivery> findHighPriorityDeliveries();

    /**
     * Find express deliveries
     */
    @Query("SELECT d FROM Delivery d WHERE d.deliveryType = 'EXPRESS'")
    List<Delivery> findExpressDeliveries();

    /**
     * Find deliveries by driver and status
     */
    List<Delivery> findByDriverIdAndStatus(UUID driverId, Delivery.DeliveryStatus status);

    /**
     * Find deliveries by customer and status
     */
    List<Delivery> findByCustomerIdAndStatus(UUID customerId, Delivery.DeliveryStatus status);

    /**
     * Count deliveries by driver
     */
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driverId = :driverId")
    Long countDeliveriesByDriver(@Param("driverId") UUID driverId);

    /**
     * Count completed deliveries by driver
     */
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driverId = :driverId AND d.status = 'DELIVERED'")
    Long countCompletedDeliveriesByDriver(@Param("driverId") UUID driverId);

    /**
     * Count failed deliveries by driver
     */
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driverId = :driverId AND d.status = 'FAILED'")
    Long countFailedDeliveriesByDriver(@Param("driverId") UUID driverId);

    /**
     * Find deliveries with long delivery times
     */
    @Query("SELECT d FROM Delivery d WHERE d.actualDeliveryTime IS NOT NULL " +
           "AND d.pickupCompletedTime IS NOT NULL " +
           "AND EXTRACT(EPOCH FROM (d.actualDeliveryTime - d.pickupCompletedTime))/60 > :thresholdMinutes")
    List<Delivery> findDeliveriesWithLongDeliveryTimes(@Param("thresholdMinutes") Double thresholdMinutes);

    /**
     * Calculate average delivery time by zone
     */
    @Query("SELECT d.deliveryZoneId, AVG(EXTRACT(EPOCH FROM (d.actualDeliveryTime - d.pickupCompletedTime))/60) " +
           "FROM Delivery d WHERE d.actualDeliveryTime IS NOT NULL AND d.pickupCompletedTime IS NOT NULL " +
           "AND d.deliveryZoneId IS NOT NULL GROUP BY d.deliveryZoneId")
    List<Object[]> getAverageDeliveryTimeByZone();

    /**
     * Find deliveries requiring customer contact
     */
    @Query("SELECT d FROM Delivery d WHERE d.status = 'ARRIVED' " +
           "AND d.arrivalTime < :thresholdTime")
    List<Delivery> findDeliveriesRequiringCustomerContact(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Find deliveries by payment status
     */
    @Query("SELECT d FROM Delivery d WHERE d.paymentStatus = :paymentStatus")
    List<Delivery> findDeliveriesByPaymentStatus(@Param("paymentStatus") String paymentStatus);

    /**
     * Find recent deliveries for customer
     */
    @Query("SELECT d FROM Delivery d WHERE d.customerId = :customerId " +
           "ORDER BY d.createdAt DESC")
    Page<Delivery> findRecentDeliveriesForCustomer(@Param("customerId") UUID customerId, Pageable pageable);

    /**
     * Find deliveries with special requirements
     */
    @Query("SELECT d FROM Delivery d WHERE d.specialRequirements IS NOT NULL " +
           "AND JSON_EXTRACT(d.specialRequirements, '$') != '{}'")
    List<Delivery> findDeliveriesWithSpecialRequirements();

    /**
     * Update delivery status
     */
    @Query("UPDATE Delivery d SET d.status = :status, d.updatedAt = :timestamp " +
           "WHERE d.id = :deliveryId")
    void updateDeliveryStatus(
            @Param("deliveryId") UUID deliveryId,
            @Param("status") Delivery.DeliveryStatus status,
            @Param("timestamp") LocalDateTime timestamp);

    /**
     * Assign driver to delivery
     */
    @Query("UPDATE Delivery d SET d.driverId = :driverId, d.status = 'ASSIGNED', " +
           "d.driverAssignedTime = :assignmentTime WHERE d.id = :deliveryId")
    void assignDriverToDelivery(
            @Param("deliveryId") UUID deliveryId,
            @Param("driverId") UUID driverId,
            @Param("assignmentTime") LocalDateTime assignmentTime);

    /**
     * Find deliveries needing reassignment
     */
    @Query("SELECT d FROM Delivery d WHERE d.driverId IS NOT NULL " +
           "AND d.status = 'ASSIGNED' " +
           "AND d.driverAssignedTime < :thresholdTime")
    List<Delivery> findDeliveriesNeedingReassignment(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * Check if customer has any active deliveries
     */
    @Query("SELECT COUNT(d) > 0 FROM Delivery d WHERE d.customerId = :customerId " +
           "AND d.status IN ('PENDING', 'ASSIGNED', 'PICKUP_IN_PROGRESS', 'PICKED_UP', 'EN_ROUTE', 'ARRIVED')")
    boolean hasActiveDeliveries(@Param("customerId") UUID customerId);

    /**
     * Check if driver has any active deliveries
     */
    @Query("SELECT COUNT(d) > 0 FROM Delivery d WHERE d.driverId = :driverId " +
           "AND d.status IN ('ASSIGNED', 'PICKUP_IN_PROGRESS', 'PICKED_UP', 'EN_ROUTE', 'ARRIVED')")
    boolean driverHasActiveDeliveries(@Param("driverId") UUID driverId);

    /**
     * Find delivery statistics by date range
     */
    @Query("SELECT " +
           "COUNT(d) as totalDeliveries, " +
           "COUNT(CASE WHEN d.status = 'DELIVERED' THEN 1 END) as completedDeliveries, " +
           "COUNT(CASE WHEN d.status = 'FAILED' THEN 1 END) as failedDeliveries, " +
           "COUNT(CASE WHEN d.status = 'CANCELLED' THEN 1 END) as cancelledDeliveries, " +
           "AVG(CASE WHEN d.actualDeliveryTime IS NOT NULL AND d.pickupCompletedTime IS NOT NULL " +
           "THEN EXTRACT(EPOCH FROM (d.actualDeliveryTime - d.pickupCompletedTime))/60 END) as avgDeliveryTimeMinutes " +
           "FROM Delivery d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    Object getDeliveryStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
