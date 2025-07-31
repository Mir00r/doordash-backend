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
import java.util.UUID;

/**
 * Refund Entity
 * 
 * Represents a refund transaction associated with a payment.
 * Supports full and partial refunds with comprehensive audit trail.
 * 
 * @author DoorDash Engineering
 */
@Entity
@Table(name = "refunds", indexes = {
    @Index(name = "idx_refund_payment_id", columnList = "payment_id"),
    @Index(name = "idx_refund_status", columnList = "status"),
    @Index(name = "idx_refund_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

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
    private RefundStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private RefundReason reason;

    @Column(name = "description")
    private String description;

    @Column(name = "provider_refund_id")
    private String providerRefundId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "initiated_by", nullable = false)
    private UUID initiatedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // Business methods
    public boolean isSuccessful() {
        return RefundStatus.SUCCEEDED.equals(this.status);
    }

    public boolean isFailed() {
        return RefundStatus.FAILED.equals(this.status);
    }

    public boolean isPending() {
        return RefundStatus.PENDING.equals(this.status) || 
               RefundStatus.PROCESSING.equals(this.status);
    }

    public void markAsProcessing() {
        this.status = RefundStatus.PROCESSING;
    }

    public void markAsSucceeded(String providerRefundId) {
        this.status = RefundStatus.SUCCEEDED;
        this.providerRefundId = providerRefundId;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String failureReason) {
        this.status = RefundStatus.FAILED;
        this.failureReason = failureReason;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isPartialRefund() {
        return payment != null && amount.compareTo(payment.getAmount()) < 0;
    }

    public boolean isFullRefund() {
        return payment != null && amount.compareTo(payment.getAmount()) == 0;
    }
}

/**
 * Refund Status Enumeration
 */
enum RefundStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}

/**
 * Refund Reason Enumeration
 */
enum RefundReason {
    CUSTOMER_REQUEST,
    ORDER_CANCELLED,
    DUPLICATE_PAYMENT,
    FRAUDULENT,
    MERCHANT_ERROR,
    PROCESSING_ERROR,
    CHARGEBACK,
    OTHER
}
