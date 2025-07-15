package com.doordash.ordering_service.services;

import com.doordash.ordering_service.models.dtos.PaymentResult;
import com.stripe.exception.StripeException;

import java.util.UUID;

public interface PaymentService {
    /**
     * Process a payment for an order
     * @param customerId the customer ID
     * @param paymentMethodId the payment method ID
     * @param amount the amount to charge
     * @return the payment result
     * @throws StripeException if there's an error processing the payment
     */
    PaymentResult processPayment(UUID customerId, UUID paymentMethodId, Double amount) throws StripeException;
    
    /**
     * Refund a payment
     * @param paymentId the payment ID to refund
     * @param amount the amount to refund (null for full refund)
     * @return the payment result
     * @throws StripeException if there's an error processing the refund
     */
    PaymentResult refundPayment(String paymentId, Double amount) throws StripeException;
}