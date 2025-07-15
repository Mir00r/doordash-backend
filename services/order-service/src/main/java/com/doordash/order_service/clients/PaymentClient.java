package com.doordash.order_service.clients;

import com.doordash.order_service.models.dtos.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment-service", url = "${app.payment-service.url}")
public interface PaymentClient {

    @PostMapping("/api/v1/payments/process")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentService")
    PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest);

    default PaymentResponse processPayment(UUID customerId, UUID paymentMethodId, BigDecimal amount) {
        PaymentRequest request = PaymentRequest.builder()
                .customerId(customerId)
                .paymentMethodId(paymentMethodId)
                .amount(amount)
                .build();
        return processPayment(request);
    }

    default PaymentResponse processPaymentFallback(PaymentRequest paymentRequest, Exception ex) {
        throw new RuntimeException("Payment service is unavailable", ex);
    }

    default PaymentResponse processPaymentFallback(UUID customerId, UUID paymentMethodId, BigDecimal amount, Exception ex) {
        throw new RuntimeException("Payment service is unavailable", ex);
    }
}