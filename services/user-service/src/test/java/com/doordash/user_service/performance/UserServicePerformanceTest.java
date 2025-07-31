package com.doordash.user_service.performance;

import com.doordash.user_service.domain.dtos.user.CreateUserProfileRequest;
import com.doordash.user_service.testcontainers.BaseIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance tests for User Service.
 * 
 * Tests system behavior under load and stress conditions:
 * - Response time under various loads
 * - Throughput measurements
 * - Memory usage patterns
 * - Database connection pool behavior
 * - Cache performance
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("performance-test")
@DisplayName("User Service Performance Tests")
class UserServicePerformanceTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer test-jwt-token");
    }

    @Test
    @DisplayName("Should handle high concurrent user profile creations")
    void shouldHandleHighConcurrentUserProfileCreations() throws InterruptedException {
        int numberOfUsers = 100;
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();
        List<Long> responseTimes = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfUsers; i++) {
            final int userIndex = i;
            
            CompletableFuture<ResponseEntity<String>> future = CompletableFuture.supplyAsync(() -> {
                UUID userId = UUID.randomUUID();
                CreateUserProfileRequest request = CreateUserProfileRequest.builder()
                    .firstName("User" + userIndex)
                    .lastName("Test" + userIndex)
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .phoneNumber("+123456789" + (userIndex % 10))
                    .build();

                String createUrl = baseUrl + "/users/" + userId + "/profile";
                HttpEntity<CreateUserProfileRequest> entity = new HttpEntity<>(request, headers);
                
                long requestStart = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.postForEntity(createUrl, entity, String.class);
                long requestEnd = System.currentTimeMillis();
                
                synchronized (responseTimes) {
                    responseTimes.add(requestEnd - requestStart);
                }
                
                return response;
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Verify all requests succeeded
        int successCount = 0;
        for (CompletableFuture<ResponseEntity<String>> future : futures) {
            ResponseEntity<String> response = future.get();
            if (response.getStatusCode().is2xxSuccessful()) {
                successCount++;
            }
        }
        
        // Calculate performance metrics
        double successRate = (double) successCount / numberOfUsers * 100;
        double throughput = (double) numberOfUsers / (totalTime / 1000.0); // requests per second
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        System.out.println("Performance Test Results:");
        System.out.println("Total Users: " + numberOfUsers);
        System.out.println("Success Rate: " + successRate + "%");
        System.out.println("Total Time: " + totalTime + "ms");
        System.out.println("Throughput: " + throughput + " requests/second");
        System.out.println("Average Response Time: " + avgResponseTime + "ms");
        System.out.println("Max Response Time: " + maxResponseTime + "ms");
        
        // Performance assertions
        assertThat(successRate).isGreaterThan(95.0); // 95% success rate
        assertThat(avgResponseTime).isLessThan(1000); // Average response time < 1 second
        assertThat(maxResponseTime).isLessThan(5000); // Max response time < 5 seconds
        assertThat(throughput).isGreaterThan(10); // At least 10 requests per second
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should maintain performance under sustained load")
    void shouldMaintainPerformanceUnderSustainedLoad() throws InterruptedException {
        int durationMinutes = 2; // Short duration for CI/CD
        int requestsPerSecond = 20;
        int totalRequests = durationMinutes * 60 * requestsPerSecond;
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Long> responseTimes = new ArrayList<>();
        
        System.out.println("Starting sustained load test for " + durationMinutes + " minutes...");
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalRequests; i++) {
            final int requestIndex = i;
            
            executor.submit(() -> {
                try {
                    UUID userId = UUID.randomUUID();
                    String getUrl = baseUrl + "/users/" + userId + "/profile";
                    HttpEntity<Void> entity = new HttpEntity<>(headers);
                    
                    long requestStart = System.currentTimeMillis();
                    
                    try {
                        ResponseEntity<String> response = restTemplate.exchange(
                            getUrl, HttpMethod.GET, entity, String.class);
                        
                        long requestEnd = System.currentTimeMillis();
                        
                        synchronized (responseTimes) {
                            responseTimes.add(requestEnd - requestStart);
                        }
                        
                        if (requestIndex % 100 == 0) {
                            System.out.println("Completed " + requestIndex + " requests");
                        }
                    } catch (Exception e) {
                        System.err.println("Request failed: " + e.getMessage());
                    }
                    
                    // Rate limiting to maintain consistent load
                    if (requestIndex < totalRequests - 1) {
                        Thread.sleep(1000 / requestsPerSecond);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
        executor.awaitTermination(durationMinutes + 1, TimeUnit.MINUTES);
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Calculate performance metrics
        double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double p95ResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .sorted()
            .skip((long) (responseTimes.size() * 0.95))
            .findFirst()
            .orElse(0);
        
        System.out.println("Sustained Load Test Results:");
        System.out.println("Total Requests: " + responseTimes.size());
        System.out.println("Total Time: " + totalTime + "ms");
        System.out.println("Average Response Time: " + avgResponseTime + "ms");
        System.out.println("95th Percentile Response Time: " + p95ResponseTime + "ms");
        
        // Performance assertions for sustained load
        assertThat(avgResponseTime).isLessThan(500); // Average response time < 500ms
        assertThat(p95ResponseTime).isLessThan(1000); // 95th percentile < 1 second
        assertThat(responseTimes.size()).isGreaterThan(totalRequests * 0.9); // 90% completion rate
    }

    @Test
    @DisplayName("Should handle memory efficiently with large datasets")
    void shouldHandleMemoryEfficientlyWithLargeDatasets() {
        int numberOfProfiles = 1000;
        Runtime runtime = Runtime.getRuntime();
        
        // Measure initial memory
        runtime.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Create many user profiles
        for (int i = 0; i < numberOfProfiles; i++) {
            UUID userId = UUID.randomUUID();
            CreateUserProfileRequest request = CreateUserProfileRequest.builder()
                .firstName("BulkUser" + i)
                .lastName("Test" + i)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phoneNumber("+123456789" + (i % 10))
                .build();

            String createUrl = baseUrl + "/users/" + userId + "/profile";
            HttpEntity<CreateUserProfileRequest> entity = new HttpEntity<>(request, headers);
            
            try {
                restTemplate.postForEntity(createUrl, entity, String.class);
            } catch (Exception e) {
                // Continue with next request
            }
            
            if (i % 100 == 0) {
                System.out.println("Created " + i + " profiles");
            }
        }
        
        // Measure final memory
        runtime.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;
        
        System.out.println("Memory Usage Test Results:");
        System.out.println("Initial Memory: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final Memory: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory Used: " + (memoryUsed / 1024 / 1024) + " MB");
        System.out.println("Memory per Profile: " + (memoryUsed / numberOfProfiles) + " bytes");
        
        // Memory efficiency assertions
        long memoryPerProfile = memoryUsed / numberOfProfiles;
        assertThat(memoryPerProfile).isLessThan(10 * 1024); // Less than 10KB per profile
        assertThat(finalMemory).isLessThan(512 * 1024 * 1024); // Less than 512MB total
    }

    @Test
    @DisplayName("Should demonstrate cache performance improvement")
    void shouldDemonstrateCachePerformanceImprovement() {
        // Create a user profile first
        UUID userId = UUID.randomUUID();
        CreateUserProfileRequest request = CreateUserProfileRequest.builder()
            .firstName("CacheTest")
            .lastName("User")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .phoneNumber("+1234567890")
            .build();

        String createUrl = baseUrl + "/users/" + userId + "/profile";
        HttpEntity<CreateUserProfileRequest> createEntity = new HttpEntity<>(request, headers);
        restTemplate.postForEntity(createUrl, createEntity, String.class);

        String getUrl = baseUrl + "/users/" + userId + "/profile";
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);
        
        // Measure first request (database hit)
        List<Long> coldRequestTimes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            restTemplate.exchange(getUrl, HttpMethod.GET, getEntity, String.class);
            long endTime = System.currentTimeMillis();
            coldRequestTimes.add(endTime - startTime);
        }
        
        // Measure subsequent requests (cache hits)
        List<Long> warmRequestTimes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long startTime = System.currentTimeMillis();
            restTemplate.exchange(getUrl, HttpMethod.GET, getEntity, String.class);
            long endTime = System.currentTimeMillis();
            warmRequestTimes.add(endTime - startTime);
        }
        
        double avgColdTime = coldRequestTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double avgWarmTime = warmRequestTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        double improvement = ((avgColdTime - avgWarmTime) / avgColdTime) * 100;
        
        System.out.println("Cache Performance Test Results:");
        System.out.println("Average Cold Request Time: " + avgColdTime + "ms");
        System.out.println("Average Warm Request Time: " + avgWarmTime + "ms");
        System.out.println("Performance Improvement: " + improvement + "%");
        
        // Cache performance assertions
        assertThat(avgWarmTime).isLessThan(avgColdTime); // Cached requests should be faster
        assertThat(improvement).isGreaterThan(0); // Should show some improvement
    }

    @Test
    @DisplayName("Should handle database connection pool efficiently")
    void shouldHandleDatabaseConnectionPoolEfficiently() throws InterruptedException {
        int numberOfConcurrentRequests = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);
        
        List<CompletableFuture<Long>> futures = IntStream.range(0, numberOfConcurrentRequests)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                UUID userId = UUID.randomUUID();
                CreateUserProfileRequest request = CreateUserProfileRequest.builder()
                    .firstName("PoolTest" + i)
                    .lastName("User" + i)
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .phoneNumber("+123456789" + (i % 10))
                    .build();

                String createUrl = baseUrl + "/users/" + userId + "/profile";
                HttpEntity<CreateUserProfileRequest> entity = new HttpEntity<>(request, headers);
                
                long startTime = System.currentTimeMillis();
                
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(createUrl, entity, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return System.currentTimeMillis() - startTime;
                    }
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                }
                
                return -1L; // Indicate failure
            }, executor))
            .toList();
        
        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        List<Long> successfulRequestTimes = futures.stream()
            .map(CompletableFuture::join)
            .filter(time -> time > 0)
            .toList();
        
        double successRate = (double) successfulRequestTimes.size() / numberOfConcurrentRequests * 100;
        double avgResponseTime = successfulRequestTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        
        System.out.println("Database Connection Pool Test Results:");
        System.out.println("Concurrent Requests: " + numberOfConcurrentRequests);
        System.out.println("Successful Requests: " + successfulRequestTimes.size());
        System.out.println("Success Rate: " + successRate + "%");
        System.out.println("Average Response Time: " + avgResponseTime + "ms");
        
        // Connection pool efficiency assertions
        assertThat(successRate).isGreaterThan(95.0); // 95% success rate
        assertThat(avgResponseTime).isLessThan(2000); // Average response time < 2 seconds
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }
}
