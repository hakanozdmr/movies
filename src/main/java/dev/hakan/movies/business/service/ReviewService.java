package dev.hakan.movies.business.service;

import dev.hakan.movies.data.model.Review;
import dev.hakan.movies.data.model.User;
import dev.hakan.movies.data.repository.ReviewRepository;
import dev.hakan.movies.data.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieService movieService;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, MovieService movieService, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.movieService = movieService;
        this.userRepository = userRepository;
    }

    public Review createReview(String reviewBody, String imdbId, String userId){
        Review review;
        
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    review = reviewRepository.insert(new Review(reviewBody, userId, user.getName(), user.getEmail()));
                } else {
                    log.warn("User not found with id: {}, creating review without user info", userId);
                    review = reviewRepository.insert(new Review(reviewBody));
                }
            } catch (Exception e) {
                log.error("Error finding user with id: {}, creating review without user info", userId, e);
                review = reviewRepository.insert(new Review(reviewBody));
            }
        } else {
            log.warn("UserId is null or empty, creating review without user info");
            review = reviewRepository.insert(new Review(reviewBody));
        }
        
        movieService.updateMovie()
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviewIds").value(review))
                .first();
        log.info("The movie with this imdbId : {} has been updated",imdbId);
        return review;
    }

    // Yeni detaylı review oluşturma method'u
    public Review createDetailedReview(String title, String reviewBody, Integer rating, String imdbId, String userId, String reviewType, List<String> tags, Boolean isRecommended) {
        Review review;
        
        if (userId != null && !userId.trim().isEmpty()) {
            try {
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    review = new Review(title, reviewBody, rating, imdbId, userId, user.getName(), user.getEmail());
                } else {
                    log.warn("User not found with id: {}, creating review without user info", userId);
                    review = new Review(reviewBody);
                    review.setTitle(title);
                    review.setRating(rating);
                    review.setMovieId(imdbId);
                }
            } catch (Exception e) {
                log.error("Error finding user with id: {}, creating review without user info", userId, e);
                review = new Review(reviewBody);
                review.setTitle(title);
                review.setRating(rating);
                review.setMovieId(imdbId);
            }
        } else {
            log.warn("UserId is null or empty, creating review without user info");
            review = new Review(reviewBody);
            review.setTitle(title);
            review.setRating(rating);
            review.setMovieId(imdbId);
        }

        // Ekstra alanları set et
        if (reviewType != null) review.setReviewType(reviewType);
        if (tags != null && !tags.isEmpty()) review.setTags(tags);
        if (isRecommended != null) {
            review.setIsRecommended(isRecommended);
        } else if (rating != null) {
            review.setIsRecommended(rating >= 6);
        }

        review.setUpdatedAt(LocalDateTime.now());
        review = reviewRepository.insert(review);
        
        movieService.updateMovie()
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviewIds").value(review))
                .first();
        
        log.info("Detailed review created for movie imdbId: {} by user: {}", imdbId, userId);
        return review;
    }

    // Review güncelleme method'u
    public Review updateReview(String reviewId, String title, String reviewBody, Integer rating, List<String> tags, String reviewType, Boolean isRecommended) {
        Optional<Review> reviewOpt = reviewRepository.findById(new ObjectId(reviewId));
        
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            
            if (title != null) review.setTitle(title);
            if (reviewBody != null) review.setBody(reviewBody);
            if (rating != null) {
                review.setRating(rating);
                if (isRecommended == null) {
                    review.setIsRecommended(rating >= 6);
                }
            }
            if (tags != null) review.setTags(tags);
            if (reviewType != null) review.setReviewType(reviewType);
            if (isRecommended != null) review.setIsRecommended(isRecommended);
            
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        
        throw new RuntimeException("Review not found with id: " + reviewId);
    }

    // Review oy verme method'u
    public Review voteReview(String reviewId, Boolean isHelpful) {
        Optional<Review> reviewOpt = reviewRepository.findById(new ObjectId(reviewId));
        
        if (reviewOpt.isPresent()) {
            Review review = reviewOpt.get();
            
            if (review.getHelpfulVotes() == null) review.setHelpfulVotes(0);
            if (review.getTotalVotes() == null) review.setTotalVotes(0);
            
            review.setTotalVotes(review.getTotalVotes() + 1);
            if (isHelpful) {
                review.setHelpfulVotes(review.getHelpfulVotes() + 1);
            }
            
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        
        throw new RuntimeException("Review not found with id: " + reviewId);
    }

    // Review silme method'u
    public void deleteReview(String reviewId) {
        reviewRepository.deleteById(new ObjectId(reviewId));
        log.info("Review deleted with id: {}", reviewId);
    }
}
