package com.doordash.ordering_service.repositories;

import com.doordash.ordering_service.models.entities.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByCustomerId(UUID customerId);
    
    Optional<CustomerAddress> findByCustomerIdAndIsDefaultTrue(UUID customerId);
    
    @Modifying
    @Query("UPDATE CustomerAddress a SET a.isDefault = false WHERE a.customerId = :customerId AND a.id != :addressId")
    void unsetDefaultAddresses(UUID customerId, UUID addressId);
}