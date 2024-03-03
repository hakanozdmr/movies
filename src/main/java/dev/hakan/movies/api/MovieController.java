package dev.hakan.movies.api;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
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
    @GetMapping("/searchAndSort")
    public  ResponseEntity<List<?>> searchAndSort(@RequestBody SearchAndSortDto request){
            return new ResponseEntity<>(movieService.searchAndSortAllParamsWithLike(request), HttpStatus.OK) ;
    }
    @GetMapping("/searchAndSortWithIs")
    public   ResponseEntity<List<?>> searchAndSortWithIs(@RequestBody SearchAndSortDto request){
            return new ResponseEntity<>(movieService.searchAllParamsWithIs(request), HttpStatus.OK) ;
    }
}
