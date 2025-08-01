package com.doordash.user_service.observability.tracing;

import com.doordash.user_service.observability.metrics.MetricsService;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP Request Tracing Interceptor for DoorDash User Service.
 * 
 * This interceptor provides comprehensive distributed tracing for HTTP requests including:
 * - Automatic span creation for incoming HTTP requests
 * - Request/response correlation with trace and span IDs
 * - HTTP method, URL, and status code tracking
 * - User context propagation for authenticated requests
 * - Performance monitoring with request duration metrics
 * - Error tracking and exception correlation
 * - Integration with security context for user identification
 * 
 * Tracing Features:
 * - Parent span extraction from incoming headers (B3, Jaeger, etc.)
 * - Custom tag injection for service identification and filtering
 * - Correlation ID generation for log aggregation
 * - Request payload size tracking for performance analysis
 * - Response status code and error tracking
 * - User authentication status and identity tracking
 * 
 * Integration Points:
 * - Spring Security context for user information
 * - Metrics service for performance monitoring
 * - Logging framework for trace correlation
 * - Rate limiting filter for security event tracking
 * - CORS filter for cross-origin request handling
 * 
 * Performance Considerations:
 * - Lightweight span creation with minimal overhead
 * - Async-friendly with proper scope management
 * - Memory-efficient with automatic cleanup
 * - Configurable sampling for production environments
 * - Integration with load balancer health checks exclusion
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TracingInterceptor implements HandlerInterceptor {

    private final Tracer tracer;
    private final DistributedTracingService tracingService;
    private final MetricsService metricsService;

    // Request attribute keys for span management
    private static final String SPAN_ATTRIBUTE = "tracing.span";
    private static final String SCOPE_ATTRIBUTE = "tracing.scope";
    private static final String START_TIME_ATTRIBUTE = "tracing.start_time";

    // Standard HTTP and tracing headers
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String SPAN_ID_HEADER = "X-Span-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    // Paths to exclude from tracing (health checks, static resources)
    private static final String[] EXCLUDED_PATHS = {
        "/actuator/health",
        "/actuator/info", 
        "/actuator/prometheus",
        "/favicon.ico",
        "/static/",
        "/css/",
        "/js/",
        "/images/"
    };

    /**
     * Pre-handle method called before request processing.
     * Creates a new span for the incoming HTTP request and sets up tracing context.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param handler request handler
     * @return boolean true to continue processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Skip tracing for excluded paths
        if (shouldExcludeFromTracing(request)) {
            log.debug("Skipping tracing for excluded path: {}", request.getRequestURI());
            return true;
        }

        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        try {
            // Extract or generate correlation ID
            String correlationId = extractOrGenerateCorrelationId(request);
            
            // Create span for HTTP request
            Span span = createHttpRequestSpan(request, correlationId);
            Scope scope = tracer.scopeManager().activate(span);

            // Store span and scope in request attributes for cleanup
            request.setAttribute(SPAN_ATTRIBUTE, span);
            request.setAttribute(SCOPE_ATTRIBUTE, scope);

            // Add request headers for downstream services
            addTracingHeaders(response, span, correlationId);

            // Log request start with trace context
            log.info("Starting HTTP request: {} {} - TraceID: {} SpanID: {} CorrelationID: {}",
                request.getMethod(), request.getRequestURI(),
                tracingService.getCurrentTraceId(), tracingService.getCurrentSpanId(), correlationId);

            // Record request metrics
            metricsService.incrementCounter("doordash.user.http.requests.total",
                "method", request.getMethod(),
                "endpoint", extractEndpoint(request.getRequestURI()));

        } catch (Exception e) {
            log.error("Error in tracing interceptor preHandle", e);
            // Continue processing even if tracing fails
        }

        return true;
    }

    /**
     * Post-handle method called after request processing but before view rendering.
     * Updates span with response information and performance metrics.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param handler request handler
     * @param modelAndView model and view object
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
                          org.springframework.web.servlet.ModelAndView modelAndView) {
        
        if (shouldExcludeFromTracing(request)) {
            return;
        }

        try {
            Span span = (Span) request.getAttribute(SPAN_ATTRIBUTE);
            if (span != null) {
                // Add response information to span
                addResponseInformationToSpan(span, response);
                
                // Record performance metrics
                recordPerformanceMetrics(request, response);
                
                log.debug("Updated span with response information: status={}", response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error in tracing interceptor postHandle", e);
        }
    }

    /**
     * After-completion method called after request completion.
     * Finalizes span, records metrics, and cleans up tracing context.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param handler request handler
     * @param ex exception if any occurred during request processing
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        
        if (shouldExcludeFromTracing(request)) {
            return;
        }

        try {
            Span span = (Span) request.getAttribute(SPAN_ATTRIBUTE);
            Scope scope = (Scope) request.getAttribute(SCOPE_ATTRIBUTE);
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);

            if (span != null) {
                // Record exception if any
                if (ex != null) {
                    tracingService.recordError(span, ex);
                    
                    // Record error metrics
                    metricsService.incrementCounter("doordash.user.http.errors.total",
                        "method", request.getMethod(),
                        "endpoint", extractEndpoint(request.getRequestURI()),
                        "error_type", ex.getClass().getSimpleName());
                }

                // Record final metrics and finish span
                recordFinalMetrics(request, response, startTime);
                
                // Finish span
                span.finish();
                
                log.info("Completed HTTP request: {} {} - Status: {} Duration: {}ms - TraceID: {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(),
                    startTime != null ? System.currentTimeMillis() - startTime : "unknown",
                    tracingService.getCurrentTraceId());
            }

            // Clean up scope
            if (scope != null) {
                scope.close();
            }

        } catch (Exception e) {
            log.error("Error in tracing interceptor afterCompletion", e);
        }
    }

    /**
     * Creates a new span for HTTP request with comprehensive tagging.
     * 
     * @param request HTTP servlet request
     * @param correlationId correlation ID for request tracking
     * @return Span created span for the HTTP request
     */
    private Span createHttpRequestSpan(HttpServletRequest request, String correlationId) {
        String operationName = String.format("%s %s", request.getMethod(), extractEndpoint(request.getRequestURI()));
        
        Span span = tracer.buildSpan(operationName)
            .withTag(Tags.COMPONENT, "http-server")
            .withTag(Tags.HTTP_METHOD, request.getMethod())
            .withTag(Tags.HTTP_URL, request.getRequestURL().toString())
            .withTag("http.user_agent", request.getHeader("User-Agent"))
            .withTag("http.remote_addr", getClientIpAddress(request))
            .withTag("service.name", "user-service")
            .withTag("correlation.id", correlationId)
            .withTag("endpoint", extractEndpoint(request.getRequestURI()))
            .start();

        // Add user context if available from security context
        addUserContextToSpan(span, request);
        
        // Add custom business tags
        addBusinessContextToSpan(span, request);

        return span;
    }

    /**
     * Adds response information to the span including status code and size.
     * 
     * @param span span to update
     * @param response HTTP servlet response
     */
    private void addResponseInformationToSpan(Span span, HttpServletResponse response) {
        span.setTag(Tags.HTTP_STATUS, response.getStatus());
        
        // Mark as error if status code indicates error
        if (response.getStatus() >= 400) {
            span.setTag(Tags.ERROR, true);
        }

        // Add response size if available
        String contentLength = response.getHeader("Content-Length");
        if (contentLength != null) {
            try {
                span.setTag("http.response.size", Long.parseLong(contentLength));
            } catch (NumberFormatException e) {
                log.debug("Invalid Content-Length header: {}", contentLength);
            }
        }
    }

    /**
     * Adds user context information to span from security context.
     * 
     * @param span span to update
     * @param request HTTP servlet request
     */
    private void addUserContextToSpan(Span span, HttpServletRequest request) {
        try {
            // Extract user information from security context
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                span.setTag("user.authenticated", true);
                span.setTag("user.name", authentication.getName());
                
                // Add user ID if available from custom authentication token
                if (authentication instanceof com.doordash.user_service.security.jwt.DoorDashJwtAuthenticationToken) {
                    com.doordash.user_service.security.jwt.DoorDashJwtAuthenticationToken jwtToken = 
                        (com.doordash.user_service.security.jwt.DoorDashJwtAuthenticationToken) authentication;
                    
                    if (jwtToken.getUserId() != null) {
                        span.setTag("user.id", jwtToken.getUserId());
                    }
                    
                    if (jwtToken.getServiceName() != null) {
                        span.setTag("user.service", jwtToken.getServiceName());
                    }
                }
                
                // Add authorities
                String authorities = authentication.getAuthorities().stream()
                    .map(Object::toString)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
                span.setTag("user.authorities", authorities);
            } else {
                span.setTag("user.authenticated", false);
            }
        } catch (Exception e) {
            log.debug("Error adding user context to span", e);
        }
    }

    /**
     * Adds business context information to span for analytics and monitoring.
     * 
     * @param span span to update
     * @param request HTTP servlet request
     */
    private void addBusinessContextToSpan(Span span, HttpServletRequest request) {
        // Add API version if present in URL
        String uri = request.getRequestURI();
        if (uri.contains("/api/v")) {
            String version = uri.substring(uri.indexOf("/api/v") + 6, uri.indexOf("/", uri.indexOf("/api/v") + 6));
            span.setTag("api.version", version);
        }

        // Add request source information
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            if (userAgent.contains("Mobile")) {
                span.setTag("client.type", "mobile");
            } else if (userAgent.contains("Postman")) {
                span.setTag("client.type", "api-testing");
            } else {
                span.setTag("client.type", "web");
            }
        }

        // Add tenant information if available
        String tenant = request.getHeader("X-Tenant-ID");
        if (tenant != null) {
            span.setTag("tenant.id", tenant);
        }
    }

    /**
     * Records performance metrics for the HTTP request.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     */
    private void recordPerformanceMetrics(HttpServletRequest request, HttpServletResponse response) {
        String method = request.getMethod();
        String endpoint = extractEndpoint(request.getRequestURI());
        String status = String.valueOf(response.getStatus());

        // Record response status metrics
        metricsService.incrementCounter("doordash.user.http.responses.total",
            "method", method, "endpoint", endpoint, "status", status);

        // Record error metrics for 4xx and 5xx responses
        if (response.getStatus() >= 400) {
            String errorCategory = response.getStatus() < 500 ? "client_error" : "server_error";
            metricsService.incrementCounter("doordash.user.http.errors.by_category",
                "method", method, "endpoint", endpoint, "category", errorCategory);
        }
    }

    /**
     * Records final metrics after request completion.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param startTime request start time
     */
    private void recordFinalMetrics(HttpServletRequest request, HttpServletResponse response, Long startTime) {
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String endpoint = extractEndpoint(request.getRequestURI());

            // Record request duration
            metricsService.recordTimer("doordash.user.http.request.duration", 
                duration, java.util.concurrent.TimeUnit.MILLISECONDS,
                "method", method, "endpoint", endpoint);

            // Record slow request metrics
            if (duration > 2000) { // Requests slower than 2 seconds
                metricsService.incrementCounter("doordash.user.http.slow_requests.total",
                    "method", method, "endpoint", endpoint);
            }
        }
    }

    /**
     * Extracts or generates correlation ID for request tracking.
     * 
     * @param request HTTP servlet request
     * @return String correlation ID
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        // Try to extract from headers in order of preference
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = request.getHeader(REQUEST_ID_HEADER);
        }
        if (correlationId == null) {
            correlationId = request.getHeader("X-Amzn-Trace-Id");
        }
        
        // Generate new correlation ID if not found
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        return correlationId;
    }

    /**
     * Adds tracing headers to response for downstream service propagation.
     * 
     * @param response HTTP servlet response
     * @param span current span
     * @param correlationId correlation ID
     */
    private void addTracingHeaders(HttpServletResponse response, Span span, String correlationId) {
        response.setHeader(TRACE_ID_HEADER, span.context().toTraceId());
        response.setHeader(SPAN_ID_HEADER, span.context().toSpanId());
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
    }

    /**
     * Extracts client IP address from request headers and remote address.
     * 
     * @param request HTTP servlet request
     * @return String client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Extracts normalized endpoint from request URI for consistent tagging.
     * 
     * @param requestURI request URI
     * @return String normalized endpoint
     */
    private String extractEndpoint(String requestURI) {
        // Remove query parameters
        if (requestURI.contains("?")) {
            requestURI = requestURI.substring(0, requestURI.indexOf("?"));
        }
        
        // Normalize path parameters (replace UUIDs and IDs with placeholders)
        requestURI = requestURI.replaceAll("/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "/{uuid}");
        requestURI = requestURI.replaceAll("/\\d+", "/{id}");
        
        return requestURI;
    }

    /**
     * Determines if the request should be excluded from tracing.
     * 
     * @param request HTTP servlet request
     * @return boolean true if should be excluded
     */
    private boolean shouldExcludeFromTracing(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String excludedPath : EXCLUDED_PATHS) {
            if (uri.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}
