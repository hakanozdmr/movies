package dev.hakan.movies.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(controllers = MovieController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.security.csrf.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

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

        testMovies = Arrays.asList(testMovie);

        searchAndSortDto = new SearchAndSortDto();
        searchAndSortDto.setMovie(testMovie);
    }

    @Test
    void getAllMovies_ShouldReturnOk() throws Exception {
        // Given
        when(movieService.findAllMovies()).thenReturn(testMovies);

        // When & Then
        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Movie"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void searchAndSort_WithValidRequest_ShouldReturnOk() throws Exception {
        // Given
        doReturn(testMovies).when(movieService).searchAndSortAllParamsWithLike(any(SearchAndSortDto.class));

        // When & Then
        mockMvc.perform(post("/movies/searchAndSort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchAndSortDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void searchAndSortWithIsPost_WithValidRequest_ShouldReturnOk() throws Exception {
        // Given
        doReturn(testMovies).when(movieService).searchAllParamsWithIs(any(SearchAndSortDto.class));

        // When & Then
        mockMvc.perform(post("/movies/searchAndSortWithIs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchAndSortDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateMovie_WithValidData_ShouldReturnOk() throws Exception {
        // Given
        String imdbId = "tt1234567";
        Map<String, String> updates = new HashMap<>();
        updates.put("description", "Updated description");
        updates.put("imdbRating", "9.0");

        when(movieService.updateMovieByImdbId(imdbId, "Updated description", "9.0"))
                .thenReturn(testMovie);

        // When & Then
        mockMvc.perform(put("/movies/update/{imdbId}", imdbId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    void updateMovie_WithNonExistentMovie_ShouldReturnNotFound() throws Exception {
        // Given
        String imdbId = "nonexistent";
        Map<String, String> updates = new HashMap<>();
        updates.put("description", "Updated description");

        when(movieService.updateMovieByImdbId(imdbId, "Updated description", null))
                .thenReturn(null);

        // When & Then
        mockMvc.perform(put("/movies/update/{imdbId}", imdbId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllMoviesForUpdate_ShouldReturnOk() throws Exception {
        // Given
        when(movieService.getAllMoviesForUpdate()).thenReturn(testMovies);

        // When & Then
        mockMvc.perform(get("/movies/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Movie"));
    }

    @Test
    void updateMoviesWithSampleData_ShouldReturnOk() throws Exception {
        // Given
        when(movieService.updateMoviesWithSampleData()).thenReturn(testMovies);

        // When & Then
        mockMvc.perform(post("/movies/update-with-sample-data")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Movies updated successfully with sample data"))
                .andExpect(jsonPath("$.updatedCount").value(1))
                .andExpect(jsonPath("$.movies").isArray());
    }
}
