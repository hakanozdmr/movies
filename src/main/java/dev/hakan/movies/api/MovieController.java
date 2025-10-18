package dev.hakan.movies.api;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.model.Movie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
@CrossOrigin(origins = "*")
public class MovieController{
    MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public  ResponseEntity<List<?>> getAllMovies(){
        return new ResponseEntity<>(movieService.findAllMovies(), HttpStatus.OK) ;
    }
    @PostMapping("/searchAndSort")
    public  ResponseEntity<List<?>> searchAndSort(@RequestBody SearchAndSortDto request){
            return new ResponseEntity<>(movieService.searchAndSortAllParamsWithLike(request), HttpStatus.OK) ;
    }
    @GetMapping("/searchAndSortWithIs")
    public   ResponseEntity<List<?>> searchAndSortWithIs(@RequestBody SearchAndSortDto request){
            return new ResponseEntity<>(movieService.searchAllParamsWithIs(request), HttpStatus.OK) ;
    }

    @PostMapping("/searchAndSortWithIs")
    public ResponseEntity<List<?>> searchAndSortWithIsPost(@RequestBody SearchAndSortDto request){
        return new ResponseEntity<>(movieService.searchAllParamsWithIs(request), HttpStatus.OK);
    }

    @PutMapping("/update/{imdbId}")
    public ResponseEntity<Movie> updateMovie(@PathVariable String imdbId, @RequestBody Map<String, String> updates) {
        String description = updates.get("description");
        String imdbRating = updates.get("imdbRating");
        
        Movie updatedMovie = movieService.updateMovieByImdbId(imdbId, description, imdbRating);
        
        if (updatedMovie != null) {
            return new ResponseEntity<>(updatedMovie, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Movie>> getAllMoviesForUpdate() {
        List<Movie> movies = movieService.getAllMoviesForUpdate();
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    @PostMapping("/update-with-sample-data")
    public ResponseEntity<Map<String, Object>> updateMoviesWithSampleData() {
        List<Movie> updatedMovies = movieService.updateMoviesWithSampleData();
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Movies updated successfully with sample data");
        response.put("updatedCount", updatedMovies.size());
        response.put("movies", updatedMovies);
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
