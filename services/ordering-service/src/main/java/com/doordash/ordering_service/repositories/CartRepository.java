package com.doordash.ordering_service.repositories;

import com.doordash.ordering_service.models.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByCustomerId(UUID customerId);
    
    void deleteByCustomerId(UUID customerId);
    
    boolean existsByCustomerIdAndRestaurantId(UUID customerId, UUID restaurantId);
}