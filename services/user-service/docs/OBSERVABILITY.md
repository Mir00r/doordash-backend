# DoorDash User Service - Observability Implementation Guide

## 🔍 **Overview**

This document describes the comprehensive observability implementation for the DoorDash User Service, including distributed tracing with Jaeger, metrics collection with Prometheus, and alerting with Grafana. The implementation follows enterprise-grade observability practices and is designed for production environments at scale.

## 🏗️ **Architecture**

### **Observability Stack**
- **Distributed Tracing**: Jaeger with OpenTracing
- **Metrics Collection**: Prometheus with Micrometer
- **Visualization**: Grafana dashboards
- **Alerting**: Prometheus AlertManager
- **Log Aggregation**: Structured logging with trace correlation

### **Components**

#### **1. Distributed Tracing Service**
```java
@Service
public class DistributedTracingService {
    // Comprehensive tracing capabilities
    // - Custom span creation and management
    // - Error tracking and correlation
    // - Performance monitoring
    // - Security event tracing
}
```

#### **2. Metrics Service**
```java
@Service
public class MetricsService {
    // Enterprise-grade metrics collection
    // - Business metrics
    // - Performance metrics
    // - Security metrics
    // - System resource metrics
}
```

#### **3. Tracing Interceptor**
```java
@Component
public class TracingInterceptor implements HandlerInterceptor {
    // Automatic HTTP request instrumentation
    // - Request/response correlation
    // - User context propagation
    // - Performance tracking
}
```

## 🚀 **Features**

### **Distributed Tracing**
- ✅ **End-to-end request tracing** across microservices
- ✅ **Custom span creation** for business operations
- ✅ **Error tracking and correlation** with detailed stack traces
- ✅ **User context propagation** with security information
- ✅ **Database operation tracing** with performance metrics
- ✅ **HTTP client tracing** for external service calls
- ✅ **Configurable sampling** for production optimization

### **Metrics Collection**
- ✅ **Business metrics** (registrations, authentications, engagement)
- ✅ **Performance metrics** (latency, throughput, error rates)
- ✅ **Security metrics** (failed logins, rate limiting, security events)
- ✅ **System metrics** (CPU, memory, GC, thread pools)
- ✅ **Custom metrics** with flexible tagging
- ✅ **Prometheus exposition** format compatibility

### **Alerting & Monitoring**
- ✅ **Comprehensive alerting rules** for all critical metrics
- ✅ **Multi-severity levels** (Critical, Warning, Info)
- ✅ **Business intelligence alerts** for unusual patterns
- ✅ **Integration with PagerDuty** and Slack
- ✅ **Grafana dashboards** with real-time visualization
- ✅ **SLA monitoring** and reporting

## 📊 **Metrics Categories**

### **User Service Metrics**
```yaml
# User Registration
doordash_user_registration_total{status="success|failure", method="email|social"}

# User Authentication
doordash_user_authentication_total{status="success|failure", method="password|oauth"}

# Profile Operations
doordash_user_profile_operation_duration_seconds{operation="view|update|delete"}
```

### **Security Metrics**
```yaml
# Security Events
doordash_security_events_total{event_type, severity, user_id, source_ip}

# JWT Operations
doordash_security_jwt_operation_total{operation="validate|refresh", status}

# Rate Limiting
doordash_security_rate_limit_exceeded_total{type="ip|user|api_key"}
```

### **Performance Metrics**
```yaml
# Database Operations
doordash_performance_database_operation_duration_seconds{operation, table}

# HTTP Client Requests
doordash_performance_http_client_request_duration_seconds{method, service}

# Cache Operations
doordash_performance_cache_operation_total{operation="hit|miss|eviction", cache}
```

### **System Metrics**
```yaml
# Memory Usage
doordash_system_memory_used{type="heap|non_heap"}
doordash_system_memory_usage_percent{type}

# Thread Pools
doordash_system_threadpool_active{pool}
doordash_system_threadpool_queue_size{pool}

# Active Sessions
doordash_system_sessions_active
```

## 🔧 **Configuration**

