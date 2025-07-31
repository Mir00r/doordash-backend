package com.doordash.payment.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit Log Entity
 * 
 * Comprehensive audit trail for all payment-related operations
 * to ensure compliance, security, and forensic capabilities.
 * 
 * @author DoorDash Engineering
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_log_payment_id", columnList = "payment_id"),
    @Index(name = "idx_audit_log_entity_type", columnList = "entity_type"),
    @Index(name = "idx_audit_log_action", columnList = "action"),
    @Index(name = "idx_audit_log_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_log_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @NotNull
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @NotNull
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private AuditAction action;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "old_values", columnDefinition = "jsonb")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "jsonb")
    private String newValues;

    @Column(name = "description")
    private String description;

    @Column(name = "risk_indicators", columnDefinition = "jsonb")
    private String riskIndicators;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Static factory methods for common audit log entries
    public static AuditLog paymentCreated(Payment payment, UUID userId, String ipAddress) {
        return AuditLog.builder()
            .payment(payment)
            .entityType("Payment")
            .entityId(payment.getId())
            .action(AuditAction.CREATE)
            .userId(userId)
            .ipAddress(ipAddress)
            .description("Payment created")
            .build();
    }

    public static AuditLog paymentStatusChanged(Payment payment, String oldStatus, String newStatus, UUID userId) {
        return AuditLog.builder()
            .payment(payment)
            .entityType("Payment")
            .entityId(payment.getId())
            .action(AuditAction.UPDATE)
            .userId(userId)
            .oldValues(String.format("{\"status\":\"%s\"}", oldStatus))
            .newValues(String.format("{\"status\":\"%s\"}", newStatus))
            .description(String.format("Payment status changed from %s to %s", oldStatus, newStatus))
            .build();
    }

    public static AuditLog refundCreated(Refund refund, UUID userId, String ipAddress) {
        return AuditLog.builder()
            .payment(refund.getPayment())
            .entityType("Refund")
            .entityId(refund.getId())
            .action(AuditAction.CREATE)
            .userId(userId)
            .ipAddress(ipAddress)
            .description("Refund created")
            .build();
    }

    public static AuditLog paymentMethodCreated(PaymentMethod paymentMethod, UUID userId, String ipAddress) {
        return AuditLog.builder()
            .entityType("PaymentMethod")
            .entityId(paymentMethod.getId())
            .action(AuditAction.CREATE)
            .userId(userId)
            .ipAddress(ipAddress)
            .description("Payment method created")
            .build();
    }

    public static AuditLog securityEvent(String entityType, UUID entityId, String description, UUID userId, String ipAddress, String riskIndicators) {
        return AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action(AuditAction.SECURITY_EVENT)
            .userId(userId)
            .ipAddress(ipAddress)
            .description(description)
            .riskIndicators(riskIndicators)
            .build();
    }
}

/**
 * Audit Action Enumeration
 */
enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    VIEW,
    PROCESS,
    AUTHORIZE,
    CAPTURE,
    REFUND,
    CANCEL,
    SECURITY_EVENT,
    COMPLIANCE_CHECK,
    FRAUD_DETECTION
}
