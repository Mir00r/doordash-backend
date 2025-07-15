package com.doordash.order_service.repositories;

import com.doordash.order_service.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find orders by customer ID, ordered by order time descending
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = ?1 ORDER BY o.orderTime DESC")
    List<Order> findByCustomerId(UUID customerId);

    /**
     * Find an order by customer ID and order ID
     */
    @Query("SELECT o FROM Order o WHERE o.customerId = ?1 AND o.id = ?2")
    Optional<Order> findByCustomerIdAndOrderId(UUID customerId, UUID orderId);

    /**
     * Find orders by restaurant ID and status
     */
    @Query("SELECT o FROM Order o WHERE o.restaurantId = ?1 AND o.status IN ?2")
    List<Order> findByRestaurantIdAndStatusIn(UUID restaurantId, List<Order.OrderStatus> statuses);
}