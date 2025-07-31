package com.doordash.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Response DTO
 * 
 * Response object for payment operations with comprehensive
 * payment information and status.
 * 
 * @author DoorDash Engineering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private UUID userId;
    private UUID orderId;
    private UUID paymentMethodId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String provider;
    private String providerTransactionId;
    private String description;
    private String failureReason;
    private Integer retryCount;
    private LocalDateTime processedAt;
    private LocalDateTime settledAt;
    private LocalDateTime expiresAt;
    private BigDecimal riskScore;
    private Boolean isTest;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    // Related data
    private PaymentMethodInfo paymentMethod;
    private List<RefundInfo> refunds;
    private BigDecimal refundableAmount;
    
    // Metadata
    private Map<String, Object> metadata;

    // Computed fields
    private Boolean isSuccessful;
    private Boolean isFailed;
    private Boolean isPending;
    private Boolean isRefundable;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodInfo {
        private UUID id;
        private String type;
        private String brand;
        private String lastFourDigits;
        private Integer expiryMonth;
        private Integer expiryYear;
        private String cardholderName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundInfo {
        private UUID id;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String reason;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
    }
}
