package dev.hakan.movies.business.service;

import dev.hakan.movies.data.model.Review;
import dev.hakan.movies.data.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final MovieService movieService;

    public ReviewService(ReviewRepository reviewRepository,MovieService movieService) {
        this.reviewRepository = reviewRepository;
        this.movieService = movieService;
    }

    public Review createReview(String reviewBody, String imdbId){
        Review review =  reviewRepository.insert(new Review(reviewBody));
        movieService.updateMovie()
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviewIds").value(review))
                .first();
        log.info("The movie with this imdbId : {} has been updated",imdbId);
        return review;
    }
}
