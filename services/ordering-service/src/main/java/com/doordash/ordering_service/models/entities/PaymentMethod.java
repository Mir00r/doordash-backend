package com.doordash.ordering_service.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_methods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "payment_provider", nullable = false)
    private String paymentProvider; // e.g., "STRIPE", "PAYPAL"
    
    @Column(name = "payment_token")
    private String paymentToken; // Token from payment provider
    
    @Column(name = "card_last_four")
    private String cardLastFour;
    
    @Column(name = "card_type")
    private String cardType; // e.g., "VISA", "MASTERCARD"
    
    @Column(name = "expiry_month")
    private Integer expiryMonth;
    
    @Column(name = "expiry_year")
    private Integer expiryYear;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (isDefault == null) {
            isDefault = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}