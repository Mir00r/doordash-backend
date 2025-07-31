package com.doordash.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Health check controller for API Gateway and dependent services.
 */
@RestController
@RequestMapping("/actuator")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

    private final DiscoveryClient discoveryClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    /**
     * Basic health check endpoint
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "api-gateway");
        health.put("version", "1.0.0");
        
        return Mono.just(ResponseEntity.ok(health));
    }

    /**
     * Detailed health check including dependent services
     */
    @GetMapping("/health/detailed")
    public Mono<ResponseEntity<Map<String, Object>>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "api-gateway");
        
        return checkRedisHealth()
            .zipWith(checkServicesHealth())
            .map(tuple -> {
                boolean redisHealthy = tuple.getT1();
                Map<String, Object> servicesHealth = tuple.getT2();
                
                boolean overallHealthy = redisHealthy && 
                    servicesHealth.values().stream()
                        .allMatch(service -> "UP".equals(((Map<?, ?>) service).get("status")));
                
                health.put("status", overallHealthy ? "UP" : "DOWN");
                health.put("components", Map.of(
                    "redis", Map.of("status", redisHealthy ? "UP" : "DOWN"),
                    "services", servicesHealth
                ));
                
                HttpStatus httpStatus = overallHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                return ResponseEntity.status(httpStatus).body(health);
            })
            .onErrorResume(throwable -> {
                log.error("Error during health check", throwable);
                health.put("status", "DOWN");
                health.put("error", throwable.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health));
            });
    }

    /**
     * Gateway-specific information
     */
    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("app", Map.of(
            "name", "api-gateway",
            "version", "1.0.0",
            "description", "DoorDash API Gateway"
        ));
        info.put("build", Map.of(
            "time", LocalDateTime.now(),
            "java", Map.of(
                "version", System.getProperty("java.version"),
                "vendor", System.getProperty("java.vendor")
            )
        ));
        info.put("capabilities", List.of(
            "JWT Authentication",
            "Rate Limiting", 
            "API Versioning",
            "Circuit Breaker",
            "Request/Response Logging",
            "Global Error Handling",
            "Service Discovery",
            "Load Balancing"
        ));
        
        return Mono.just(ResponseEntity.ok(info));
    }

    /**
     * Circuit breaker status for all services
     */
    @GetMapping("/circuitbreakers")
    public Mono<ResponseEntity<Map<String, Object>>> circuitBreakers() {
        Map<String, Object> circuitBreakers = new HashMap<>();
        circuitBreakers.put("timestamp", LocalDateTime.now());
        
        // This would be populated by actual circuit breaker status
        // For now, we'll show discovered services
        List<String> services = discoveryClient.getServices();
        Map<String, Object> serviceStates = services.stream()
            .collect(Collectors.toMap(
                service -> service,
                service -> Map.of(
                    "state", "CLOSED", // Default state
                    "failureRate", "0%",
                    "callsInLastMinute", 0
                )
            ));
        
        circuitBreakers.put("services", serviceStates);
        return Mono.just(ResponseEntity.ok(circuitBreakers));
    }

    private Mono<Boolean> checkRedisHealth() {
        return redisTemplate.opsForValue()
            .get("health-check")
            .timeout(Duration.ofSeconds(2))
            .map(result -> true)
            .onErrorReturn(false);
    }

    private Mono<Map<String, Object>> checkServicesHealth() {
        return Mono.fromCallable(() -> {
            List<String> services = discoveryClient.getServices();
            Map<String, Object> servicesHealth = new HashMap<>();
            
            for (String serviceName : services) {
                if ("api-gateway".equals(serviceName)) {
                    continue; // Skip self
                }
                
                try {
                    List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                    String status = instances.isEmpty() ? "DOWN" : "UP";
                    
                    servicesHealth.put(serviceName, Map.of(
                        "status", status,
                        "instances", instances.size(),
                        "details", instances.stream()
                            .map(instance -> Map.of(
                                "host", instance.getHost(),
                                "port", instance.getPort(),
                                "uri", instance.getUri().toString()
                            ))
                            .collect(Collectors.toList())
                    ));
                } catch (Exception e) {
                    log.warn("Failed to check health for service: {}", serviceName, e);
                    servicesHealth.put(serviceName, Map.of(
                        "status", "UNKNOWN",
                        "error", e.getMessage()
                    ));
                }
            }
            
            return servicesHealth;
        });
    }
}
