package com.doordash.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Create Request DTO
 * 
 * Request object for creating a new payment with validation
 * and security considerations.
 * 
 * @author DoorDash Engineering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Payment method ID is required")
    private UUID paymentMethodId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @DecimalMax(value = "10000.00", message = "Amount cannot exceed 10,000.00")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Pattern(regexp = "^(STRIPE|PAYPAL|BRAINTREE|APPLE_PAY|GOOGLE_PAY)$", message = "Invalid payment provider")
    private String preferredProvider;

    @Min(value = 1, message = "Expiry hours must be at least 1")
    @Max(value = 72, message = "Expiry hours cannot exceed 72")
    private Integer expiryHours;

    private Boolean savePaymentMethod;

    private Map<String, Object> metadata;

    // Risk assessment fields
    private String ipAddress;
    private String userAgent;
    private String deviceFingerprint;
    private Boolean isTrustedDevice;
    
    // Order context
    private BigDecimal tipAmount;
    private BigDecimal deliveryFee;
    private BigDecimal serviceFee;
    private BigDecimal taxAmount;
    private String promoCode;
}
