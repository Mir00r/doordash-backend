package com.doordash.ordering_service.repositories;

import com.doordash.ordering_service.enums.OrderStatus;
import com.doordash.ordering_service.models.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

  @Query("SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.orderTime DESC")
  List<Order> findByCustomerId(UUID customerId);

  @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.id = :orderId")
  Optional<Order> findByCustomerIdAndOrderId(UUID customerId, UUID orderId);

  @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.status IN :statuses")
  List<Order> findByRestaurantIdAndStatusIn(UUID restaurantId, List<OrderStatus> statuses);
}
