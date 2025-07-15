package com.doordash.ordering_service.repositories;

import com.doordash.ordering_service.models.entities.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
    Optional<CustomerProfile> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
}