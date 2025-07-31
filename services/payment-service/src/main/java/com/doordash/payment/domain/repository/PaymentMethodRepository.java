package com.doordash.payment.domain.repository;

import com.doordash.payment.domain.entity.PaymentMethod;
import com.doordash.payment.domain.entity.PaymentMethodType;
import com.doordash.payment.domain.entity.PaymentProvider;
import com.doordash.payment.domain.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Method Repository
 * 
 * Repository interface for PaymentMethod entity with comprehensive
 * query methods for payment method management operations.
 * 
 * @author DoorDash Engineering
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID>, JpaSpecificationExecutor<PaymentMethod> {

    /**
     * Find all payment methods for a user
     */
    List<PaymentMethod> findByUserIdAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    /**
     * Find active payment methods for a user
     */
    List<PaymentMethod> findByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Find default payment method for a user
     */
    Optional<PaymentMethod> findByUserIdAndIsDefaultTrueAndIsActiveTrue(UUID userId);

    /**
     * Find payment methods by type for a user
     */
    List<PaymentMethod> findByUserIdAndTypeAndIsActiveTrue(UUID userId, PaymentMethodType type);

    /**
     * Find payment methods by provider for a user
     */
    List<PaymentMethod> findByUserIdAndProviderAndIsActiveTrue(UUID userId, PaymentProvider provider);

    /**
     * Find payment method by provider method ID
     */
    Optional<PaymentMethod> findByProviderMethodId(String providerMethodId);

    /**
     * Find payment methods by fingerprint (for duplicate detection)
     */
    List<PaymentMethod> findByFingerprintAndIsActiveTrue(String fingerprint);

    /**
     * Find payment methods by last four digits and user
     */
    List<PaymentMethod> findByUserIdAndLastFourDigitsAndIsActiveTrue(UUID userId, String lastFourDigits);

    /**
     * Find expired payment methods
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.isActive = true AND pm.expiryYear < :currentYear OR (pm.expiryYear = :currentYear AND pm.expiryMonth < :currentMonth)")
    List<PaymentMethod> findExpiredPaymentMethods(@Param("currentYear") Integer currentYear, @Param("currentMonth") Integer currentMonth);

    /**
     * Find payment methods requiring verification
     */
    List<PaymentMethod> findByVerificationStatusAndIsActiveTrue(VerificationStatus verificationStatus);

    /**
     * Count active payment methods for a user
     */
    Long countByUserIdAndIsActiveTrue(UUID userId);

    /**
     * Count payment methods by type for a user
     */
    Long countByUserIdAndTypeAndIsActiveTrue(UUID userId, PaymentMethodType type);

    /**
     * Check if user has a default payment method
     */
    boolean existsByUserIdAndIsDefaultTrueAndIsActiveTrue(UUID userId);

    /**
     * Update all payment methods to non-default for a user
     */
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId")
    void unsetAllDefaultPaymentMethods(@Param("userId") UUID userId);

    /**
     * Deactivate all payment methods for a user
     */
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isActive = false WHERE pm.userId = :userId")
    void deactivateAllPaymentMethodsForUser(@Param("userId") UUID userId);

    /**
     * Find payment methods with high risk scores
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.riskScore > :threshold AND pm.isActive = true ORDER BY pm.riskScore DESC")
    List<PaymentMethod> findHighRiskPaymentMethods(@Param("threshold") java.math.BigDecimal threshold);

    /**
     * Find payment methods by brand
     */
    List<PaymentMethod> findByUserIdAndBrandAndIsActiveTrue(UUID userId, String brand);

    /**
     * Find payment methods expiring soon
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.isActive = true AND pm.expiryYear = :year AND pm.expiryMonth <= :month")
    List<PaymentMethod> findPaymentMethodsExpiringSoon(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * Find duplicate payment methods by fingerprint and user
     */
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.userId = :userId AND pm.fingerprint = :fingerprint AND pm.isActive = true AND pm.id != :excludeId")
    List<PaymentMethod> findDuplicatePaymentMethods(@Param("userId") UUID userId, @Param("fingerprint") String fingerprint, @Param("excludeId") UUID excludeId);

    /**
     * Find payment methods by billing country
     */
    List<PaymentMethod> findByBillingCountryAndIsActiveTrue(String country);

    /**
     * Update verification status
     */
    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.verificationStatus = :status WHERE pm.id = :id")
    void updateVerificationStatus(@Param("id") UUID id, @Param("status") VerificationStatus status);
}
