package com.doordash.user_service.consistency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository Interfaces for Saga Pattern Data Access.
 * 
 * Provides data access methods for saga management including:
 * - Saga instance persistence and retrieval
 * - Saga step execution tracking
 * - Saga state queries and monitoring
 * - Recovery and cleanup operations
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */

/**
 * Repository for Saga Instance entities.
 */
@Repository
public interface SagaRepository extends JpaRepository<SagaInstance, String> {

    /**
     * Find sagas by status.
     */
    List<SagaInstance> findByStatus(SagaStatus status);

    /**
     * Find sagas by type.
     */
    List<SagaInstance> findByType(String type);

    /**
     * Find sagas by type and status.
     */
    List<SagaInstance> findByTypeAndStatus(String type, SagaStatus status);

    /**
     * Find sagas created within date range.
     */
    @Query("SELECT s FROM SagaInstance s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<SagaInstance> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Find long-running sagas (older than specified time).
     */
    @Query("SELECT s FROM SagaInstance s WHERE s.status IN ('STARTED', 'IN_PROGRESS') AND s.createdAt < :cutoffTime")
    List<SagaInstance> findLongRunningSagas(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find sagas that need recovery (stuck in non-terminal states).
     */
    @Query("""
        SELECT s FROM SagaInstance s 
        WHERE s.status IN ('STARTED', 'IN_PROGRESS', 'COMPENSATING') 
        AND s.updatedAt < :staleTime
        """)
    List<SagaInstance> findSagasNeedingRecovery(@Param("staleTime") LocalDateTime staleTime);

    /**
     * Find sagas by tenant ID.
     */
    @Query("SELECT s FROM SagaInstance s WHERE s.context.tenantId = :tenantId")
    List<SagaInstance> findByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find sagas by user ID.
     */
    @Query("SELECT s FROM SagaInstance s WHERE s.context.userId = :userId")
    List<SagaInstance> findByUserId(@Param("userId") String userId);

    /**
     * Find sagas by correlation ID.
     */
    @Query("SELECT s FROM SagaInstance s WHERE s.context.correlationId = :correlationId")
    Optional<SagaInstance> findByCorrelationId(@Param("correlationId") String correlationId);

    /**
     * Count sagas by status.
     */
    long countByStatus(SagaStatus status);

    /**
     * Count sagas by type and status.
     */
    long countByTypeAndStatus(String type, SagaStatus status);

    /**
     * Find failed sagas for a specific time period.
     */
    @Query("""
        SELECT s FROM SagaInstance s 
        WHERE s.status = 'FAILED' 
        AND s.updatedAt BETWEEN :startDate AND :endDate
        """)
    List<SagaInstance> findFailedSagasBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate average execution time for completed sagas.
     */
    @Query("""
        SELECT AVG(TIMESTAMPDIFF(MICROSECOND, s.createdAt, s.completedAt) / 1000) 
        FROM SagaInstance s 
        WHERE s.status = 'COMPLETED' 
        AND s.type = :type
        """)
    Double calculateAverageExecutionTime(@Param("type") String type);

    /**
     * Delete old completed sagas for cleanup.
     */
    @Query("DELETE FROM SagaInstance s WHERE s.status IN ('COMPLETED', 'COMPENSATED') AND s.completedAt < :cutoffDate")
    int deleteOldCompletedSagas(@Param("cutoffDate") LocalDateTime cutoffDate);
}

/**
 * Repository for Saga Step Execution entities.
 */
@Repository
interface SagaStepRepository extends JpaRepository<SagaStepExecution, Long> {

    /**
     * Find step executions by saga ID.
     */
    List<SagaStepExecution> findBySagaIdOrderByExecutedAtAsc(String sagaId);

    /**
     * Find step executions by saga ID and status.
     */
    List<SagaStepExecution> findBySagaIdAndStatus(String sagaId, SagaStepStatus status);

    /**
     * Find step executions by step name.
     */
    List<SagaStepExecution> findByStepName(String stepName);

    /**
     * Find step executions by step name and status.
     */
    List<SagaStepExecution> findByStepNameAndStatus(String stepName, SagaStepStatus status);

    /**
     * Find the latest step execution for a saga.
     */
    @Query("""
        SELECT se FROM SagaStepExecution se 
        WHERE se.sagaId = :sagaId 
        ORDER BY se.executedAt DESC 
        LIMIT 1
        """)
    Optional<SagaStepExecution> findLatestStepExecution(@Param("sagaId") String sagaId);

    /**
     * Find step executions that failed and need compensation.
     */
    @Query("""
        SELECT se FROM SagaStepExecution se 
        WHERE se.status = 'FAILED' 
        AND se.compensatedAt IS NULL
        """)
    List<SagaStepExecution> findStepsNeedingCompensation();

    /**
     * Find long-running step executions.
     */
    @Query("""
        SELECT se FROM SagaStepExecution se 
        WHERE se.status = 'EXECUTING' 
        AND se.executedAt < :cutoffTime
        """)
    List<SagaStepExecution> findLongRunningSteps(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count step executions by status.
     */
    long countByStatus(SagaStepStatus status);

    /**
     * Count step executions by step name and status.
     */
    long countByStepNameAndStatus(String stepName, SagaStepStatus status);

    /**
     * Calculate average execution time for steps.
     */
    @Query("""
        SELECT AVG(se.executionTimeMs) 
        FROM SagaStepExecution se 
        WHERE se.stepName = :stepName 
        AND se.status = 'COMPLETED'
        """)
    Double calculateAverageStepExecutionTime(@Param("stepName") String stepName);

    /**
     * Find steps executed within date range.
     */
    @Query("""
        SELECT se FROM SagaStepExecution se 
        WHERE se.executedAt BETWEEN :startDate AND :endDate
        """)
    List<SagaStepExecution> findByExecutedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find failed steps for analysis.
     */
    @Query("""
        SELECT se FROM SagaStepExecution se 
        WHERE se.status = 'FAILED' 
        AND se.executedAt BETWEEN :startDate AND :endDate
        """)
    List<SagaStepExecution> findFailedStepsBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Delete old step executions for cleanup.
     */
    @Query("DELETE FROM SagaStepExecution se WHERE se.executedAt < :cutoffDate")
    int deleteOldStepExecutions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
