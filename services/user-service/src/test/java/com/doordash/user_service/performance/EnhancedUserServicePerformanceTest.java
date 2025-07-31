package com.doordash.user_service.performance;

import com.doordash.user_service.config.EnhancedTestConfig;
import com.doordash.user_service.domain.dtos.user.CreateUserProfileRequest;
import com.doordash.user_service.domain.entities.User;
import com.doordash.user_service.repositories.UserRepository;
import com.doordash.user_service.testcontainers.EnhancedBaseIntegrationTest;
import com.doordash.user_service.utils.EnhancedTestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive performance tests for the User Service.
 * 
 * This test class covers:
 * - Load testing with concurrent requests
 * - Throughput measurement
 * - Response time analysis
 * - Memory usage monitoring
 * - Database connection pool testing
 * - Cache performance validation
 * - Scalability limits testing
 * - Resource utilization analysis
 * 
 * @author DoorDash Backend Team
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "performance"})
@Import(EnhancedTestConfig.class)
@Tag("performance")
@DisplayName("User Service Performance Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnhancedUserServicePerformanceTest extends EnhancedBaseIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final int WARMUP_REQUESTS = 50;
    private static final int LOAD_TEST_REQUESTS = 1000;
    private static final int CONCURRENT_USERS = 50;
    private static final Duration MAX_RESPONSE_TIME = Duration.ofMillis(500);
    private static final double MIN_THROUGHPUT_RPS = 100.0;

    @BeforeEach
    void setUp() {
        // Clear cache and prepare clean state
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        userRepository.deleteAll();
        
        // Warmup the JVM and database connections
        performWarmup();
    }

    @Nested
    @DisplayName("Load Testing")
    @Order(1)
    class LoadTesting {

        @Test
        @DisplayName("Should handle concurrent profile creation requests")
        void shouldHandleConcurrentProfileCreation() throws Exception {
            // Arrange
            int numberOfRequests = LOAD_TEST_REQUESTS;
            int concurrentUsers = CONCURRENT_USERS;
            ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
            CountDownLatch latch = new CountDownLatch(numberOfRequests);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            AtomicLong totalResponseTime = new AtomicLong(0);
            
            // Act
            Instant startTime = Instant.now();
            
            for (int i = 0; i < numberOfRequests; i++) {
                final int requestId = i;
                executor.submit(() -> {
                    try {
                        Instant requestStart = Instant.now();
                        
                        CreateUserProfileRequest request = EnhancedTestDataFactory.createRealisticCreateRequest();
                        HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders();
                        HttpEntity<CreateUserProfileRequest> entity = new HttpEntity<>(request, headers);
                        
                        ResponseEntity<String> response = testRestTemplate.postForEntity(
                            getUsersApiUrl() + "/profile", entity, String.class);
                        
                        Instant requestEnd = Instant.now();
                        long responseTime = Duration.between(requestStart, requestEnd).toMillis();
                        totalResponseTime.addAndGet(responseTime);
                        
                        if (response.getStatusCode().is2xxSuccessful()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                            System.err.println("Request " + requestId + " failed with status: " + response.getStatusCode());
                        }
                        
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        System.err.println("Request " + requestId + " failed with exception: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // Wait for all requests to complete with timeout
            boolean completed = latch.await(5, TimeUnit.MINUTES);
            executor.shutdown();
            
            Instant endTime = Instant.now();
            Duration totalDuration = Duration.between(startTime, endTime);
            
            // Assert
            assertThat(completed).isTrue();
            
            double successRate = (double) successCount.get() / numberOfRequests * 100;
            double throughputRps = (double) successCount.get() / totalDuration.toSeconds();
            double averageResponseTime = (double) totalResponseTime.get() / successCount.get();
            
            System.out.println("\n=== Load Test Results ===");
            System.out.println("Total Requests: " + numberOfRequests);
            System.out.println("Successful Requests: " + successCount.get());
            System.out.println("Failed Requests: " + errorCount.get());
            System.out.println("Success Rate: " + String.format("%.2f%%", successRate));
            System.out.println("Total Duration: " + totalDuration.toSeconds() + " seconds");
            System.out.println("Throughput: " + String.format("%.2f", throughputRps) + " RPS");
            System.out.println("Average Response Time: " + String.format("%.2f", averageResponseTime) + " ms");
            
            // Performance assertions
            assertThat(successRate).isGreaterThan(95.0); // 95% success rate minimum
            assertThat(throughputRps).isGreaterThan(MIN_THROUGHPUT_RPS);
            assertThat(averageResponseTime).isLessThan(MAX_RESPONSE_TIME.toMillis());
        }

        @Test
        @DisplayName("Should handle concurrent profile read requests")
        void shouldHandleConcurrentProfileReads() throws Exception {
            // Setup test data
            List<User> testUsers = EnhancedTestDataFactory.createRealisticUsers(100);
            userRepository.saveAll(testUsers);
            
            // Arrange
            int numberOfRequests = LOAD_TEST_REQUESTS * 2; // More reads than writes
            int concurrentUsers = CONCURRENT_USERS * 2;
            ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
            CountDownLatch latch = new CountDownLatch(numberOfRequests);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicLong totalResponseTime = new AtomicLong(0);
            
            // Act
            Instant startTime = Instant.now();
            
            for (int i = 0; i < numberOfRequests; i++) {
                executor.submit(() -> {
                    try {
                        Instant requestStart = Instant.now();
                        
                        // Random user selection
                        User randomUser = testUsers.get(ThreadLocalRandom.current().nextInt(testUsers.size()));
                        HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders(randomUser.getId().toString());
                        HttpEntity<Void> entity = new HttpEntity<>(headers);
                        
                        ResponseEntity<String> response = testRestTemplate.exchange(
                            getUsersApiUrl() + "/profile", HttpMethod.GET, entity, String.class);
                        
                        Instant requestEnd = Instant.now();
                        long responseTime = Duration.between(requestStart, requestEnd).toMillis();
                        totalResponseTime.addAndGet(responseTime);
                        
                        if (response.getStatusCode().is2xxSuccessful()) {
                            successCount.incrementAndGet();
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Read request failed: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            boolean completed = latch.await(3, TimeUnit.MINUTES);
            executor.shutdown();
            
            Instant endTime = Instant.now();
            Duration totalDuration = Duration.between(startTime, endTime);
            
            // Assert
            assertThat(completed).isTrue();
            
            double throughputRps = (double) successCount.get() / totalDuration.toSeconds();
            double averageResponseTime = (double) totalResponseTime.get() / successCount.get();
            
            System.out.println("\n=== Read Load Test Results ===");
            System.out.println("Successful Read Requests: " + successCount.get());
            System.out.println("Read Throughput: " + String.format("%.2f", throughputRps) + " RPS");
            System.out.println("Average Read Response Time: " + String.format("%.2f", averageResponseTime) + " ms");
            
            // Read operations should be faster
            assertThat(throughputRps).isGreaterThan(MIN_THROUGHPUT_RPS * 2);
            assertThat(averageResponseTime).isLessThan(MAX_RESPONSE_TIME.toMillis() / 2);
        }
    }

    @Nested
    @DisplayName("Cache Performance Testing")
    @Order(2)
    class CachePerformanceTests {

        @Test
        @DisplayName("Should demonstrate cache performance improvement")
        void shouldDemonstrateCachePerformance() throws Exception {
            // Setup test user
            User testUser = EnhancedTestDataFactory.createRealisticUser();
            userRepository.save(testUser);
            
            HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders(testUser.getId().toString());
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            // First request (cache miss)
            Instant start1 = Instant.now();
            ResponseEntity<String> response1 = testRestTemplate.exchange(
                getUsersApiUrl() + "/profile", HttpMethod.GET, entity, String.class);
            Duration firstRequestTime = Duration.between(start1, Instant.now());
            
            assertThat(response1.getStatusCode().is2xxSuccessful()).isTrue();
            
            // Second request (cache hit)
            Instant start2 = Instant.now();
            ResponseEntity<String> response2 = testRestTemplate.exchange(
                getUsersApiUrl() + "/profile", HttpMethod.GET, entity, String.class);
            Duration secondRequestTime = Duration.between(start2, Instant.now());
            
            assertThat(response2.getStatusCode().is2xxSuccessful()).isTrue();
            
            // Cache should make second request significantly faster
            double improvementRatio = (double) firstRequestTime.toMillis() / secondRequestTime.toMillis();
            
            System.out.println("\n=== Cache Performance Results ===");
            System.out.println("First Request (Cache Miss): " + firstRequestTime.toMillis() + " ms");
            System.out.println("Second Request (Cache Hit): " + secondRequestTime.toMillis() + " ms");
            System.out.println("Performance Improvement: " + String.format("%.2fx", improvementRatio));
            
            assertThat(improvementRatio).isGreaterThan(2.0); // At least 2x improvement
        }

        @Test
        @DisplayName("Should handle cache invalidation efficiently")
        void shouldHandleCacheInvalidationEfficiently() throws Exception {
            // Setup
            User testUser = EnhancedTestDataFactory.createRealisticUser();
            userRepository.save(testUser);
            
            HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders(testUser.getId().toString());
            
            // Prime the cache
            testRestTemplate.exchange(getUsersApiUrl() + "/profile", HttpMethod.GET, 
                new HttpEntity<>(headers), String.class);
            
            // Update the profile (should invalidate cache)
            var updateRequest = EnhancedTestDataFactory.createRealisticUpdateRequest();
            HttpEntity<Object> updateEntity = new HttpEntity<>(updateRequest, headers);
            
            Instant updateStart = Instant.now();
            ResponseEntity<String> updateResponse = testRestTemplate.exchange(
                getUsersApiUrl() + "/profile", HttpMethod.PUT, updateEntity, String.class);
            Duration updateTime = Duration.between(updateStart, Instant.now());
            
            assertThat(updateResponse.getStatusCode().is2xxSuccessful()).isTrue();
            
            // Subsequent read should reflect the update
            ResponseEntity<String> readResponse = testRestTemplate.exchange(
                getUsersApiUrl() + "/profile", HttpMethod.GET, 
                new HttpEntity<>(headers), String.class);
            
            assertThat(readResponse.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(updateTime.toMillis()).isLessThan(1000); // Update should be fast
            
            System.out.println("Cache invalidation time: " + updateTime.toMillis() + " ms");
        }
    }

    @Nested
    @DisplayName("Scalability Testing")
    @Order(3)
    class ScalabilityTests {

        @Test
        @DisplayName("Should maintain performance with large datasets")
        void shouldMaintainPerformanceWithLargeDatasets() throws Exception {
            // Create large dataset
            int datasetSize = 10000;
            List<User> largeDataset = EnhancedTestDataFactory.PerformanceTestDataGenerator
                .createConcurrentTestUsers(datasetSize);
            
            Instant insertStart = Instant.now();
            userRepository.saveAll(largeDataset);
            Duration insertTime = Duration.between(insertStart, Instant.now());
            
            System.out.println("Large dataset creation time: " + insertTime.toSeconds() + " seconds");
            
            // Test search performance with large dataset
            HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders();
            
            Instant searchStart = Instant.now();
            ResponseEntity<String> searchResponse = testRestTemplate.exchange(
                getUsersApiUrl() + "/search?query=concurrent&page=0&size=20", 
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
            Duration searchTime = Duration.between(searchStart, Instant.now());
            
            assertThat(searchResponse.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(searchTime.toMillis()).isLessThan(1000); // Search should remain fast
            
            System.out.println("Search time with large dataset: " + searchTime.toMillis() + " ms");
        }

        @Test
        @DisplayName("Should handle burst traffic patterns")
        void shouldHandleBurstTrafficPatterns() throws Exception {
            ExecutorService executor = Executors.newFixedThreadPool(100);
            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch burstLatch = new CountDownLatch(500);
            
            // Simulate burst traffic
            Instant burstStart = Instant.now();
            
            for (int i = 0; i < 500; i++) {
                executor.submit(() -> {
                    try {
                        CreateUserProfileRequest request = EnhancedTestDataFactory.createRealisticCreateRequest();
                        HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders();
                        HttpEntity<CreateUserProfileRequest> entity = new HttpEntity<>(request, headers);
                        
                        ResponseEntity<String> response = testRestTemplate.postForEntity(
                            getUsersApiUrl() + "/profile", entity, String.class);
                        
                        if (response.getStatusCode().is2xxSuccessful()) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Expected during burst - some requests may fail
                    } finally {
                        burstLatch.countDown();
                    }
                });
            }
            
            boolean completed = burstLatch.await(2, TimeUnit.MINUTES);
            executor.shutdown();
            
            Duration burstDuration = Duration.between(burstStart, Instant.now());
            double burstThroughput = (double) successCount.get() / burstDuration.toSeconds();
            
            System.out.println("\n=== Burst Traffic Results ===");
            System.out.println("Burst Requests Successful: " + successCount.get() + "/500");
            System.out.println("Burst Duration: " + burstDuration.toSeconds() + " seconds");
            System.out.println("Burst Throughput: " + String.format("%.2f", burstThroughput) + " RPS");
            
            assertThat(completed).isTrue();
            assertThat(successCount.get()).isGreaterThan(400); // At least 80% success rate during burst
        }
    }

    @Nested
    @DisplayName("Memory and Resource Usage")
    @Order(4)
    class ResourceUsageTests {

        @Test
        @DisplayName("Should not have memory leaks during sustained load")
        void shouldNotHaveMemoryLeaks() throws Exception {
            Runtime runtime = Runtime.getRuntime();
            
            // Force garbage collection and get baseline
            System.gc();
            Thread.sleep(1000);
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();
            
            // Sustained load test
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(1000);
            
            for (int i = 0; i < 1000; i++) {
                executor.submit(() -> {
                    try {
                        CreateUserProfileRequest request = EnhancedTestDataFactory.createRealisticCreateRequest();
                        HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders();
                        HttpEntity<CreateUserProfileRequest> entity = new HttpEntity<>(request, headers);
                        
                        testRestTemplate.postForEntity(getUsersApiUrl() + "/profile", entity, String.class);
                    } catch (Exception e) {
                        // Ignore errors for memory test
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(3, TimeUnit.MINUTES);
            executor.shutdown();
            
            // Force garbage collection and measure memory
            System.gc();
            Thread.sleep(1000);
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            
            long memoryIncrease = finalMemory - initialMemory;
            double memoryIncreasePercent = (double) memoryIncrease / initialMemory * 100;
            
            System.out.println("\n=== Memory Usage Results ===");
            System.out.println("Initial Memory: " + formatBytes(initialMemory));
            System.out.println("Final Memory: " + formatBytes(finalMemory));
            System.out.println("Memory Increase: " + formatBytes(memoryIncrease) + 
                " (" + String.format("%.2f%%", memoryIncreasePercent) + ")");
            
            // Memory increase should be reasonable (less than 50% increase)
            assertThat(memoryIncreasePercent).isLessThan(50.0);
        }
    }

    // Helper methods

    private void performWarmup() {
        System.out.println("Performing JVM warmup...");
        
        for (int i = 0; i < WARMUP_REQUESTS; i++) {
            try {
                CreateUserProfileRequest request = EnhancedTestDataFactory.createRealisticCreateRequest();
                HttpHeaders headers = EnhancedTestDataFactory.createAuthenticatedHeaders();
                HttpEntity<CreateUserProfileRequest> entity = new HttpEntity<>(request, headers);
                
                testRestTemplate.postForEntity(getUsersApiUrl() + "/profile", entity, String.class);
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }
        
        // Clear warmup data
        userRepository.deleteAll();
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        System.out.println("Warmup completed.");
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
