package dev.hakan.movies.api;

import dev.hakan.movies.business.service.MovieService;
import dev.hakan.movies.business.service.ReviewService;
import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.model.Movie;
import dev.hakan.movies.data.model.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    private final MovieService movieService;

    public ReviewController(ReviewService reviewService, MovieService movieService) {
        this.reviewService = reviewService;
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestBody Map<String,String> payload,
            @RequestHeader(value = "UserId", required = false) String userId,
            HttpServletRequest request){
        
        // Alternative way to get header in case of case sensitivity issues
        String userIdHeader = request.getHeader("UserId");
        if (userIdHeader == null) {
            userIdHeader = request.getHeader("userid");
        }
        if (userIdHeader == null) {
            userIdHeader = userId; // fallback to parameter
        }
        
        log.info("Creating review - UserId: {}, ReviewBody: {}, ImdbId: {}", 
                userIdHeader, payload.get("reviewBody"), payload.get("imdbId"));
        
        reviewService.createReview(payload.get("reviewBody"), payload.get("imdbId"), userIdHeader);
        Movie movie = new Movie();
        movie.setImdbId(payload.get("imdbId"));
        SearchAndSortDto searchAndSortDto = new SearchAndSortDto();
        searchAndSortDto.setMovie(movie);
        return new ResponseEntity<>(movieService.searchAllParamsWithIs(searchAndSortDto), HttpStatus.CREATED);
     }

    @PostMapping("/detailed")
    public ResponseEntity<?> createDetailedReview(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "UserId", required = false) String userId,
            HttpServletRequest request) {
        
        String userIdHeader = request.getHeader("UserId");
        if (userIdHeader == null) {
            userIdHeader = request.getHeader("userid");
        }
        if (userIdHeader == null) {
            userIdHeader = userId;
        }

        String title = (String) payload.get("title");
        String reviewBody = (String) payload.get("reviewBody");
        Integer rating = payload.get("rating") != null ? Integer.valueOf(payload.get("rating").toString()) : null;
        String imdbId = (String) payload.get("imdbId");
        String reviewType = (String) payload.get("reviewType");
        List<String> tags = (List<String>) payload.get("tags");
        Boolean isRecommended = payload.get("isRecommended") != null ? 
            Boolean.valueOf(payload.get("isRecommended").toString()) : null;

        log.info("Creating detailed review - UserId: {}, Title: {}, Rating: {}, ImdbId: {}", 
                userIdHeader, title, rating, imdbId);

        Review review = reviewService.createDetailedReview(
            title, reviewBody, rating, imdbId, userIdHeader, reviewType, tags, isRecommended);

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        SearchAndSortDto searchAndSortDto = new SearchAndSortDto();
        searchAndSortDto.setMovie(movie);
        return new ResponseEntity<>(movieService.searchAllParamsWithIs(searchAndSortDto), HttpStatus.CREATED);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "UserId", required = false) String userId) {
        
        try {
            String title = (String) payload.get("title");
            String reviewBody = (String) payload.get("reviewBody");
            Integer rating = payload.get("rating") != null ? Integer.valueOf(payload.get("rating").toString()) : null;
            List<String> tags = (List<String>) payload.get("tags");
            String reviewType = (String) payload.get("reviewType");
            Boolean isRecommended = payload.get("isRecommended") != null ? 
                Boolean.valueOf(payload.get("isRecommended").toString()) : null;

            Review updatedReview = reviewService.updateReview(
                reviewId, title, reviewBody, rating, tags, reviewType, isRecommended);
            
            return new ResponseEntity<>(updatedReview, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error updating review: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{reviewId}/vote")
    public ResponseEntity<Review> voteReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, Boolean> payload,
            @RequestHeader(value = "UserId", required = false) String userId) {
        
        try {
            Boolean isHelpful = payload.get("isHelpful");
            Review updatedReview = reviewService.voteReview(reviewId, isHelpful);
            return new ResponseEntity<>(updatedReview, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error voting on review: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader(value = "UserId", required = false) String userId) {
        
        try {
            reviewService.deleteReview(reviewId);
            return new ResponseEntity<>(Map.of("message", "Review deleted successfully"), HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Error deleting review: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
