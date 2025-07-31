package com.doordash.payment.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Method Entity
 * 
 * Represents a stored payment method (credit card, bank account, digital wallet)
 * with PCI DSS compliant tokenization and secure storage.
 * 
 * @author DoorDash Engineering
 */
@Entity
@Table(name = "payment_methods", indexes = {
    @Index(name = "idx_payment_method_user_id", columnList = "user_id"),
    @Index(name = "idx_payment_method_provider", columnList = "provider"),
    @Index(name = "idx_payment_method_type", columnList = "type"),
    @Index(name = "idx_payment_method_is_default", columnList = "is_default")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PaymentMethodType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private PaymentProvider provider;

    @Column(name = "provider_method_id", nullable = false)
    private String providerMethodId;

    @Column(name = "last_four_digits", length = 4)
    private String lastFourDigits;

    @Column(name = "brand")
    private String brand;

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    @Column(name = "billing_address_line1")
    private String billingAddressLine1;

    @Column(name = "billing_address_line2")
    private String billingAddressLine2;

    @Column(name = "billing_city")
    private String billingCity;

    @Column(name = "billing_state")
    private String billingState;

    @Column(name = "billing_postal_code")
    private String billingPostalCode;

    @Column(name = "billing_country")
    private String billingCountry;

    @Column(name = "cardholder_name")
    private String cardholderName;

    @Column(name = "fingerprint")
    private String fingerprint;

    @Column(name = "is_default", columnDefinition = "boolean default false")
    private Boolean isDefault = false;

    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @Column(name = "verification_data", columnDefinition = "jsonb")
    private String verificationData;

    @Column(name = "risk_score", precision = 3, scale = 2)
    private java.math.BigDecimal riskScore;

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
    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.getYear() > expiryYear || 
               (now.getYear() == expiryYear && now.getMonthValue() > expiryMonth);
    }

    public boolean isVerified() {
        return VerificationStatus.VERIFIED.equals(verificationStatus);
    }

    public boolean canBeUsedForPayment() {
        return isActive && !isExpired() && isVerified();
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    public void markAsNonDefault() {
        this.isDefault = false;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void verify() {
        this.verificationStatus = VerificationStatus.VERIFIED;
    }

    public void markVerificationFailed() {
        this.verificationStatus = VerificationStatus.FAILED;
    }

    public String getMaskedCardNumber() {
        if (lastFourDigits == null) {
            return null;
        }
        return "**** **** **** " + lastFourDigits;
    }
}

/**
 * Payment Method Type Enumeration
 */
enum PaymentMethodType {
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_ACCOUNT,
    DIGITAL_WALLET,
    APPLE_PAY,
    GOOGLE_PAY,
    PAYPAL
}

/**
 * Verification Status Enumeration
 */
enum VerificationStatus {
    PENDING,
    VERIFIED,
    FAILED,
    REQUIRES_ACTION
}
