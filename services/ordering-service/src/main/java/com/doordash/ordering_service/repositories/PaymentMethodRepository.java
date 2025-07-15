package com.doordash.ordering_service.repositories;

import com.doordash.ordering_service.models.entities.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    List<PaymentMethod> findByCustomerId(UUID customerId);
    
    Optional<PaymentMethod> findByCustomerIdAndIsDefaultTrue(UUID customerId);
    
    @Modifying
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.customerId = :customerId AND p.id != :paymentMethodId")
    void unsetDefaultPaymentMethods(UUID customerId, UUID paymentMethodId);
}