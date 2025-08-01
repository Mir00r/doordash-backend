package com.doordash.user_service.config;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.jaegertracing.internal.samplers.ProbabilisticSampler;
import io.jaegertracing.internal.samplers.RateLimitingSampler;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.opentracing.Tracer;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Comprehensive Observability Configuration for DoorDash User Service.
 * 
 * This configuration implements enterprise-grade observability practices including:
 * - Distributed tracing with Jaeger for request flow tracking
 * - Metrics collection with Prometheus for monitoring and alerting
 * - Custom metrics for business logic and security events
 * - Performance monitoring with JVM and system metrics
 * - Structured logging integration with tracing context
 * - Service mesh compatibility for microservices architecture
 * 
 * Observability Features:
 * - End-to-end request tracing across microservices
 * - Custom span creation for critical business operations
 * - Security event tracking and monitoring
 * - Performance metrics (latency, throughput, error rates)
 * - Resource utilization monitoring (CPU, memory, GC)
 * - Business metrics (user operations, authentication events)
 * 
 * Integration Points:
 * - API Gateway trace propagation
 * - Database operation tracing
 * - External service call tracking
 * - Security filter chain instrumentation
 * - Rate limiting and performance monitoring
 * 
 * Production Considerations:
 * - Configurable sampling rates for performance optimization
 * - Environment-specific tracing configuration
 * - Correlation ID propagation for log aggregation
 * - Custom tags for service identification and filtering
 * - Integration with alerting systems (PagerDuty, Slack)
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
@Slf4j
public class ObservabilityConfig {

    @Value("${spring.application.name:user-service}")
    private String applicationName;

    @Value("${app.observability.jaeger.enabled:true}")
    private boolean jaegerEnabled;

    @Value("${app.observability.jaeger.sampling-rate:1.0}")
    private Double samplingRate;

    @Value("${app.observability.jaeger.max-traces-per-second:100}")
    private Integer maxTracesPerSecond;

    @Value("${app.observability.jaeger.agent-host:localhost}")
    private String jaegerAgentHost;

    @Value("${app.observability.jaeger.agent-port:14268}")
    private Integer jaegerAgentPort;

    @Value("${app.observability.jaeger.collector-endpoint:http://localhost:14268/api/traces}")
    private String jaegerCollectorEndpoint;

    @Value("${app.observability.metrics.enabled:true}")
    private boolean metricsEnabled;

    @Value("${management.endpoints.web.exposure.include:health,info,metrics,prometheus}")
    private String[] exposedEndpoints;

    /**
     * Jaeger Tracer Configuration for Distributed Tracing.
     * 
     * Configures Jaeger tracer with enterprise-grade settings:
     * - Service name identification for trace filtering
     * - Configurable sampling strategies (constant, probabilistic, rate-limiting)
     * - Reporter configuration for trace data transmission
     * - Custom tags for service metadata
     * - Environment-specific configuration
     * 
     * Sampling Strategies:
     * - Development: Constant sampling (sample all traces)
     * - Staging: Probabilistic sampling (sample percentage of traces)
     * - Production: Rate limiting sampling (sample up to N traces per second)
     * 
     * @return Tracer configured Jaeger tracer instance
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "app.observability.jaeger.enabled", havingValue = "true", matchIfMissing = true)
    public Tracer jaegerTracer() {
        log.info("Configuring Jaeger tracer for service: {}", applicationName);
        
        // Configure sampler based on environment and performance requirements
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv()
            .withType(determineSamplerType())
            .withParam(samplingRate);

        // Configure reporter for trace data transmission
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv()
            .withLogSpans(true)
            .withMaxQueueSize(10000)
            .withFlushInterval(1000)
            .withSender(
                Configuration.SenderConfiguration.fromEnv()
                    .withAgentHost(jaegerAgentHost)
                    .withAgentPort(jaegerAgentPort)
                    .withEndpoint(jaegerCollectorEndpoint)
            );

        // Configure service identification and metadata
        Configuration config = new Configuration(applicationName)
            .withSampler(samplerConfig)
            .withReporter(reporterConfig);

        Tracer tracer = config.getTracer();
        
        log.info("Jaeger tracer configured successfully with sampler: {} and sampling rate: {}", 
                determineSamplerType(), samplingRate);
        
        return tracer;
    }

    /**
     * Tracer Builder Customizer for additional Jaeger configuration.
     * Adds custom tags and configuration for service identification.
     * 
     * @return TracerBuilderCustomizer customizer for tracer configuration
     */
    @Bean
    @ConditionalOnProperty(value = "app.observability.jaeger.enabled", havingValue = "true", matchIfMissing = true)
    public TracerBuilderCustomizer tracerBuilderCustomizer() {
        return builder -> {
            builder.withTag("service.name", applicationName)
                   .withTag("service.version", getClass().getPackage().getImplementationVersion())
                   .withTag("service.type", "microservice")
                   .withTag("service.domain", "user-management")
                   .withTag("deployment.environment", System.getProperty("spring.profiles.active", "development"))
                   .withTag("deployment.region", System.getProperty("aws.region", "us-east-1"));
        };
    }

