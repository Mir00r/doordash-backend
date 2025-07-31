package com.doordash.payment.controller;

import com.doordash.payment.dto.request.PaymentCreateRequest;
import com.doordash.payment.dto.request.PaymentUpdateRequest;
import com.doordash.payment.dto.request.RefundCreateRequest;
import com.doordash.payment.dto.response.PaymentResponse;
import com.doordash.payment.dto.response.RefundResponse;
import com.doordash.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Payment Controller
 * 
 * REST API controller for payment operations including
 * payment processing, refunds, and payment management.
 * 
 * @author DoorDash Engineering
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing and management operations")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a new payment", description = "Create a new payment for an order")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "409", description = "Duplicate payment"),
        @ApiResponse(responseCode = "422", description = "Payment validation failed")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.info("Creating payment for user: {} and order: {}", userId, request.getOrderId());
        
        PaymentResponse response = paymentService.createPayment(request, userId);
        
        log.info("Payment created successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by payment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "Payment ID") @PathVariable UUID paymentId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Getting payment: {} for user: {}", paymentId, userId);
        
        PaymentResponse response = paymentService.getPayment(paymentId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment by order ID", description = "Retrieve payment details by order ID")
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Getting payment for order: {} and user: {}", orderId, userId);
        
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user payment history", description = "Retrieve paginated payment history for the user")
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getUserPayments(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Getting payment history for user: {}", userId);
        
        Page<PaymentResponse> response = paymentService.getUserPayments(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process a pending payment", description = "Process a payment that is in pending status")
    @PostMapping("/{paymentId}/process")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "Payment ID") @PathVariable UUID paymentId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.info("Processing payment: {} for user: {}", paymentId, userId);
        
        PaymentResponse response = paymentService.processPayment(paymentId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update payment", description = "Update payment details")
    @PutMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> updatePayment(
            @Parameter(description = "Payment ID") @PathVariable UUID paymentId,
            @Valid @RequestBody PaymentUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.info("Updating payment: {} for user: {}", paymentId, userId);
        
        PaymentResponse response = paymentService.updatePayment(paymentId, request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel payment", description = "Cancel a pending payment")
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID") @PathVariable UUID paymentId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.info("Cancelling payment: {} for user: {}", paymentId, userId);
        
        PaymentResponse response = paymentService.cancelPayment(paymentId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create refund", description = "Create a refund for a successful payment")
    @PostMapping("/{paymentId}/refunds")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> createRefund(
            @Parameter(description = "Payment ID") @PathVariable UUID paymentId,
            @Valid @RequestBody RefundCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.info("Creating refund for payment: {} by user: {}", paymentId, userId);
        
        RefundResponse response = paymentService.createRefund(paymentId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get payment refunds", description = "Get all refunds for a payment")
    @GetMapping("/{paymentId}/refunds")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<RefundResponse>> getPaymentRefunds(
            @Parameter(description = "Payment ID") @PathVariable UUID paymentId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Getting refunds for payment: {} and user: {}", paymentId, userId);
        
        List<RefundResponse> response = paymentService.getPaymentRefunds(paymentId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get refund by ID", description = "Retrieve refund details by refund ID")
    @GetMapping("/refunds/{refundId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> getRefund(
            @Parameter(description = "Refund ID") @PathVariable UUID refundId,
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Getting refund: {} for user: {}", refundId, userId);
        
        RefundResponse response = paymentService.getRefund(refundId, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get payment statistics", description = "Get payment statistics for the user")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentService.PaymentStatistics> getPaymentStatistics(
            @AuthenticationPrincipal Jwt jwt) {
        
        UUID userId = UUID.fromString(jwt.getSubject());
        log.debug("Getting payment statistics for user: {}", userId);
        
        PaymentService.PaymentStatistics statistics = paymentService.getUserPaymentStatistics(userId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Handle payment webhook", description = "Handle webhook notifications from payment providers")
    @PostMapping("/webhooks/{provider}")
    public ResponseEntity<Void> handleWebhook(
            @Parameter(description = "Payment provider") @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        
        log.info("Received webhook from provider: {}", provider);
        
        paymentService.handlePaymentWebhook(provider, payload, signature);
        return ResponseEntity.ok().build();
    }
}
