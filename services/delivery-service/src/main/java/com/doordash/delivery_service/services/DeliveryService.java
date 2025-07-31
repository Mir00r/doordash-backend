package com.doordash.delivery_service.services;

import com.doordash.delivery_service.domain.entities.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Delivery operations.
 * 
 * Provides business logic for delivery management including creation,
 * assignment, tracking, and completion of deliveries.
 */
public interface DeliveryService {

    /**
     * Create a new delivery request
     */
    Delivery createDelivery(Delivery delivery);

    /**
     * Update delivery information
     */
    Delivery updateDelivery(UUID deliveryId, Delivery deliveryUpdate);

    /**
     * Get delivery by ID
     */
    Optional<Delivery> getDeliveryById(UUID deliveryId);

    /**
     * Get delivery by order ID
     */
    Optional<Delivery> getDeliveryByOrderId(UUID orderId);

    /**
     * Assign driver to delivery
     */
    Delivery assignDriverToDelivery(UUID deliveryId, UUID driverId);

    /**
     * Auto-assign driver to delivery
     */
    Delivery autoAssignDriverToDelivery(UUID deliveryId);

    /**
     * Update delivery status
     */
    Delivery updateDeliveryStatus(UUID deliveryId, Delivery.DeliveryStatus status);

    /**
     * Start pickup process
     */
    Delivery startPickup(UUID deliveryId);

    /**
     * Complete pickup
     */
    Delivery completePickup(UUID deliveryId);

    /**
     * Start delivery to customer
     */
    Delivery startDelivery(UUID deliveryId);

    /**
     * Mark as arrived at delivery location
     */
    Delivery markArrivedAtDeliveryLocation(UUID deliveryId);

    /**
     * Complete delivery
     */
    Delivery completeDelivery(UUID deliveryId, String deliveryProof);

    /**
     * Mark delivery as failed
     */
    Delivery markDeliveryFailed(UUID deliveryId, String reason);

    /**
     * Cancel delivery
     */
    Delivery cancelDelivery(UUID deliveryId, String reason);

    /**
     * Get deliveries by customer
     */
    List<Delivery> getDeliveriesByCustomer(UUID customerId);

    /**
     * Get deliveries by driver
     */
    List<Delivery> getDeliveriesByDriver(UUID driverId);

    /**
     * Get deliveries by restaurant
     */
    List<Delivery> getDeliveriesByRestaurant(UUID restaurantId);

    /**
     * Get active deliveries for driver
     */
    List<Delivery> getActiveDeliveriesForDriver(UUID driverId);

    /**
     * Get pending deliveries
     */
    List<Delivery> getPendingDeliveries();

    /**
     * Get pending deliveries in zone
     */
    List<Delivery> getPendingDeliveriesInZone(UUID zoneId);

    /**
     * Get overdue deliveries
     */
    List<Delivery> getOverdueDeliveries();

    /**
     * Get deliveries in progress
     */
    List<Delivery> getDeliveriesInProgress();

    /**
     * Calculate delivery fee
     */
    Double calculateDeliveryFee(UUID deliveryId);

    /**
     * Estimate delivery time
     */
    Integer estimateDeliveryTime(double pickupLat, double pickupLon, double deliveryLat, double deliveryLon);

    /**
     * Find optimal delivery route
     */
    List<UUID> findOptimalDeliveryRoute(List<UUID> deliveryIds);

    /**
     * Reassign delivery to different driver
     */
    Delivery reassignDelivery(UUID deliveryId, UUID newDriverId, String reason);

    /**
     * Get delivery statistics for period
     */
    Delivery.DeliveryStatistics getDeliveryStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get delivery analytics by zone
     */
    List<Delivery.ZoneAnalytics> getDeliveryAnalyticsByZone(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Rate delivery
     */
    Delivery rateDelivery(UUID deliveryId, Integer customerRating, String customerFeedback);

    /**
     * Get deliveries requiring customer contact
     */
    List<Delivery> getDeliveriesRequiringCustomerContact();

    /**
     * Get high priority deliveries
     */
    List<Delivery> getHighPriorityDeliveries();

    /**
     * Get express deliveries
     */
    List<Delivery> getExpressDeliveries();

    /**
     * Check if customer has active deliveries
     */
    boolean hasActiveDeliveries(UUID customerId);

    /**
     * Check if driver has active deliveries
     */
    boolean driverHasActiveDeliveries(UUID driverId);

    /**
     * Get deliveries with special requirements
     */
    List<Delivery> getDeliveriesWithSpecialRequirements();

    /**
     * Update delivery ETA
     */
    Delivery updateDeliveryETA(UUID deliveryId, LocalDateTime newETA);

    /**
     * Get delivery performance metrics
     */
    Delivery.PerformanceMetrics getDeliveryPerformanceMetrics(UUID driverId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Schedule delivery
     */
    Delivery scheduleDelivery(UUID deliveryId, LocalDateTime scheduledTime);

    /**
     * Get all deliveries with pagination
     */
    Page<Delivery> getAllDeliveries(Pageable pageable);

    /**
     * Search deliveries by criteria
     */
    List<Delivery> searchDeliveries(String searchTerm);

    /**
     * Get recent deliveries for customer
     */
    Page<Delivery> getRecentDeliveriesForCustomer(UUID customerId, Pageable pageable);

    /**
     * Validate delivery request
     */
    boolean validateDeliveryRequest(Delivery delivery);

    /**
     * Calculate driver payout for delivery
     */
    Double calculateDriverPayout(UUID deliveryId);

    /**
     * Process delivery payment
     */
    Delivery processDeliveryPayment(UUID deliveryId);

    /**
     * Get deliveries needing reassignment
     */
    List<Delivery> getDeliveriesNeedingReassignment();

    /**
     * Bulk update delivery status
     */
    List<Delivery> bulkUpdateDeliveryStatus(List<UUID> deliveryIds, Delivery.DeliveryStatus status);

    /**
     * Get delivery insights and recommendations
     */
    Delivery.DeliveryInsights getDeliveryInsights(UUID restaurantId, LocalDateTime startDate, LocalDateTime endDate);
}
