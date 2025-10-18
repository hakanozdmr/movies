package dev.hakan.movies.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hakan.movies.data.dto.AuthResponse;
import dev.hakan.movies.data.dto.LoginRequest;
import dev.hakan.movies.data.dto.RegisterRequest;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
public class ApiEndpointPerformanceTest {

    private ConfigurableApplicationContext context;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private String baseUrl;
    private HttpHeaders headers;

    @Setup(Level.Trial)
    public void initializeBenchmark() {
        // Spring context başlat
        context = SpringApplication.run(dev.hakan.movies.MoviesApplication.class);
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        baseUrl = "http://localhost:8080"; // Default port
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @TearDown(Level.Trial)
    public void tearDownBenchmark() {
        context.close();
    }

    @Benchmark
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public void benchmarkGetAllMovies() {
        try {
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/movies",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Unexpected status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // Performans testi için exception'ları logla ama durdurma
            System.err.println("Error in benchmarkGetAllMovies: " + e.getMessage());
        }
    }

    @Benchmark
    @Measurement(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
    public void benchmarkRegisterUser() {
        try {
            RegisterRequest request = new RegisterRequest();
            request.setName("Performance Test User " + new Random().nextInt(10000));
            request.setEmail("perf" + new Random().nextInt(10000) + "@test.com");
            request.setPassword("password123");

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(request),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/auth/register",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK && 
                response.getStatusCode() != HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Unexpected status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error in benchmarkRegisterUser: " + e.getMessage());
        }
    }

    @Benchmark
    @Measurement(iterations = 6, time = 1, timeUnit = TimeUnit.SECONDS)
    public void benchmarkLoginUser() {
        try {
            LoginRequest request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(request),
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/auth/login",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            // 401 Unauthorized beklenen durum
            if (response.getStatusCode() != HttpStatus.OK && 
                response.getStatusCode() != HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Unexpected status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error in benchmarkLoginUser: " + e.getMessage());
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    public void benchmarkSearchAndSort() {
        try {
            String searchRequest = """
                    {
                      "movie": {
                        "title": "test"
                      }
                    }
                    """;

            HttpEntity<String> entity = new HttpEntity<>(searchRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/movies/searchAndSort",
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Unexpected status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error in benchmarkSearchAndSort: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ApiEndpointPerformanceTest.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}

