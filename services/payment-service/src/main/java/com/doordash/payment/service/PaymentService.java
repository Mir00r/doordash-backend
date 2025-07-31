package com.doordash.payment.service;

import com.doordash.payment.dto.request.PaymentCreateRequest;
import com.doordash.payment.dto.request.PaymentUpdateRequest;
import com.doordash.payment.dto.request.RefundCreateRequest;
import com.doordash.payment.dto.response.PaymentResponse;
import com.doordash.payment.dto.response.RefundResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Payment Service Interface
 * 
 * Core service interface for payment operations including
 * payment processing, refunds, and payment lifecycle management.
 * 
 * @author DoorDash Engineering
 */
public interface PaymentService {

    /**
     * Create a new payment
     */
    PaymentResponse createPayment(PaymentCreateRequest request, UUID userId);

    /**
     * Process a pending payment
     */
    PaymentResponse processPayment(UUID paymentId, UUID userId);

    /**
     * Get payment by ID
     */
    PaymentResponse getPayment(UUID paymentId, UUID userId);

    /**
     * Get payment by order ID
     */
    PaymentResponse getPaymentByOrderId(UUID orderId, UUID userId);

    /**
     * Get user's payment history
     */
    Page<PaymentResponse> getUserPayments(UUID userId, Pageable pageable);

    /**
     * Update payment details
     */
    PaymentResponse updatePayment(UUID paymentId, PaymentUpdateRequest request, UUID userId);

    /**
     * Cancel a pending payment
     */
    PaymentResponse cancelPayment(UUID paymentId, UUID userId);

    /**
     * Create a refund for a payment
     */
    RefundResponse createRefund(UUID paymentId, RefundCreateRequest request, UUID userId);

    /**
     * Get refund by ID
     */
    RefundResponse getRefund(UUID refundId, UUID userId);

    /**
     * Get all refunds for a payment
     */
    List<RefundResponse> getPaymentRefunds(UUID paymentId, UUID userId);

    /**
     * Process pending refunds
     */
    void processRefunds();

    /**
     * Retry failed payments
     */
    void retryFailedPayments();

    /**
     * Settle successful payments
     */
    void settlePayments();

    /**
     * Cancel expired payments
     */
    void cancelExpiredPayments();

    /**
     * Get payment statistics for a user
     */
    PaymentStatistics getUserPaymentStatistics(UUID userId);

    /**
     * Get payment statistics by date range
     */
    PaymentStatistics getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Validate payment amount and limits
     */
    void validatePaymentAmount(BigDecimal amount, UUID userId);

    /**
     * Check for duplicate payments
     */
    boolean isDuplicatePayment(UUID userId, BigDecimal amount, UUID orderId);

    /**
     * Handle payment webhook from provider
     */
    void handlePaymentWebhook(String provider, String payload, String signature);

    /**
     * Payment Statistics Inner Class
     */
    record PaymentStatistics(
        Long totalPayments,
        BigDecimal totalAmount,
        BigDecimal averageAmount,
        Long successfulPayments,
        Long failedPayments,
        Long refundedPayments,
        BigDecimal totalRefundAmount
    ) {}
}
