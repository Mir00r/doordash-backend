package com.doordash.payment.domain.repository;

import com.doordash.payment.domain.entity.Payment;
import com.doordash.payment.domain.entity.PaymentStatus;
import com.doordash.payment.domain.entity.PaymentProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Repository
 * 
 * Repository interface for Payment entity with comprehensive
 * query methods for payment operations and analytics.
 * 
 * @author DoorDash Engineering
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    /**
     * Find payment by order ID
     */
    Optional<Payment> findByOrderId(UUID orderId);

    /**
     * Find all payments for a user
     */
    Page<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payments by provider and status
     */
    List<Payment> findByProviderAndStatus(PaymentProvider provider, PaymentStatus status);

    /**
     * Find payments that need settlement
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'SUCCEEDED' AND p.settledAt IS NULL AND p.processedAt < :cutoffTime")
    List<Payment> findPaymentsForSettlement(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find failed payments that can be retried
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < :maxRetries AND p.createdAt > :cutoffTime")
    List<Payment> findRetryableFailedPayments(@Param("maxRetries") Integer maxRetries, @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find payments by provider transaction ID
     */
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    /**
     * Find payments by provider payment intent ID
     */
    Optional<Payment> findByProviderPaymentIntentId(String providerPaymentIntentId);

    /**
     * Get payment statistics for a user
     */
    @Query("SELECT COUNT(p), SUM(p.amount), AVG(p.amount) FROM Payment p WHERE p.userId = :userId AND p.status = 'SUCCEEDED'")
    List<Object[]> getPaymentStatsByUser(@Param("userId") UUID userId);

    /**
     * Get payment statistics by date range
     */
    @Query("SELECT p.provider, COUNT(p), SUM(p.amount) FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status = 'SUCCEEDED' GROUP BY p.provider")
    List<Object[]> getPaymentStatsByProvider(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find high-risk payments
     */
    @Query("SELECT p FROM Payment p WHERE p.riskScore > :threshold ORDER BY p.riskScore DESC")
    List<Payment> findHighRiskPayments(@Param("threshold") BigDecimal threshold);

    /**
     * Find payments with amount greater than threshold
     */
    List<Payment> findByAmountGreaterThanOrderByAmountDesc(BigDecimal amount);

    /**
     * Find payments created within date range
     */
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find expired pending payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING') AND p.expiresAt < :now")
    List<Payment> findExpiredPendingPayments(@Param("now") LocalDateTime now);

    /**
     * Count payments by status for a user
     */
    Long countByUserIdAndStatus(UUID userId, PaymentStatus status);

    /**
     * Find payments by payment method
     */
    List<Payment> findByPaymentMethodIdOrderByCreatedAtDesc(UUID paymentMethodId);

    /**
     * Check if payment exists for order
     */
    boolean existsByOrderId(UUID orderId);

    /**
     * Get total payment amount for user in date range
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.userId = :userId AND p.status = 'SUCCEEDED' AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaymentAmountByUserAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Find duplicate payments (same user, amount, and timeframe)
     */
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.amount = :amount AND p.createdAt BETWEEN :startTime AND :endTime AND p.id != :excludePaymentId")
    List<Payment> findPotentialDuplicatePayments(@Param("userId") UUID userId, @Param("amount") BigDecimal amount, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("excludePaymentId") UUID excludePaymentId);
}
