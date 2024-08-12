package dev.hakan.movies.api;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.business.service.ReviewService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.model.Movie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    private final MovieService movieService;

    public ReviewController(ReviewService reviewService, MovieService movieService) {
        this.reviewService = reviewService;
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Map<String,String> payload){
        //TODO: Burayı daha düzenli yaz
        reviewService.createReview(payload.get("reviewBody"),payload.get("imdbId"));
        Movie movie = new Movie();
        movie.setImdbId(payload.get("imdbId"));
        SearchAndSortDto searchAndSortDto = new SearchAndSortDto();
        searchAndSortDto.setMovie(movie);
        return new ResponseEntity<>(movieService.searchAllParamsWithIs(searchAndSortDto), HttpStatus.CREATED);
     }
}
