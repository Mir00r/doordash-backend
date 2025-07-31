package com.doordash.api_gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpenAPI documentation aggregation controller.
 * Provides centralized access to API documentation from all microservices.
 */
@RestController
@RequestMapping("/api-docs")
@Slf4j
@RequiredArgsConstructor
public class OpenApiController {

    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    @Value("${gateway.api-docs.enabled:true}")
    private boolean apiDocsEnabled;

    @Value("${gateway.api-docs.services}")
    private List<String> documentedServices;

    /**
     * Get aggregated OpenAPI documentation for all services
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> getAggregatedDocs() {
        if (!apiDocsEnabled) {
            return Mono.just(Map.of("error", "API documentation is disabled"));
        }

        log.info("Fetching aggregated OpenAPI documentation");

        Map<String, Object> aggregatedDocs = new HashMap<>();
        aggregatedDocs.put("openapi", "3.0.1");
        aggregatedDocs.put("info", createGatewayInfo());
        aggregatedDocs.put("servers", List.of(Map.of("url", "/", "description", "API Gateway")));
        
        // Get all documented services
        List<String> services = getDocumentedServices();
        Map<String, Object> paths = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        return Mono.fromCallable(() -> {
            for (String service : services) {
                try {
                    Map<String, Object> serviceDocs = fetchServiceDocs(service);
                    if (serviceDocs != null) {
                        mergePaths(paths, serviceDocs, service);
                        mergeComponents(components, serviceDocs);
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch documentation for service: {}", service, e);
                }
            }
            
            aggregatedDocs.put("paths", paths);
            aggregatedDocs.put("components", components);
            return aggregatedDocs;
        });
    }

    /**
     * Get OpenAPI documentation for a specific service
     */
    @GetMapping(value = "/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> getServiceDocs(@PathVariable String service) {
        if (!apiDocsEnabled) {
            return Mono.just(Map.of("error", "API documentation is disabled"));
        }

        log.info("Fetching OpenAPI documentation for service: {}", service);

        return Mono.fromCallable(() -> {
            try {
                Map<String, Object> serviceDocs = fetchServiceDocs(service);
                if (serviceDocs != null) {
                    // Add gateway context
                    serviceDocs.put("_gateway", Map.of(
                        "service", service,
                        "version", "v1",
                        "timestamp", System.currentTimeMillis()
                    ));
                    return serviceDocs;
                }
                return Map.of("error", "Service documentation not found");
            } catch (Exception e) {
                log.error("Failed to fetch documentation for service: {}", service, e);
                return Map.of("error", "Failed to fetch service documentation");
            }
        });
    }

    /**
     * Get list of services with available documentation
     */
    @GetMapping("/services")
    public Mono<Map<String, Object>> getAvailableServices() {
        List<String> services = getDocumentedServices();
        
        Map<String, Object> result = new HashMap<>();
        result.put("services", services.stream()
            .map(service -> Map.of(
                "name", service,
                "docsUrl", "/api-docs/" + service,
                "status", checkServiceStatus(service)
            ))
            .collect(Collectors.toList()));
        
        return Mono.just(result);
    }

    private Map<String, Object> createGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("title", "DoorDash API Gateway");
        info.put("description", "Aggregated API documentation for all DoorDash microservices");
        info.put("version", "1.0.0");
        info.put("contact", Map.of(
            "name", "DoorDash API Team",
            "email", "api@doordash.com"
        ));
        return info;
    }

    private List<String> getDocumentedServices() {
        if (documentedServices != null && !documentedServices.isEmpty()) {
            return documentedServices;
        }
        
        // Discover services automatically
        return discoveryClient.getServices().stream()
            .filter(service -> !service.equals("api-gateway"))
            .collect(Collectors.toList());
    }

    private Map<String, Object> fetchServiceDocs(String serviceName) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (instances.isEmpty()) {
                log.warn("No instances found for service: {}", serviceName);
                return null;
            }

            ServiceInstance instance = instances.get(0);
            String docsUrl = String.format("http://%s:%d/v3/api-docs", 
                instance.getHost(), instance.getPort());

            WebClient webClient = webClientBuilder.build();
            
            @SuppressWarnings("unchecked")
            Map<String, Object> docs = webClient.get()
                .uri(docsUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return docs;
        } catch (Exception e) {
            log.error("Error fetching docs for service: {}", serviceName, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void mergePaths(Map<String, Object> targetPaths, Map<String, Object> serviceDocs, String serviceName) {
        Map<String, Object> paths = (Map<String, Object>) serviceDocs.get("paths");
        if (paths != null) {
            paths.forEach((path, pathItem) -> {
                // Prefix path with service context if needed
                String prefixedPath = path.startsWith("/api/") ? path : "/api/v1/" + serviceName + path;
                targetPaths.put(prefixedPath, pathItem);
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeComponents(Map<String, Object> targetComponents, Map<String, Object> serviceDocs) {
        Map<String, Object> components = (Map<String, Object>) serviceDocs.get("components");
        if (components != null) {
            components.forEach((key, value) -> {
                if (targetComponents.containsKey(key)) {
                    if (value instanceof Map && targetComponents.get(key) instanceof Map) {
                        ((Map<String, Object>) targetComponents.get(key)).putAll((Map<String, Object>) value);
                    }
                } else {
                    targetComponents.put(key, value);
                }
            });
        }
    }

    private String checkServiceStatus(String serviceName) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            return instances.isEmpty() ? "DOWN" : "UP";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
