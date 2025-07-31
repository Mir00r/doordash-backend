package com.doordash.payment.service.impl;

import com.doordash.payment.domain.entity.Payment;
import com.doordash.payment.domain.entity.PaymentMethod;
import com.doordash.payment.domain.entity.PaymentProvider;
import com.doordash.payment.domain.entity.PaymentStatus;
import com.doordash.payment.domain.repository.PaymentRepository;
import com.doordash.payment.domain.repository.PaymentMethodRepository;
import com.doordash.payment.dto.request.PaymentCreateRequest;
import com.doordash.payment.dto.response.PaymentResponse;
import com.doordash.payment.exception.PaymentNotFoundException;
import com.doordash.payment.exception.PaymentValidationException;
import com.doordash.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Payment Service Implementation Test
 * 
 * Comprehensive unit tests for PaymentService implementation
 * covering payment creation, processing, and edge cases.
 * 
 * @author DoorDash Engineering
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Payment Service Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentMethodRepository paymentMethodRepository;
    
    @Mock
    private PaymentProviderService paymentProviderService;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private AuditService auditService;
    
    private PaymentService paymentService;
    
    private UUID userId;
    private UUID orderId;
    private UUID paymentMethodId;
    private PaymentCreateRequest validPaymentRequest;
    private PaymentMethod validPaymentMethod;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
            paymentRepository,
            paymentMethodRepository,
            paymentProviderService,
            eventPublisher,
            auditService
        );
        
        // Test data setup
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        paymentMethodId = UUID.randomUUID();
        
        validPaymentRequest = PaymentCreateRequest.builder()
            .orderId(orderId)
            .paymentMethodId(paymentMethodId)
            .amount(new BigDecimal("25.99"))
            .currency("USD")
            .description("Order payment")
            .build();
            
        validPaymentMethod = PaymentMethod.builder()
            .id(paymentMethodId)
            .userId(userId)
            .provider(PaymentProvider.STRIPE)
            .providerMethodId("pm_test_123")
            .isActive(true)
            .verificationStatus(VerificationStatus.VERIFIED)
            .build();
    }

    @Test
    @DisplayName("Should create payment successfully with valid request")
    void shouldCreatePaymentSuccessfully() {
        // Given
        given(paymentMethodRepository.findById(paymentMethodId))
            .willReturn(Optional.of(validPaymentMethod));
        given(paymentRepository.existsByOrderId(orderId))
            .willReturn(false);
        given(paymentRepository.save(any(Payment.class)))
            .willAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                payment.setId(UUID.randomUUID());
                payment.setCreatedAt(LocalDateTime.now());
                return payment;
            });

        // When
        PaymentResponse response = paymentService.createPayment(validPaymentRequest, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getAmount()).isEqualTo(new BigDecimal("25.99"));
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING.name());

        verify(paymentRepository).save(any(Payment.class));
        verify(eventPublisher).publishEvent(any());
        verify(auditService).logPaymentCreated(any(), eq(userId));
    }

    @Test
    @DisplayName("Should throw exception when payment method not found")
    void shouldThrowExceptionWhenPaymentMethodNotFound() {
        // Given
        given(paymentMethodRepository.findById(paymentMethodId))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(validPaymentRequest, userId))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Payment method not found");

        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when payment method belongs to different user")
    void shouldThrowExceptionWhenPaymentMethodBelongsToDifferentUser() {
        // Given
        PaymentMethod differentUserPaymentMethod = validPaymentMethod.toBuilder()
            .userId(UUID.randomUUID()) // Different user
            .build();
            
        given(paymentMethodRepository.findById(paymentMethodId))
            .willReturn(Optional.of(differentUserPaymentMethod));

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(validPaymentRequest, userId))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Payment method does not belong to user");
    }

    @Test
    @DisplayName("Should throw exception when duplicate payment exists for order")
    void shouldThrowExceptionWhenDuplicatePaymentExists() {
        // Given
        given(paymentMethodRepository.findById(paymentMethodId))
            .willReturn(Optional.of(validPaymentMethod));
        given(paymentRepository.existsByOrderId(orderId))
            .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(validPaymentRequest, userId))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Payment already exists for order");
    }

    @Test
    @DisplayName("Should throw exception when payment amount is invalid")
    void shouldThrowExceptionWhenPaymentAmountIsInvalid() {
        // Given
        PaymentCreateRequest invalidAmountRequest = validPaymentRequest.toBuilder()
            .amount(new BigDecimal("-10.00")) // Negative amount
            .build();

        // When & Then
        assertThatThrownBy(() -> paymentService.createPayment(invalidAmountRequest, userId))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Invalid payment amount");
    }

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() {
        // Given
        UUID paymentId = UUID.randomUUID();
        Payment pendingPayment = Payment.builder()
            .id(paymentId)
            .userId(userId)
            .orderId(orderId)
            .paymentMethodId(paymentMethodId)
            .amount(new BigDecimal("25.99"))
            .currency("USD")
            .status(PaymentStatus.PENDING)
            .provider(PaymentProvider.STRIPE)
            .build();

        given(paymentRepository.findById(paymentId))
            .willReturn(Optional.of(pendingPayment));
        given(paymentProviderService.processPayment(pendingPayment))
            .willReturn(true);
        given(paymentRepository.save(any(Payment.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.processPayment(paymentId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED.name());
        assertThat(response.getProcessedAt()).isNotNull();

        verify(paymentRepository).save(argThat(payment -> 
            PaymentStatus.SUCCEEDED.equals(payment.getStatus())
        ));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw exception when payment not found for processing")
    void shouldThrowExceptionWhenPaymentNotFoundForProcessing() {
        // Given
        UUID paymentId = UUID.randomUUID();
        given(paymentRepository.findById(paymentId))
            .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentId, userId))
            .isInstanceOf(PaymentNotFoundException.class)
            .hasMessageContaining("Payment not found");
    }

    @Test
    @DisplayName("Should handle payment processing failure")
    void shouldHandlePaymentProcessingFailure() {
        // Given
        UUID paymentId = UUID.randomUUID();
        Payment pendingPayment = Payment.builder()
            .id(paymentId)
            .userId(userId)
            .status(PaymentStatus.PENDING)
            .provider(PaymentProvider.STRIPE)
            .build();

        given(paymentRepository.findById(paymentId))
            .willReturn(Optional.of(pendingPayment));
        given(paymentProviderService.processPayment(pendingPayment))
            .willReturn(false);
        given(paymentRepository.save(any(Payment.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // When
        PaymentResponse response = paymentService.processPayment(paymentId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED.name());

        verify(paymentRepository).save(argThat(payment -> 
            PaymentStatus.FAILED.equals(payment.getStatus())
        ));
    }

    @Test
    @DisplayName("Should validate payment amount against limits")
    void shouldValidatePaymentAmountAgainstLimits() {
        // Given
        BigDecimal excessiveAmount = new BigDecimal("15000.00"); // Above limit
        UUID testUserId = UUID.randomUUID();

        // When & Then
        assertThatThrownBy(() -> paymentService.validatePaymentAmount(excessiveAmount, testUserId))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Payment amount exceeds maximum limit");
    }

    @Test
    @DisplayName("Should detect duplicate payment attempts")
    void shouldDetectDuplicatePaymentAttempts() {
        // Given
        BigDecimal amount = new BigDecimal("25.99");
        UUID testOrderId = UUID.randomUUID();
        
        given(paymentRepository.findPotentialDuplicatePayments(
            eq(userId), eq(amount), any(), any(), any()))
            .willReturn(List.of(Payment.builder().id(UUID.randomUUID()).build()));

        // When
        boolean isDuplicate = paymentService.isDuplicatePayment(userId, amount, testOrderId);

        // Then
        assertThat(isDuplicate).isTrue();
    }

    @Test
    @DisplayName("Should get payment by ID with proper authorization")
    void shouldGetPaymentByIdWithProperAuthorization() {
        // Given
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
            .id(paymentId)
            .userId(userId)
            .orderId(orderId)
            .amount(new BigDecimal("25.99"))
            .currency("USD")
            .status(PaymentStatus.SUCCEEDED)
            .build();

        given(paymentRepository.findById(paymentId))
            .willReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.getPayment(paymentId, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(paymentId);
        assertThat(response.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("Should throw exception when accessing payment of different user")
    void shouldThrowExceptionWhenAccessingPaymentOfDifferentUser() {
        // Given
        UUID paymentId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        Payment payment = Payment.builder()
            .id(paymentId)
            .userId(differentUserId) // Different user
            .build();

        given(paymentRepository.findById(paymentId))
            .willReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> paymentService.getPayment(paymentId, userId))
            .isInstanceOf(PaymentValidationException.class)
            .hasMessageContaining("Access denied");
    }
}
