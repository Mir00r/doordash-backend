package com.doordash.user_service.consistency;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Saga Pattern Entities and Supporting Classes for DoorDash Platform.
 * 
 * Contains all entities and classes needed for saga pattern implementation:
 * - SagaInstance: Persistent saga execution state
 * - SagaStepExecution: Individual step execution records
 * - SagaDefinition: Saga workflow definition
 * - SagaStep: Individual step definition with execution and compensation logic
 * - SagaContext: Runtime context data for saga execution
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */

/**
 * Saga Instance Entity - represents a running or completed saga execution.
 */
@Entity
@Table(name = "saga_instances", indexes = {
    @Index(name = "idx_saga_status", columnList = "status"),
    @Index(name = "idx_saga_type", columnList = "type"),
    @Index(name = "idx_saga_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaInstance {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;

    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Embedded
    private SagaContext context;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if saga is in a terminal state.
     */
    public boolean isTerminal() {
        return status == SagaStatus.COMPLETED || 
               status == SagaStatus.FAILED || 
               status == SagaStatus.COMPENSATED ||
               status == SagaStatus.COMPENSATION_FAILED;
    }

    /**
     * Check if saga completed successfully.
     */
    public boolean isSuccessful() {
        return status == SagaStatus.COMPLETED;
    }
}

/**
 * Saga Step Execution Entity - records execution of individual saga steps.
 */
@Entity
@Table(name = "saga_step_executions", indexes = {
    @Index(name = "idx_saga_step_saga_id", columnList = "saga_id"),
    @Index(name = "idx_saga_step_status", columnList = "status"),
    @Index(name = "idx_saga_step_executed", columnList = "executed_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", nullable = false, length = 36)
    private String sagaId;

    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStepStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "sagaId", column = @Column(name = "result_saga_id")),
        @AttributeOverride(name = "status", column = @Column(name = "result_status")),
        @AttributeOverride(name = "errorMessage", column = @Column(name = "result_error"))
    })
    private SagaResult result;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "sagaId", column = @Column(name = "compensation_saga_id")),
        @AttributeOverride(name = "status", column = @Column(name = "compensation_status")),
        @AttributeOverride(name = "errorMessage", column = @Column(name = "compensation_error"))
    })
    private SagaResult compensationResult;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "compensated_at")
    private LocalDateTime compensatedAt;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    /**
     * Check if step was successfully executed.
     */
    public boolean wasSuccessful() {
        return status == SagaStepStatus.COMPLETED;
    }

    /**
     * Check if step was compensated.
     */
    public boolean wasCompensated() {
        return compensatedAt != null && compensationResult != null;
    }
}

/**
 * Saga Status enumeration.
 */
enum SagaStatus {
    STARTED,                // Saga execution started
    IN_PROGRESS,           // Saga is currently executing steps
    COMPLETED,             // Saga completed successfully
    FAILED,                // Saga failed and requires compensation
    COMPENSATING,          // Compensation is in progress
    COMPENSATED,           // Compensation completed successfully
    COMPENSATION_FAILED    // Compensation failed
}

/**
 * Saga Step Status enumeration.
 */
enum SagaStepStatus {
    PENDING,      // Step is waiting to be executed
    EXECUTING,    // Step is currently executing
    COMPLETED,    // Step completed successfully
    FAILED,       // Step failed
    SKIPPED,      // Step was skipped due to conditions
    COMPENSATED   // Step was compensated
}

/**
 * Saga Context - embedded context data for saga execution.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaContext {

    @Column(name = "saga_id", length = 36)
    private String sagaId;

    @Column(name = "tenant_id", length = 36)
    private String tenantId;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Column(name = "request_id", length = 36)
    private String requestId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "saga_context_data", joinColumns = @JoinColumn(name = "saga_id"))
    @MapKeyColumn(name = "data_key")
    @Column(name = "data_value", columnDefinition = "TEXT")
    private Map<String, String> contextData;

    /**
     * Get context data value.
     */
    public String getContextValue(String key) {
        return contextData != null ? contextData.get(key) : null;
    }

    /**
     * Set context data value.
     */
    public void setContextValue(String key, String value) {
        if (contextData != null) {
            contextData.put(key, value);
        }
    }
}

/**
 * Saga Result - represents the result of saga or step execution.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaResult {

    @Column(name = "saga_id", length = 36)
    private String sagaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SagaResultStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    /**
     * Check if result indicates success.
     */
    public boolean isSuccess() {
        return status == SagaResultStatus.SUCCESS;
    }

    /**
     * Check if result indicates failure.
     */
    public boolean isFailure() {
        return status == SagaResultStatus.FAILURE;
    }

    /**
     * Create successful result.
     */
    public static SagaResult success(String sagaId, String resultData) {
        return SagaResult.builder()
            .sagaId(sagaId)
            .status(SagaResultStatus.SUCCESS)
            .resultData(resultData)
            .build();
    }

    /**
     * Create failed result.
     */
    public static SagaResult failed(String sagaId, String errorMessage) {
        return SagaResult.builder()
            .sagaId(sagaId)
            .status(SagaResultStatus.FAILURE)
            .errorMessage(errorMessage)
            .build();
    }
}

/**
 * Saga Result Status enumeration.
 */
enum SagaResultStatus {
    SUCCESS,
    FAILURE,
    PENDING
}

/**
 * Saga Definition - defines the workflow and steps of a saga.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaDefinition {

    private String type;
    private String name;
    private String description;
    private List<SagaStep> steps;
    private long timeoutMs;
    private boolean enableParallelExecution;
    private int maxRetries;
    private long retryDelayMs;

    /**
     * Get step by name.
     */
    public SagaStep getStep(String stepName) {
        return steps.stream()
            .filter(step -> step.getName().equals(stepName))
            .findFirst()
            .orElse(null);
    }

    /**
     * Check if saga supports parallel execution.
     */
    public boolean supportsParallelExecution() {
        return enableParallelExecution;
    }
}

/**
 * Saga Step - defines an individual step in a saga workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {

    private String name;
    private String description;
    private SagaStepExecutor executor;
    private SagaStepCompensator compensator;
    private long timeout;
    private int maxRetries;
    private long retryDelay;
    private boolean mandatory;
    private List<String> dependencies;

    /**
     * Check if step is retryable based on result.
     */
    public boolean isRetryable(SagaResult result) {
        // Implement retry logic based on error type
        return result.getErrorMessage() != null && 
               !result.getErrorMessage().contains("PERMANENT_FAILURE");
    }

    /**
     * Check if step has dependencies.
     */
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }
}

/**
 * Saga Step Context - runtime context for step execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepContext {

    private String sagaId;
    private String stepName;
    private SagaContext sagaContext;
    private int retryCount;
    private long timeout;
    private SagaResult originalResult; // For compensation context
    private Map<String, Object> stepData;

    /**
     * Get step data value.
     */
    public Object getStepData(String key) {
        return stepData != null ? stepData.get(key) : null;
    }

    /**
     * Set step data value.
     */
    public void setStepData(String key, Object value) {
        if (stepData != null) {
            stepData.put(key, value);
        }
    }
}

/**
 * Functional interface for saga step execution.
 */
@FunctionalInterface
public interface SagaStepExecutor {
    SagaResult execute(SagaStepContext context) throws Exception;
}

/**
 * Functional interface for saga step compensation.
 */
@FunctionalInterface
public interface SagaStepCompensator {
    SagaResult compensate(SagaStepContext context) throws Exception;
}
