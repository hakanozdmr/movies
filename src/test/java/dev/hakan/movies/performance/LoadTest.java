package dev.hakan.movies.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hakan.movies.data.dto.LoginRequest;
import dev.hakan.movies.data.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoadTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate = new TestRestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void concurrentUsersLoadTest() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        int numberOfUsers = 50; // 50 concurrent user
        int requestsPerUser = 10; // Her user 10 request yapacak
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> responseTimes = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfUsers; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerUser; j++) {
                        long requestStart = System.currentTimeMillis();
                        
                        // GET /movies request
                        ResponseEntity<String> response = restTemplate.getForEntity(
                                baseUrl + "/movies", String.class);
                        
                        long requestEnd = System.currentTimeMillis();
                        responseTimes.add(requestEnd - requestStart);

                        if (response.getStatusCode() == HttpStatus.OK) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }

                        // Request'ler arasında küçük delay
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Error in user " + userIndex + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Tüm request'lerin tamamlanmasını bekle
        boolean allCompleted = latch.await(60, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;

        executor.shutdown();

        // Sonuçları yazdır
        System.out.println("=== LOAD TEST RESULTS ===");
        System.out.println("Total Users: " + numberOfUsers);
        System.out.println("Requests per User: " + requestsPerUser);
        System.out.println("Total Requests: " + (numberOfUsers * requestsPerUser));
        System.out.println("Total Time: " + totalTime + " ms");
        System.out.println("Successful Requests: " + successCount.get());
        System.out.println("Failed Requests: " + errorCount.get());
        System.out.println("Success Rate: " + (successCount.get() * 100.0 / (numberOfUsers * requestsPerUser)) + "%");
        
        if (!responseTimes.isEmpty()) {
            double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            
            System.out.println("Average Response Time: " + avgResponseTime + " ms");
            System.out.println("Max Response Time: " + maxResponseTime + " ms");
            System.out.println("Min Response Time: " + minResponseTime + " ms");
        }

        // Test başarılı mı kontrol et
        assert allCompleted : "Not all requests completed within timeout";
        assert successCount.get() > (numberOfUsers * requestsPerUser * 0.9) : "Success rate too low: " + successCount.get();
    }

    @Test
    public void authEndpointsLoadTest() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        int numberOfUsers = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);
        AtomicInteger successCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfUsers; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    // Register request
                    RegisterRequest registerRequest = new RegisterRequest();
                    registerRequest.setName("Load Test User " + userIndex);
                    registerRequest.setEmail("loadtest" + userIndex + "@test.com");
                    registerRequest.setPassword("password123");

                    HttpEntity<String> registerEntity = new HttpEntity<>(
                            objectMapper.writeValueAsString(registerRequest), headers);

                    ResponseEntity<String> registerResponse = restTemplate.exchange(
                            baseUrl + "/auth/register",
                            HttpMethod.POST,
                            registerEntity,
                            String.class
                    );

                    if (registerResponse.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    System.err.println("Error in auth test user " + userIndex + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean allCompleted = latch.await(30, TimeUnit.SECONDS);
        long totalTime = System.currentTimeMillis() - startTime;

        executor.shutdown();

        System.out.println("=== AUTH LOAD TEST RESULTS ===");
        System.out.println("Total Users: " + numberOfUsers);
        System.out.println("Successful Registrations: " + successCount.get());
        System.out.println("Total Time: " + totalTime + " ms");

        assert allCompleted : "Auth load test did not complete in time";
    }

    @Test
    public void stressTestWithIncreasingLoad() throws InterruptedException {
        String baseUrl = "http://localhost:" + port;
        
        // Farklı yük seviyelerinde test
        int[] userCounts = {10, 25, 50, 100};
        
        for (int userCount : userCounts) {
            System.out.println("\n=== STRESS TEST WITH " + userCount + " CONCURRENT USERS ===");
            
            ExecutorService executor = Executors.newFixedThreadPool(userCount);
            CountDownLatch latch = new CountDownLatch(userCount);
            AtomicInteger successCount = new AtomicInteger(0);
            List<Long> responseTimes = new ArrayList<>();

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < userCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 5; j++) { // Her user 5 request
                            long requestStart = System.currentTimeMillis();
                            
                            ResponseEntity<String> response = restTemplate.getForEntity(
                                    baseUrl + "/movies", String.class);
                            
                            long requestEnd = System.currentTimeMillis();
                            responseTimes.add(requestEnd - requestStart);

                            if (response.getStatusCode() == HttpStatus.OK) {
                                successCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error in stress test: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            long totalTime = System.currentTimeMillis() - startTime;

            executor.shutdown();

            if (!responseTimes.isEmpty()) {
                double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                System.out.println("Users: " + userCount);
                System.out.println("Success: " + successCount.get() + "/" + (userCount * 5));
                System.out.println("Avg Response Time: " + avgResponseTime + " ms");
                System.out.println("Total Time: " + totalTime + " ms");
            }

            assert completed : "Stress test failed to complete with " + userCount + " users";
        }
    }
}

