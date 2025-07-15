package com.doordash.ordering_service.services.impl;

import com.doordash.ordering_service.enums.PaymentStatus;
import com.doordash.ordering_service.exceptions.PaymentException;
import com.doordash.ordering_service.exceptions.ResourceNotFoundException;
import com.doordash.ordering_service.models.dtos.PaymentResult;
import com.doordash.ordering_service.models.entities.PaymentMethod;
import com.doordash.ordering_service.repositories.PaymentMethodRepository;
import com.doordash.ordering_service.services.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final MeterRegistry meterRegistry;
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${stripe.api.currency:usd}")
    private String currency;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public PaymentResult processPayment(UUID customerId, UUID paymentMethodId, Double amount) throws StripeException {
        log.info("Processing payment for customer: {}, amount: {}", customerId, amount);
        meterRegistry.counter("payment.process").increment();
        
        try {
            // Get payment method
            PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with ID: " + paymentMethodId));
            
            // Verify payment method belongs to customer
            if (!paymentMethod.getCustomerId().equals(customerId)) {
                throw new ResourceNotFoundException("Payment method not found with ID: " + paymentMethodId + " for customer: " + customerId);
            }
            
            // Create charge parameters
            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", (int) (amount * 100)); // Convert to cents
            chargeParams.put("currency", currency);
            chargeParams.put("source", paymentMethod.getPaymentToken()); // Use the token from the payment method
            chargeParams.put("description", "Order payment for customer: " + customerId);
            
            // Create charge
            Charge charge = Charge.create(chargeParams);
            
            // Return payment result
            return PaymentResult.builder()
                    .paymentId(charge.getId())
                    .status(charge.getPaid() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                    .amount(amount)
                    .currency(charge.getCurrency())
                    .receiptUrl(charge.getReceiptUrl())
                    .build();
            
        } catch (StripeException e) {
            log.error("Error processing payment: {}", e.getMessage());
            meterRegistry.counter("payment.error").increment();
            
            return PaymentResult.builder()
                    .status(PaymentStatus.FAILED)
                    .amount(amount)
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error processing payment: {}", e.getMessage());
            meterRegistry.counter("payment.error").increment();
            
            throw new PaymentException("Error processing payment: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult refundPayment(String paymentId, Double amount) throws StripeException {
        log.info("Refunding payment: {}, amount: {}", paymentId, amount);
        meterRegistry.counter("payment.refund").increment();
        
        try {
            // Create refund parameters
            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put("charge", paymentId);
            
            // If amount is specified, refund that amount, otherwise refund the full amount
            if (amount != null) {
                refundParams.put("amount", (int) (amount * 100)); // Convert to cents
            }
            
            // Create refund
            Refund refund = Refund.create(refundParams);
            
            // Return payment result
            return PaymentResult.builder()
                    .paymentId(refund.getId())
                    .status(PaymentStatus.REFUNDED)
                    .amount(amount != null ? amount : refund.getAmount() / 100.0) // Convert from cents
                    .currency(refund.getCurrency())
                    .build();
            
        } catch (StripeException e) {
            log.error("Error refunding payment: {}", e.getMessage());
            meterRegistry.counter("payment.refund.error").increment();
            
            return PaymentResult.builder()
                    .status(PaymentStatus.FAILED)
                    .paymentId(paymentId)
                    .amount(amount)
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error refunding payment: {}", e.getMessage());
            meterRegistry.counter("payment.refund.error").increment();
            
            throw new PaymentException("Error refunding payment: " + e.getMessage());
        }
    }
}