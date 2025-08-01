package com.doordash.user_service.observability;

import com.doordash.user_service.config.ObservabilityConfig;
import com.doordash.user_service.observability.metrics.MetricsService;
import com.doordash.user_service.observability.tracing.DistributedTracingService;
import com.doordash.user_service.observability.tracing.TracingInterceptor;
import io.jaegertracing.Configuration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive Integration Tests for Observability Components.
 * 
 * Tests the complete observability infrastructure including:
 * - Distributed tracing with Jaeger integration
 * - Metrics collection with Prometheus
 * - HTTP request instrumentation
 * - Security event tracking
 * - Performance monitoring
 * - Error handling and recovery
 * 
 * Test Scenarios:
 * - End-to-end request tracing across components
 * - Metrics collection and aggregation
 * - Trace context propagation
 * - Error tracking and correlation
 * - Performance monitoring and alerting
 * - Security event auditing
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {ObservabilityConfig.class})
@ActiveProfiles("test")
@DisplayName("Observability Integration Tests")
class ObservabilityIntegrationTest {

    @Mock
    private MeterRegistry meterRegistry;

    private MockTracer mockTracer;
    private DistributedTracingService tracingService;
    private MetricsService metricsService;
    private TracingInterceptor tracingInterceptor;

    @BeforeEach
    void setUp() {
        mockTracer = new MockTracer();
        tracingService = new DistributedTracingService(mockTracer);
        metricsService = new MetricsService(meterRegistry);
        tracingInterceptor = new TracingInterceptor(mockTracer, tracingService, metricsService);
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create and manage distributed traces for user operations")
    void shouldCreateAndManageDistributedTracesForUserOperations() {
        // Given
        String operationName = "user.authentication";
        String userId = "user123";

        // When
        String result = tracingService.traceUserAuthentication(userId, "login", () -> {
            // Simulate authentication operation
            tracingService.addTag("auth.method", "password");
            tracingService.addLog("Authentication started");
            return "authentication_success";
        });

        // Then
        assertThat(result).isEqualTo("authentication_success");
        assertThat(mockTracer.finishedSpans()).hasSize(1);
        
        MockTracer.MockSpan span = mockTracer.finishedSpans().get(0);
        assertThat(span.operationName()).isEqualTo("user.authentication.login");
        assertThat(span.tags()).containsEntry("user.id", userId);
        assertThat(span.tags()).containsEntry("security.event", "authentication");
        assertThat(span.tags()).containsEntry("component", "security");
        assertThat(span.logEntries()).isNotEmpty();
    }

    @Test
    @DisplayName("Should trace database operations with performance metrics")
    void shouldTraceDatabaseOperationsWithPerformanceMetrics() {
        // Given
        String operation = "select";
        String query = "SELECT * FROM users WHERE id = ?";

        // When
        String result = tracingService.traceDatabaseOperation(operation, query, () -> {
            // Simulate database operation
            try {
                Thread.sleep(50); // Simulate DB latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "query_result";
        });

        // Then
        assertThat(result).isEqualTo("query_result");
        assertThat(mockTracer.finishedSpans()).hasSize(1);
        
        MockTracer.MockSpan span = mockTracer.finishedSpans().get(0);
        assertThat(span.operationName()).isEqualTo("db.select");
        assertThat(span.tags()).containsEntry("db.type", "postgresql");
        assertThat(span.tags()).containsEntry("db.statement", query);
        assertThat(span.tags()).containsEntry("component", "database");
        assertThat(span.tags()).containsKey("db.duration_ms");
    }

    @Test
    @DisplayName("Should handle errors in traced operations")
    void shouldHandleErrorsInTracedOperations() {
        // Given
        RuntimeException testException = new RuntimeException("Test error");

        // When & Then
        assertThatThrownBy(() -> 
            tracingService.traceOperation("error.operation", () -> {
                throw testException;
            })
        ).isInstanceOf(RuntimeException.class)
         .hasMessage("Test error");

        // Verify error was recorded in span
        assertThat(mockTracer.finishedSpans()).hasSize(1);
        MockTracer.MockSpan span = mockTracer.finishedSpans().get(0);
        assertThat(span.tags()).containsEntry("error", true);
        assertThat(span.tags()).containsEntry("error.kind", "RuntimeException");
        assertThat(span.tags()).containsEntry("error.object", "Test error");
    }

    @Test
    @DisplayName("Should collect metrics for user service operations")
    void shouldCollectMetricsForUserServiceOperations() {
        // Given
        when(meterRegistry.counter(anyString(), any(String[].class)))
            .thenReturn(mock(io.micrometer.core.instrument.Counter.class));

        // When
        metricsService.recordUserRegistration(true, "email");
        metricsService.recordUserAuthentication(true, "password", "user123");
        metricsService.recordSecurityEvent("login_success", "low", "user123", "192.168.1.100");

        // Then
        verify(meterRegistry, times(3)).counter(anyString(), any(String[].class));
    }

    @Test
    @DisplayName("Should record performance metrics with timers")
    void shouldRecordPerformanceMetricsWithTimers() {
        // Given
        Timer mockTimer = mock(Timer.class);
        when(meterRegistry.timer(anyString(), any(String[].class))).thenReturn(mockTimer);
        when(mockTimer.recordCallable(any())).thenReturn("operation_result");

        // When
        String result = metricsService.timeUserOperation("profile_update", () -> "operation_result", "user123");

        // Then
        assertThat(result).isEqualTo("operation_result");
        verify(meterRegistry).timer(eq("doordash.user.operation.duration"), any(String[].class));
        verify(mockTimer).recordCallable(any());
    }

    @Test
    @DisplayName("Should integrate tracing with HTTP request interceptor")
    void shouldIntegrateTracingWithHttpRequestInterceptor() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/v1/users/profile");
        request.addHeader("X-Correlation-ID", "test-correlation-id");
        request.setRemoteAddr("192.168.1.100");

        MockHttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();

        // Set up authenticated user context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        boolean preHandleResult = tracingInterceptor.preHandle(request, response, handler);
        tracingInterceptor.postHandle(request, response, handler, null);
        tracingInterceptor.afterCompletion(request, response, handler, null);

        // Then
        assertThat(preHandleResult).isTrue();
        assertThat(mockTracer.finishedSpans()).hasSize(1);
        
        MockTracer.MockSpan span = mockTracer.finishedSpans().get(0);
        assertThat(span.operationName()).isEqualTo("GET /api/v1/users/profile");
        assertThat(span.tags()).containsEntry("http.method", "GET");
        assertThat(span.tags()).containsEntry("correlation.id", "test-correlation-id");
        assertThat(span.tags()).containsEntry("user.authenticated", true);
        assertThat(span.tags()).containsEntry("user.name", "testuser");
    }

    @Test
    @DisplayName("Should handle HTTP errors in tracing interceptor")
    void shouldHandleHttpErrorsInTracingInterceptor() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/v1/users/invalid");

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);
        