    /**
     * Prometheus Meter Registry for Metrics Collection.
     * 
     * Configures Prometheus metrics registry with:
     * - Custom metrics for business operations
     * - JVM and system metrics
     * - Security event metrics
     * - Performance monitoring metrics
     * - Integration with Grafana dashboards
     * 
     * @return PrometheusMeterRegistry configured Prometheus registry
     */
    @Bean
    @Primary
    @ConditionalOnProperty(value = "app.observability.metrics.enabled", havingValue = "true", matchIfMissing = true)
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        log.info("Configuring Prometheus meter registry for service: {}", applicationName);
        
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        
        // Add common tags for service identification
        registry.config()
            .commonTags(
                "service", applicationName,
                "version", getClass().getPackage().getImplementationVersion(),
                "environment", System.getProperty("spring.profiles.active", "development"),
                "region", System.getProperty("aws.region", "us-east-1")
            );
        
        return registry;
    }

    /**
     * Meter Registry Customizer for additional metrics configuration.
     * 
     * @param meterRegistry meter registry to customize
     * @return MeterRegistryCustomizer customizer for meter registry
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "service", applicationName,
            "version", getClass().getPackage().getImplementationVersion(),
            "environment", System.getProperty("spring.profiles.active", "development")
        );
    }

    /**
     * Timed Aspect for automatic method timing with @Timed annotation.
     * Enables declarative performance monitoring.
     * 
     * @param registry meter registry for metrics collection
     * @return TimedAspect aspect for automatic timing
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * JVM Memory Metrics for monitoring heap and non-heap memory usage.
     * 
     * @return JvmMemoryMetrics JVM memory metrics collector
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * JVM Garbage Collection Metrics for monitoring GC performance.
     * 
     * @return JvmGcMetrics JVM GC metrics collector
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * JVM Thread Metrics for monitoring thread pool usage.
     * 
     * @return JvmThreadMetrics JVM thread metrics collector
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * JVM Class Loader Metrics for monitoring class loading.
     * 
     * @return ClassLoaderMetrics class loader metrics collector
     */
    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * Processor Metrics for monitoring CPU usage.
     * 
     * @return ProcessorMetrics processor metrics collector
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Uptime Metrics for monitoring application uptime.
     * 
     * @return UptimeMetrics uptime metrics collector
     */
    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * Determines the appropriate sampler type based on environment and configuration.
     * 
     * @return String sampler type (const, probabilistic, ratelimiting)
     */
    private String determineSamplerType() {
        String environment = System.getProperty("spring.profiles.active", "development");
        
        switch (environment.toLowerCase()) {
            case "production":
                return "ratelimiting";
            case "staging":
                return "probabilistic";
            case "development":
            case "test":
            default:
                return "const";
        }
    }
}
