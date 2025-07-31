package com.doordash.payment.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Payment Entity
 * 
 * Represents a payment transaction in the system with full audit trail,
 * security features, and comprehensive metadata tracking.
 * 
 * @author DoorDash Engineering
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_order_id", columnList = "order_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_provider", columnList = "provider"),
    @Index(name = "idx_payment_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @NotNull
    @Column(name = "payment_method_id", nullable = false)
    private UUID paymentMethodId;

    @NotNull
    @Positive
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private PaymentProvider provider;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "provider_payment_intent_id")
    private String providerPaymentIntentId;

    @Column(name = "description")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_count", columnDefinition = "integer default 0")
    private Integer retryCount = 0;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "risk_score", precision = 3, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "is_test", columnDefinition = "boolean default false")
    private Boolean isTest = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Relationships
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Refund> refunds = new ArrayList<>();

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AuditLog> auditLogs = new ArrayList<>();

    // Business methods
    public boolean isSuccessful() {
        return PaymentStatus.SUCCEEDED.equals(this.status);
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED.equals(this.status);
    }

    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status) || 
               PaymentStatus.PROCESSING.equals(this.status);
    }

    public boolean isRefundable() {
        return isSuccessful() && 
               (settledAt == null || settledAt.isAfter(LocalDateTime.now().minusHours(72)));
    }

    public BigDecimal getRefundableAmount() {
        if (!isRefundable()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalRefunded = refunds.stream()
            .filter(refund -> RefundStatus.SUCCEEDED.equals(refund.getStatus()))
            .map(Refund::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return amount.subtract(totalRefunded);
    }

    public void markAsProcessing() {
        this.status = PaymentStatus.PROCESSING;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsSucceeded(String providerTransactionId) {
        this.status = PaymentStatus.SUCCEEDED;
        this.providerTransactionId = providerTransactionId;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsSettled() {
        this.settledAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null) ? 1 : this.retryCount + 1;
    }
}

/**
 * Payment Status Enumeration
 */
enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIALLY_REFUNDED
}

/**
 * Payment Provider Enumeration
 */
enum PaymentProvider {
    STRIPE,
    PAYPAL,
    BRAINTREE,
    APPLE_PAY,
    GOOGLE_PAY
}