        RuntimeException testException = new RuntimeException("Validation error");

        // When
        tracingInterceptor.preHandle(request, response, new Object());
        tracingInterceptor.postHandle(request, response, new Object(), null);
        tracingInterceptor.afterCompletion(request, response, new Object(), testException);

        // Then
        assertThat(mockTracer.finishedSpans()).hasSize(1);
        MockTracer.MockSpan span = mockTracer.finishedSpans().get(0);
        assertThat(span.tags()).containsEntry("http.status", 400);
        assertThat(span.tags()).containsEntry("error", true);
        assertThat(span.tags()).containsEntry("error.kind", "RuntimeException");
    }

    @Test
    @DisplayName("Should propagate trace context across service boundaries")
    void shouldPropagateTraceContextAcrossServiceBoundaries() {
        // Given
        Span parentSpan = tracingService.createSpan("parent.operation");
        
        try (Scope scope = mockTracer.scopeManager().activate(parentSpan)) {
            // When
            Span childSpan = tracingService.createChildSpan("child.operation");
            
            try (Scope childScope = mockTracer.scopeManager().activate(childSpan)) {
                tracingService.addTag("operation.type", "service_call");
                tracingService.addLog("Child operation executed");
            } finally {
                childSpan.finish();
            }
        } finally {
            parentSpan.finish();
        }

        // Then
        assertThat(mockTracer.finishedSpans()).hasSize(2);
        
        MockTracer.MockSpan child = mockTracer.finishedSpans().get(0);
        MockTracer.MockSpan parent = mockTracer.finishedSpans().get(1);
        
        assertThat(child.operationName()).isEqualTo("child.operation");
        assertThat(parent.operationName()).isEqualTo("parent.operation");
        assertThat(child.parentId()).isEqualTo(parent.context().spanId());
    }

    @Test
    @DisplayName("Should record business metrics with custom dimensions")
    void shouldRecordBusinessMetricsWithCustomDimensions() {
        // Given
        when(meterRegistry.summary(anyString(), any(String[].class)))
            .thenReturn(mock(io.micrometer.core.instrument.DistributionSummary.class));

        Map<String, String> dimensions = Map.of(
            "operation_type", "profile_update",
            "user_tier", "premium",
            "region", "us-east-1"
        );

        // When
        metricsService.recordBusinessMetric("user_engagement", 15.5, dimensions);

        // Then
        verify(meterRegistry).summary(eq("doordash.business.user_engagement"), any(String[].class));
    }

    @Test
    @DisplayName("Should monitor system resources with gauge metrics")
    void shouldMonitorSystemResourcesWithGaugeMetrics() {
        // Given
        AtomicLong sessionCounter = new AtomicLong(100);
        when(meterRegistry.gauge(anyString(), any(), any())).thenReturn(100.0);

        // When
        metricsService.recordActiveSessionCount(sessionCounter);
        metricsService.recordMemoryUsage("heap", 1024000000L, 2048000000L);
        metricsService.recordThreadPoolMetrics("http-pool", 10, 5);

        // Then
        verify(meterRegistry, atLeast(1)).gauge(anyString(), any(), any());
    }

    @Test
    @DisplayName("Should handle trace sampling and performance optimization")
    void shouldHandleTraceSamplingAndPerformanceOptimization() {
        // Given
        String highVolumeOperation = "high.volume.operation";

        // When - Execute many operations to test sampling
        for (int i = 0; i < 100; i++) {
            tracingService.traceOperation(highVolumeOperation, () -> "result_" + i);
        }

        // Then - Verify all operations were traced (MockTracer samples everything)
        assertThat(mockTracer.finishedSpans()).hasSize(100);
        
        // Verify span names are consistent
        mockTracer.finishedSpans().forEach(span -> 
            assertThat(span.operationName()).isEqualTo(highVolumeOperation)
        );
    }

    @Test
    @DisplayName("Should integrate security events with distributed tracing")
    void shouldIntegrateSecurityEventsWithDistributedTracing() {
        // Given
        String userId = "user123";
        String sourceIp = "192.168.1.100";

        // When
        tracingService.traceUserAuthentication(userId, "failed_login", () -> {
            // Simulate failed authentication
            tracingService.addTag("auth.failure_reason", "invalid_password");
            tracingService.addTag("source.ip", sourceIp);
            
            // Record security event
            metricsService.recordSecurityEvent("authentication_failure", "medium", userId, sourceIp);
            
            return "authentication_failed";
        });

        // Then
        assertThat(mockTracer.finishedSpans()).hasSize(1);
        MockTracer.MockSpan span = mockTracer.finishedSpans().get(0);
        assertThat(span.tags()).containsEntry("user.id", userId);
        assertThat(span.tags()).containsEntry("auth.failure_reason", "invalid_password");
        assertThat(span.tags()).containsEntry("source.ip", sourceIp);
        assertThat(span.tags()).containsEntry("security.event", "authentication");
        
        verify(meterRegistry).counter(anyString(), any(String[].class));
    }

    @Test
    @DisplayName("Should maintain trace context in async operations")
    void shouldMaintainTraceContextInAsyncOperations() {
        // Given
        Span mainSpan = tracingService.createSpan("async.main.operation");
        
        try (Scope scope = mockTracer.scopeManager().activate(mainSpan)) {
            String traceId = tracingService.getCurrentTraceId();
            String spanId = tracingService.getCurrentSpanId();
            
            // When - Simulate async operation
            tracingService.traceOperation("async.sub.operation", () -> {
                // Verify trace context is maintained
                assertThat(tracingService.getCurrentTraceId()).isNotNull();
                assertThat(tracingService.getCurrentSpanId()).isNotNull();
                
                return "async_result";
            });
            
            // Then
            assertThat(tracingService.getCurrentTraceId()).isEqualTo(traceId);
            assertThat(tracingService.getCurrentSpanId()).isEqualTo(spanId);
        } finally {
            mainSpan.finish();
        }
    }
}
