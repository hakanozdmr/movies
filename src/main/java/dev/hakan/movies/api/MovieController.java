package dev.hakan.movies.api;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.model.Movie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController{
    MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> allMovies(){
        return new ResponseEntity<>(movieService.getAllMovies(), HttpStatus.OK) ;
    }
    @GetMapping("/{id}")
    public ResponseEntity<Movie> gelMovieByImdbid(@PathVariable String id){
        return new ResponseEntity<>(movieService.getMovieByImdbId(id), HttpStatus.OK) ;
    }
    @GetMapping("/search")
    public  ResponseEntity<List<Movie>> findByLike(HttpServletRequest request){
        return new ResponseEntity<>(movieService.searchAllParamsWithLike(request), HttpStatus.OK) ;
    }
    @GetMapping("/searchAndSort")
    public  ResponseEntity<List<Movie>> searchAndSortByDesc(@RequestBody SearchAndSortDto request){
        return new ResponseEntity<>(movieService.searchAndSortAllParamsWithLike(request), HttpStatus.OK) ;
        //return null;
    }
    @GetMapping("/searchOneParam")
    public  ResponseEntity<List<Movie>> findAllParamsByLike(HttpServletRequest request){
        return new ResponseEntity<>(movieService.searchOneParamWithLike(request), HttpStatus.OK) ;
    }
    //TODO : /searchOne /searchAll
}
