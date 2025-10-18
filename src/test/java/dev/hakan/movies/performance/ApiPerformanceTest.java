package dev.hakan.movies.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ApiPerformanceTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testResponseTimeUnderLoad() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        int numberOfRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        long startTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(
                            baseUrl + "/movies", String.class);
                    
                    if (response.getStatusCode() == HttpStatus.OK) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;
        
        executor.shutdown();

        System.out.println("Response Time Test Results:");
        System.out.println("Total Requests: " + numberOfRequests);
        System.out.println("Successful Requests: " + successCount.get());
        System.out.println("Total Time: " + totalTime + " ms");
        System.out.println("Average Time per Request: " + (totalTime / (double) numberOfRequests) + " ms");
        
        assertTrue(completed, "Not all requests completed in time");
        assertTrue(successCount.get() > numberOfRequests * 0.9, "Success rate too low");
        assertTrue(totalTime < 30000, "Response time too high");
    }

    @Test
    public void testConcurrentUsersLimit() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        int maxConcurrentUsers = 50;
        
        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrentUsers);
        CountDownLatch latch = new CountDownLatch(maxConcurrentUsers);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < maxConcurrentUsers; i++) {
            executor.submit(() -> {
                try {
                    // Her user 3 request yapsın
                    for (int j = 0; j < 3; j++) {
                        ResponseEntity<String> response = restTemplate.getForEntity(
                                baseUrl + "/movies", String.class);
                        
                        if (response.getStatusCode() != HttpStatus.OK) {
                            errorCount.incrementAndGet();
                        }
                        
                        Thread.sleep(100); // Request'ler arasında 100ms bekle
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(45, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;
        
        executor.shutdown();

        System.out.println("Concurrent Users Test Results:");
        System.out.println("Concurrent Users: " + maxConcurrentUsers);
        System.out.println("Total Requests: " + (maxConcurrentUsers * 3));
        System.out.println("Error Count: " + errorCount.get());
        System.out.println("Total Time: " + totalTime + " ms");
        
        assertTrue(completed, "Concurrent test did not complete");
        assertTrue(errorCount.get() < maxConcurrentUsers * 0.5, "Too many errors under concurrent load");
    }

    @Test
    public void testMemoryUsageUnderLoad() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        int numberOfRequests = 200;
        
        // Memory usage before test
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        ExecutorService executor = Executors.newFixedThreadPool(30);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        
        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.getForEntity(
                            baseUrl + "/movies", String.class);
                    
                    // Response'u kullan ki garbage collector tarafından temizlenmesin
                    if (!response.hasBody() || response.getBody() == null) {
                        throw new RuntimeException("Empty response body");
                    }
                    
                } catch (Exception e) {
                    System.err.println("Memory test request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Memory usage after test
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.println("Memory Usage Test Results:");
        System.out.println("Memory Before: " + (memoryBefore / 1024 / 1024) + " MB");
        System.out.println("Memory After: " + (memoryAfter / 1024 / 1024) + " MB");
        System.out.println("Memory Used: " + (memoryUsed / 1024 / 1024) + " MB");
        
        assertTrue(completed, "Memory test did not complete");
        assertTrue(memoryUsed < 100 * 1024 * 1024, "Memory usage too high"); // 100MB limit
    }

    @Test
    public void testResourceCleanupAfterLoad() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        
        // İlk yük testi
        executeLoadTest(baseUrl, 100, 20);
        
        // Kısa bekleme
        Thread.sleep(2000);
        
        // Resource cleanup kontrolü için tek request
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/movies", String.class);
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Resource Cleanup Test:");
        System.out.println("Response time after load test: " + responseTime + " ms");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(responseTime < 5000, "Response time too high after load test - possible resource leak");
    }

    private void executeLoadTest(String baseUrl, int requests, int threadPoolSize) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(requests);

        for (int i = 0; i < requests; i++) {
            executor.submit(() -> {
                try {
                    restTemplate.getForEntity(baseUrl + "/movies", String.class);
                } catch (Exception e) {
                    System.err.println("Load test request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
    }
}

