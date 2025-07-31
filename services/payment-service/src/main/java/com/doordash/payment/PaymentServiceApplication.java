package com.doordash.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Payment Service Application
 * 
 * A comprehensive payment microservice for DoorDash backend that handles:
 * - Payment processing with multiple providers (Stripe, PayPal, Braintree)
 * - Secure payment method management
 * - Refunds and settlements
 * - PCI DSS compliance
 * - Event-driven architecture integration
 * 
 * @author DoorDash Engineering
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableFeignClients
@EnableKafka
@EnableAsync
@EnableScheduling
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
