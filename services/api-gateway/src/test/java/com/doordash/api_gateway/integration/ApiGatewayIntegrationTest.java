package com.doordash.api_gateway.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Integration tests for API Gateway functionality.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.consul.discovery.enabled=false",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "gateway.rate-limiting.enabled=false",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/auth/realms/doordash"
})
class ApiGatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testHealthEndpoint() {
        webTestClient.get()
            .uri("/actuator/health")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.status").isEqualTo("UP")
            .jsonPath("$.service").isEqualTo("api-gateway");
    }

    @Test
    void testDetailedHealthEndpoint() {
        webTestClient.get()
            .uri("/actuator/health/detailed")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.service").isEqualTo("api-gateway")
            .jsonPath("$.components").exists();
    }

    @Test
    void testInfoEndpoint() {
        webTestClient.get()
            .uri("/actuator/info")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.app.name").isEqualTo("api-gateway")
            .jsonPath("$.capabilities").isArray();
    }

    @Test
    void testCircuitBreakersEndpoint() {
        webTestClient.get()
            .uri("/actuator/circuitbreakers")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.services").exists();
    }

    @Test
    void testApiDocsEndpoint() {
        webTestClient.get()
            .uri("/api-docs")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.openapi").isEqualTo("3.0.1")
            .jsonPath("$.info.title").isEqualTo("DoorDash API Gateway");
    }

    @Test
    void testApiDocsServicesEndpoint() {
        webTestClient.get()
            .uri("/api-docs/services")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.services").isArray();
    }

    @Test
    void testUnauthorizedAccessToProtectedRoute() {
        webTestClient.get()
            .uri("/api/v1/users/profile")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    void testPublicRouteAccess() {
        webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"username\":\"test\",\"password\":\"test\"}")
            .exchange()
            .expectStatus().is5xxServerError(); // Service not available in test
    }

    @Test
    void testCorsHeaders() {
        webTestClient.options()
            .uri("/api/v1/users/profile")
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "GET")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)
            .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
    }

    @Test
    void testApiVersioningWithHeader() {
        webTestClient.get()
            .uri("/api/v1/restaurants/search")
            .header("X-API-Version", "v2")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError(); // Service not available in test
    }

    @Test
    void testApiVersioningWithQueryParam() {
        webTestClient.get()
            .uri("/api/v1/restaurants/search?version=v2")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError(); // Service not available in test
    }

    @Test
    void testGlobalErrorHandling() {
        webTestClient.get()
            .uri("/api/v1/nonexistent/endpoint")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void testRequestWithJwtToken() {
        String mockJwtToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        webTestClient.get()
            .uri("/api/v1/users/profile")
            .header(HttpHeaders.AUTHORIZATION, mockJwtToken)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError(); // Invalid token or service not available
    }
}