### **Environment Variables**
```bash
# Jaeger Configuration
JAEGER_SERVICE_NAME=user-service
JAEGER_AGENT_HOST=jaeger-agent
JAEGER_AGENT_PORT=6831
JAEGER_COLLECTOR_ENDPOINT=http://jaeger-collector:14268/api/traces
JAEGER_SAMPLER_TYPE=probabilistic
JAEGER_SAMPLER_PARAM=0.1

# Prometheus Configuration
PROMETHEUS_URL=http://prometheus:9090
METRICS_EXPORT_ENABLED=true

# AlertManager Configuration
ALERT_MANAGER_URL=http://alertmanager:9093
SLACK_WEBHOOK_URL=https://hooks.slack.com/your-webhook
PAGERDUTY_INTEGRATION_KEY=your-pagerduty-key
```

### **Application Properties**
```yaml
app:
  observability:
    jaeger:
      enabled: true
      sampling-rate: 0.1
      max-traces-per-second: 100
    metrics:
      enabled: true
      custom:
        business-metrics: true
        security-metrics: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

## 📈 **Monitoring Dashboards**

### **Grafana Dashboard Panels**

#### **Service Health Overview**
- Service uptime and availability
- Request rate and error rate
- Response time percentiles (50th, 95th, 99th)
- Active instances and health status

#### **Security Monitoring**
- Authentication success/failure rates
- Security event frequencies
- Rate limiting statistics
- JWT token validation metrics

#### **Performance Metrics**
- Database operation latency
- HTTP client request performance
- Cache hit/miss ratios
- Thread pool utilization

#### **Business Intelligence**
- User registration trends
- Authentication patterns
- Profile update frequencies
- User engagement metrics

#### **System Resources**
- JVM memory usage (heap/non-heap)
- Garbage collection metrics
- CPU utilization
- Thread pool status

## 🚨 **Alerting Rules**

### **Critical Alerts**
```yaml
# Service Down
- alert: UserServiceDown
  expr: up{job="user-service"} == 0
  for: 30s
  severity: critical

# High Error Rate
- alert: UserServiceHighErrorRate
  expr: rate(doordash_user_authentication_total{status="failure"}[5m]) / rate(doordash_user_authentication_total[5m]) > 0.05
  for: 2m
  severity: critical

# Authentication Failure Spike
- alert: UserServiceAuthenticationFailureSpike
  expr: rate(doordash_security_events_total{event_type="authentication_failure"}[5m]) > 10
  for: 1m
  severity: critical
```

### **Warning Alerts**
```yaml
# High Latency
- alert: UserServiceHighLatency
  expr: histogram_quantile(0.95, rate(doordash_user_operation_duration_seconds_bucket[5m])) > 2
  for: 5m
  severity: warning

# Rate Limit Exceeded
- alert: UserServiceRateLimitExceeded
  expr: rate(doordash_security_rate_limit_exceeded_total[5m]) > 5
  for: 2m
  severity: warning
```

## 🔄 **Deployment**

### **Docker Compose Setup**
```yaml
version: '3.8'
services:
  jaeger:
    image: jaegertracing/all-in-one:1.35
    ports:
      - "16686:16686"
      - "14268:14268"
    environment:
      - COLLECTOR_OTLP_ENABLED=true

  prometheus:
    image: prom/prometheus:v2.37.0
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus:/etc/prometheus

  grafana:
    image: grafana/grafana:9.0.0
    ports:
      - "3000:3000"
    volumes:
      - ./monitoring/grafana:/etc/grafana
```

### **Kubernetes Deployment**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  template:
    spec:
      containers:
      - name: user-service
        image: doordash/user-service:latest
        env:
        - name: JAEGER_AGENT_HOST
          value: "jaeger-agent"
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        ports:
        - containerPort: 8080
        - containerPort: 8081  # Actuator port
```

## 📋 **Usage Examples**

### **Custom Tracing**
```java
@Service
public class UserService {
    
    @Autowired
    private DistributedTracingService tracingService;
    
    public User getUserProfile(String userId) {
        return tracingService.traceUserOperation("get_profile", () -> {
            tracingService.addTag("user.id", userId);
            return userRepository.findById(userId);
        }, userId);
    }
}
```

