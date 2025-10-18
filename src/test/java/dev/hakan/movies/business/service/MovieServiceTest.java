package dev.hakan.movies.business.service;

import dev.hakan.movies.data.dto.PageableDto;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.enums.SortingDirection;
import dev.hakan.movies.data.model.Movie;
import dev.hakan.movies.data.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie testMovie;
    private List<Movie> testMovies;
    private SearchAndSortDto searchAndSortDto;

    @BeforeEach
    void setUp() {
        testMovie = new Movie();
        testMovie.setImdbId("tt1234567");
        testMovie.setTitle("Test Movie");
        testMovie.setReleaseDate("2023-01-01");
        testMovie.setImdbRating("8.5");
        testMovie.setDescription("A test movie");
        testMovie.setDirector("Test Director");
        testMovie.setCast("Test Cast");
        testMovie.setDuration(120);
        testMovie.setAgeRating("PG-13");

        testMovies = Arrays.asList(testMovie);

        searchAndSortDto = new SearchAndSortDto();
        PageableDto pageableDto = new PageableDto();
        pageableDto.setPageNumber(0);
        pageableDto.setPageSize(10);
        searchAndSortDto.setPageableDto(pageableDto);
    }

    @Test
    void findAllMovies_ShouldReturnAllMovies() {
        // Given
        when(movieRepository.findAll()).thenReturn(testMovies);

        // When
        List<Movie> result = movieService.findAllMovies();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getTitle());
        verify(movieRepository, times(1)).findAll();
    }

    @Test
    void updateMovieByImdbId_WithValidData_ShouldReturnUpdatedMovie() {
        // Given
        String imdbId = "tt1234567";
        String description = "Updated description";
        String imdbRating = "9.0";

        when(movieRepository.findByImdbId(imdbId)).thenReturn(testMovie);
        when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);

        // When
        Movie result = movieService.updateMovieByImdbId(imdbId, description, imdbRating);

        // Then
        assertNotNull(result);
        assertEquals(description, result.getDescription());
        assertEquals(imdbRating, result.getImdbRating());
        verify(movieRepository, times(1)).findByImdbId(imdbId);
        verify(movieRepository, times(1)).save(testMovie);
    }

    @Test
    void updateMovieByImdbId_WithNonExistentMovie_ShouldReturnNull() {
        // Given
        String imdbId = "nonexistent";
        when(movieRepository.findByImdbId(imdbId)).thenReturn(null);

        // When
        Movie result = movieService.updateMovieByImdbId(imdbId, "description", "8.0");

        // Then
        assertNull(result);
        verify(movieRepository, times(1)).findByImdbId(imdbId);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void getAllMoviesForUpdate_ShouldReturnAllMovies() {
        // Given
        when(movieRepository.findAll()).thenReturn(testMovies);

        // When
        List<Movie> result = movieService.getAllMoviesForUpdate();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movieRepository, times(1)).findAll();
    }

    @Test
    void updateMoviesWithSampleData_ShouldUpdateMoviesWithMissingData() {
        // Given
        Movie movieWithMissingData = new Movie();
        movieWithMissingData.setImdbId("tt1234567");
        movieWithMissingData.setTitle("Action Movie");
        movieWithMissingData.setImdbRating(null);
        movieWithMissingData.setDescription(null);
        movieWithMissingData.setDirector(null);
        movieWithMissingData.setCast(null);

        List<Movie> moviesWithMissingData = Arrays.asList(movieWithMissingData);
        when(movieRepository.findAll()).thenReturn(moviesWithMissingData);
        when(movieRepository.save(any(Movie.class))).thenReturn(movieWithMissingData);

        // When
        List<Movie> result = movieService.updateMoviesWithSampleData();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getImdbRating());
        assertNotNull(result.get(0).getDescription());
        assertNotNull(result.get(0).getDirector());
        assertNotNull(result.get(0).getCast());
        verify(movieRepository, times(1)).findAll();
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void searchAndSortAllParamsWithLike_WithoutSelectedField_ShouldReturnAllMovies() {
        // Given
        searchAndSortDto.setMovie(testMovie);
        when(movieRepository.findAllByQuery(any(Query.class))).thenReturn(testMovies);

        // When
        List<?> result = movieService.searchAndSortAllParamsWithLike(searchAndSortDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movieRepository, times(1)).findAllByQuery(any(Query.class));
    }

    @Test
    void searchAndSortAllParamsWithLike_WithSelectedField_ShouldReturnSelectedField() {
        // Given
        searchAndSortDto.setMovie(testMovie);
        searchAndSortDto.setSelectedField("title");
        when(movieRepository.findAllByQuery(any(Query.class))).thenReturn(testMovies);

        // When
        List<?> result = movieService.searchAndSortAllParamsWithLike(searchAndSortDto);

        // Then
        assertNotNull(result);
        verify(movieRepository, times(1)).findAllByQuery(any(Query.class));
    }

    @Test
    void searchAllParamsWithIs_WithoutSelectedField_ShouldReturnAllMovies() {
        // Given
        searchAndSortDto.setMovie(testMovie);
        when(movieRepository.findAllByQuery(any(Query.class))).thenReturn(testMovies);

        // When
        List<?> result = movieService.searchAllParamsWithIs(searchAndSortDto);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(movieRepository, times(1)).findAllByQuery(any(Query.class));
    }

    @Test
    void searchAllParamsWithIs_WithSelectedField_ShouldReturnSelectedField() {
        // Given
        searchAndSortDto.setMovie(testMovie);
        searchAndSortDto.setSelectedField("imdbRating");
        when(movieRepository.findAllByQuery(any(Query.class))).thenReturn(testMovies);

        // When
        List<?> result = movieService.searchAllParamsWithIs(searchAndSortDto);

        // Then
        assertNotNull(result);
        verify(movieRepository, times(1)).findAllByQuery(any(Query.class));
    }

    @Test
    void generateSampleRating_ShouldReturnValidRating() {
        // Given
        String title = "Spider-Man";

        // When
        // We need to test this through updateMoviesWithSampleData or make the method package-private
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setImdbRating(null);

        when(movieRepository.findAll()).thenReturn(Arrays.asList(movie));
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        // When
        List<Movie> result = movieService.updateMoviesWithSampleData();

        // Then
        assertNotNull(result);
        assertNotNull(result.get(0).getImdbRating());
        assertTrue(Float.parseFloat(result.get(0).getImdbRating()) >= 0);
        verify(movieRepository, times(1)).save(any(Movie.class));
    }

    @Test
    void generateSampleDescription_ShouldReturnNonEmptyDescription() {
        // Given
        Movie movieWithoutDescription = new Movie();
        movieWithoutDescription.setTitle("Test Movie");
        movieWithoutDescription.setDescription(null);

        when(movieRepository.findAll()).thenReturn(Arrays.asList(movieWithoutDescription));
        when(movieRepository.save(any(Movie.class))).thenReturn(movieWithoutDescription);

        // When
        List<Movie> result = movieService.updateMoviesWithSampleData();

        // Then
        assertNotNull(result);
        assertNotNull(result.get(0).getDescription());
        assertFalse(result.get(0).getDescription().trim().isEmpty());
        verify(movieRepository, times(1)).save(any(Movie.class));
    }
}

