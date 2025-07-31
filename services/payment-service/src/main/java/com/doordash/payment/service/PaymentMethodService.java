package com.doordash.payment.service;

import com.doordash.payment.dto.request.PaymentMethodCreateRequest;
import com.doordash.payment.dto.request.PaymentMethodUpdateRequest;
import com.doordash.payment.dto.response.PaymentMethodResponse;

import java.util.List;
import java.util.UUID;

/**
 * Payment Method Service Interface
 * 
 * Service interface for payment method management including
 * secure storage, verification, and lifecycle operations.
 * 
 * @author DoorDash Engineering
 */
public interface PaymentMethodService {

    /**
     * Create a new payment method
     */
    PaymentMethodResponse createPaymentMethod(PaymentMethodCreateRequest request, UUID userId);

    /**
     * Get payment method by ID
     */
    PaymentMethodResponse getPaymentMethod(UUID paymentMethodId, UUID userId);

    /**
     * Get all payment methods for a user
     */
    List<PaymentMethodResponse> getUserPaymentMethods(UUID userId);

    /**
     * Get default payment method for a user
     */
    PaymentMethodResponse getDefaultPaymentMethod(UUID userId);

    /**
     * Update payment method details
     */
    PaymentMethodResponse updatePaymentMethod(UUID paymentMethodId, PaymentMethodUpdateRequest request, UUID userId);

    /**
     * Set payment method as default
     */
    PaymentMethodResponse setAsDefault(UUID paymentMethodId, UUID userId);

    /**
     * Deactivate a payment method
     */
    void deactivatePaymentMethod(UUID paymentMethodId, UUID userId);

    /**
     * Delete a payment method (hard delete)
     */
    void deletePaymentMethod(UUID paymentMethodId, UUID userId);

    /**
     * Verify a payment method
     */
    PaymentMethodResponse verifyPaymentMethod(UUID paymentMethodId, UUID userId);

    /**
     * Update verification status
     */
    void updateVerificationStatus(UUID paymentMethodId, String status, String verificationData);

    /**
     * Check if payment method can be used for payment
     */
    boolean canUseForPayment(UUID paymentMethodId, UUID userId);

    /**
     * Find duplicate payment methods
     */
    List<PaymentMethodResponse> findDuplicatePaymentMethods(UUID userId, String fingerprint);

    /**
     * Get payment methods expiring soon
     */
    List<PaymentMethodResponse> getPaymentMethodsExpiringSoon(UUID userId, int months);

    /**
     * Deactivate expired payment methods
     */
    void deactivateExpiredPaymentMethods();

    /**
     * Validate payment method for PCI compliance
     */
    void validatePaymentMethodCompliance(PaymentMethodCreateRequest request);

    /**
     * Handle payment method webhook from provider
     */
    void handlePaymentMethodWebhook(String provider, String payload, String signature);
}
