package dev.hakan.movies.api;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.business.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    private final MovieService movieService;

    public ReviewController(ReviewService reviewService, MovieService movieService) {
        this.reviewService = reviewService;
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Map<String,String> payload){
         reviewService.createReview(payload.get("reviewBody"),payload.get("imdbId"));

        return new ResponseEntity<>(movieService.getMovieByImdbId(payload.get("imdbId")), HttpStatus.CREATED);
     }
}
