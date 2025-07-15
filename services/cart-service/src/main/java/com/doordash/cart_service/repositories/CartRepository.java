package com.doordash.cart_service.repositories;

import com.doordash.cart_service.models.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends CrudRepository<Cart, UUID> {
    
    Optional<Cart> findByCustomerId(UUID customerId);
    
    List<Cart> findAllByCustomerId(UUID customerId);
    
    void deleteByCustomerId(UUID customerId);
}