package dev.hakan.movies.performance;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.model.Movie;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@ComponentScan(basePackages = "dev.hakan.movies")
public class MovieServicePerformanceTest {

    private ConfigurableApplicationContext context;
    private MovieService movieService;

    @Setup(Level.Trial)
    public void initializeBenchmark() {
        // Spring context başlat
        context = SpringApplication.run(dev.hakan.movies.MoviesApplication.class);
        movieService = context.getBean(MovieService.class);
    }

    @TearDown(Level.Trial)
    public void tearDownBenchmark() {
        context.close();
    }

    @Benchmark
    public void benchmarkFindAllMovies() {
        List<Movie> movies = movieService.findAllMovies();
        // Performans ölçümü için result'ı kullan
        if (movies == null) {
            throw new RuntimeException("Movies list is null");
        }
    }

    @Benchmark
    public void benchmarkGetAllMoviesForUpdate() {
        List<Movie> movies = movieService.getAllMoviesForUpdate();
        if (movies == null) {
            throw new RuntimeException("Movies list is null");
        }
    }

    @Benchmark
    public void benchmarkUpdateMoviesWithSampleData() {
        List<Movie> updatedMovies = movieService.updateMoviesWithSampleData();
        if (updatedMovies == null) {
            throw new RuntimeException("Updated movies list is null");
        }
    }

    @Benchmark
    public void benchmarkUpdateMovieByImdbId() {
        Movie updatedMovie = movieService.updateMovieByImdbId("tt1234567", "Updated description", "8.5");
        // Null olabilir, bu normal
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MovieServicePerformanceTest.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}

