package dev.hakan.movies.performance;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.model.Movie;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class DatabasePerformanceTest {

    private ConfigurableApplicationContext context;
    private MovieService movieService;

    @Setup(Level.Trial)
    public void initializeBenchmark() {
        context = SpringApplication.run(dev.hakan.movies.MoviesApplication.class);
        movieService = context.getBean(MovieService.class);
    }

    @TearDown(Level.Trial)
    public void tearDownBenchmark() {
        context.close();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void benchmarkFindAllMoviesThroughput() {
        List<Movie> movies = movieService.findAllMovies();
        if (movies == null) {
            throw new RuntimeException("Movies list is null");
        }
    }

    @Benchmark
    public void benchmarkComplexSearchQuery() {
        SearchAndSortDto searchRequest = new SearchAndSortDto();
        Movie movieFilter = new Movie();
        movieFilter.setTitle("spider"); // Partial match search
        searchRequest.setMovie(movieFilter);
        
        List<?> results = movieService.searchAndSortAllParamsWithLike(searchRequest);
        if (results == null) {
            throw new RuntimeException("Search results are null");
        }
    }

    @Benchmark
    public void benchmarkExactSearchQuery() {
        SearchAndSortDto searchRequest = new SearchAndSortDto();
        Movie movieFilter = new Movie();
        movieFilter.setImdbId("tt1234567");
        searchRequest.setMovie(movieFilter);
        
        List<?> results = movieService.searchAllParamsWithIs(searchRequest);
        if (results == null) {
            throw new RuntimeException("Search results are null");
        }
    }

    @Benchmark
    public void benchmarkUpdatePerformance() {
        // Test update performance
        Movie updatedMovie = movieService.updateMovieByImdbId(
            "tt1234567", 
            "Performance test update " + System.currentTimeMillis(),
            "8.5"
        );
    }

    @Benchmark
    public void benchmarkBulkUpdatePerformance() {
        // Test bulk update performance
        List<Movie> updatedMovies = movieService.updateMoviesWithSampleData();
        if (updatedMovies == null) {
            throw new RuntimeException("Bulk update failed");
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DatabasePerformanceTest.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}