### **Custom Metrics**
```java
@Service
public class UserRegistrationService {
    
    @Autowired
    private MetricsService metricsService;
    
    public void registerUser(RegistrationRequest request) {
        metricsService.timeUserOperation("registration", () -> {
            // Registration logic
            User user = createUser(request);
            
            metricsService.recordUserRegistration(true, request.getMethod());
            metricsService.recordBusinessMetric("new_user_registration", 1.0, 
                Map.of("method", request.getMethod(), "region", request.getRegion()));
            
            return user;
        }, null);
    }
}
```

### **Database Tracing**
```java
@Repository
public class UserRepository {
    
    @Autowired
    private DistributedTracingService tracingService;
    
    public User findById(String userId) {
        return tracingService.traceDatabaseOperation("select", 
            "SELECT * FROM users WHERE id = ?", () -> {
                return jdbcTemplate.queryForObject(sql, userRowMapper, userId);
            });
    }
}
```

## 🔍 **Troubleshooting**

### **Common Issues**

#### **Jaeger Not Receiving Traces**
```bash
# Check Jaeger agent connectivity
curl http://jaeger-agent:14268/api/traces

# Verify service configuration
kubectl logs user-service-pod | grep jaeger
```

#### **Metrics Not Appearing in Prometheus**
```bash
# Check metrics endpoint
curl http://user-service:8081/actuator/prometheus

# Verify Prometheus configuration
curl http://prometheus:9090/api/v1/targets
```

#### **High Memory Usage**
```yaml
# Reduce sampling rate in production
jaeger:
  sampler:
    type: probabilistic
    param: 0.01  # 1% sampling
```

### **Performance Optimization**

#### **Sampling Configuration**
```yaml
# Development
jaeger:
  sampler:
    type: const
    param: 1  # Sample all traces

# Staging
jaeger:
  sampler:
    type: probabilistic
    param: 0.1  # Sample 10% of traces

# Production
jaeger:
  sampler:
    type: ratelimiting
    param: 100  # Max 100 traces per second
```

## 📚 **Best Practices**

### **Tracing**
1. **Use meaningful operation names** that describe the business operation
2. **Add relevant tags** for filtering and analysis
3. **Include user context** for security and audit trails
4. **Handle errors gracefully** with proper error recording
5. **Use appropriate sampling** for production environments

### **Metrics**
1. **Follow consistent naming conventions** with service prefixes
2. **Use appropriate metric types** (counter, timer, gauge, summary)
3. **Add meaningful tags** for aggregation and filtering
4. **Monitor business metrics** alongside technical metrics
5. **Set up proactive alerting** for critical thresholds

### **Alerting**
1. **Define clear severity levels** and response procedures
2. **Avoid alert fatigue** with proper thresholds
3. **Include runbook links** in alert annotations
4. **Test alerting rules** regularly
5. **Monitor alerting system health**

## 🤝 **Integration with Other Services**

### **API Gateway Integration**
- Trace context propagation via HTTP headers
- Correlation ID generation and forwarding
- Centralized metrics collection

### **Auth Service Integration**
- JWT token validation tracing
- Security event correlation
- User context propagation

### **Database Integration**
- Query performance monitoring
- Connection pool metrics
- Transaction tracing

### **External Services Integration**
- HTTP client instrumentation
- Circuit breaker metrics
- Dependency health monitoring

## 📊 **Monitoring Checklist**

### **Production Readiness**
- [ ] Jaeger agent deployed and configured
- [ ] Prometheus scraping user service metrics
- [ ] Grafana dashboards imported and configured
- [ ] AlertManager rules deployed
- [ ] Notification channels configured (Slack, PagerDuty)
- [ ] Log aggregation with trace correlation
- [ ] SLA definitions and monitoring
- [ ] Runbooks created for common issues
- [ ] Team training on observability tools
- [ ] Regular review and optimization of sampling rates

---

## 📞 **Support**

For questions or issues with the observability implementation:
- **Team**: DoorDash Backend Team
- **Slack**: `#doordash-observability`
- **Documentation**: [Internal Wiki](https://wiki.doordash.com/observability)
- **Runbooks**: [Incident Response Guide](https://docs.doordash.com/runbooks)
