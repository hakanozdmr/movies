package dev.hakan.movies.business.service;

import dev.hakan.movies.data.model.Review;
import dev.hakan.movies.data.model.User;
import dev.hakan.movies.data.repository.MovieRepositoryCustom;
import dev.hakan.movies.data.repository.ReviewRepository;
import dev.hakan.movies.data.repository.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieService movieService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepositoryCustom.ExecutableUpdateMovie updateMovieMock;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private User testUser;
    private ObjectId reviewId;

    @BeforeEach
    void setUp() {
        reviewId = new ObjectId();
        testUser = new User();
        testUser.setId(new ObjectId());
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testReview = new Review();
        testReview.setId(reviewId);
        testReview.setTitle("Test Review");
        testReview.setBody("This is a test review");
        testReview.setRating(8);
        testReview.setMovieId("tt1234567");
        testReview.setUserId(testUser.getId().toString());
        testReview.setUserName("Test User");
        testReview.setUserEmail("test@example.com");
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
        testReview.setIsRecommended(true);
        testReview.setReviewType("non-spoiler");
        testReview.setHelpfulVotes(0);
        testReview.setTotalVotes(0);
    }

    @Test
    void createReview_WithValidUser_ShouldCreateReviewWithUserInfo() {
        // Given
        String reviewBody = "Great movie!";
        String imdbId = "tt1234567";
        String userId = testUser.getId().toString();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reviewRepository.insert(any(Review.class))).thenReturn(testReview);
        when(movieService.updateMovie()).thenReturn(updateMovieMock);
        when(updateMovieMock.matching(any(Criteria.class))).thenReturn(updateMovieMock);
        when(updateMovieMock.apply(any(Update.class))).thenReturn(updateMovieMock);

        // When
        Review result = reviewService.createReview(reviewBody, imdbId, userId);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(reviewRepository, times(1)).insert(any(Review.class));
        verify(movieService, times(1)).updateMovie();
    }

    @Test
    void createReview_WithNonExistentUser_ShouldCreateReviewWithoutUserInfo() {
        // Given
        String reviewBody = "Great movie!";
        String imdbId = "tt1234567";
        String userId = "nonexistent";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(reviewRepository.insert(any(Review.class))).thenReturn(testReview);
        when(movieService.updateMovie()).thenReturn(updateMovieMock);
        when(updateMovieMock.matching(any(Criteria.class))).thenReturn(updateMovieMock);
        when(updateMovieMock.apply(any(Update.class))).thenReturn(updateMovieMock);

        // When
        Review result = reviewService.createReview(reviewBody, imdbId, userId);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(reviewRepository, times(1)).insert(any(Review.class));
    }

    @Test
    void createReview_WithNullUserId_ShouldCreateReviewWithoutUserInfo() {
        // Given
        String reviewBody = "Great movie!";
        String imdbId = "tt1234567";
        String userId = null;

        when(reviewRepository.insert(any(Review.class))).thenReturn(testReview);
        when(movieService.updateMovie()).thenReturn(updateMovieMock);
        when(updateMovieMock.matching(any(Criteria.class))).thenReturn(updateMovieMock);
        when(updateMovieMock.apply(any(Update.class))).thenReturn(updateMovieMock);

        // When
        Review result = reviewService.createReview(reviewBody, imdbId, userId);

        // Then
        assertNotNull(result);
        verify(userRepository, never()).findById(any());
        verify(reviewRepository, times(1)).insert(any(Review.class));
    }

    @Test
    void createDetailedReview_WithValidData_ShouldCreateDetailedReview() {
        // Given
        String title = "Amazing Movie Review";
        String reviewBody = "This movie was absolutely fantastic!";
        Integer rating = 9;
        String imdbId = "tt1234567";
        String userId = testUser.getId().toString();
        List<String> tags = Arrays.asList("action", "thriller");
        Boolean isRecommended = true;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reviewRepository.insert(any(Review.class))).thenReturn(testReview);
        when(movieService.updateMovie()).thenReturn(updateMovieMock);
        when(updateMovieMock.matching(any(Criteria.class))).thenReturn(updateMovieMock);
        when(updateMovieMock.apply(any(Update.class))).thenReturn(updateMovieMock);

        // When
        Review result = reviewService.createDetailedReview(title, reviewBody, rating, imdbId, userId, "non-spoiler", tags, isRecommended);

        // Then
        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(reviewRepository, times(1)).insert(any(Review.class));
    }

    @Test
    void createDetailedReview_WithRatingOnly_ShouldSetIsRecommendedBasedOnRating() {
        // Given
        String title = "Review with Rating";
        String reviewBody = "Good movie";
        Integer rating = 7;
        String imdbId = "tt1234567";
        String userId = testUser.getId().toString();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reviewRepository.insert(any(Review.class))).thenReturn(testReview);
        when(movieService.updateMovie()).thenReturn(updateMovieMock);
        when(updateMovieMock.matching(any(Criteria.class))).thenReturn(updateMovieMock);
        when(updateMovieMock.apply(any(Update.class))).thenReturn(updateMovieMock);

        // When
        Review result = reviewService.createDetailedReview(title, reviewBody, rating, imdbId, userId, "non-spoiler", null, null);

        // Then
        assertNotNull(result);
        verify(reviewRepository, times(1)).insert(any(Review.class));
    }

    @Test
    void updateReview_WithValidId_ShouldUpdateReview() {
        // Given
        String reviewIdStr = reviewId.toString();
        String newTitle = "Updated Review Title";
        String newBody = "Updated review content";
        Integer newRating = 9;
        List<String> newTags = Arrays.asList("updated", "review");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        Review result = reviewService.updateReview(reviewIdStr, newTitle, newBody, newRating, newTags, "spoiler", true);

        // Then
        assertNotNull(result);
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    void updateReview_WithNonExistentId_ShouldThrowRuntimeException() {
        // Given
        String reviewIdStr = new ObjectId().toString();

        when(reviewRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.updateReview(reviewIdStr, "title", "body", 8, null, null, null);
        });

        assertTrue(exception.getMessage().contains("Review not found"));
        verify(reviewRepository, times(1)).findById(any(ObjectId.class));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void voteReview_WithValidIdAndHelpfulVote_ShouldUpdateVotes() {
        // Given
        String reviewIdStr = reviewId.toString();
        testReview.setHelpfulVotes(5);
        testReview.setTotalVotes(10);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        Review result = reviewService.voteReview(reviewIdStr, true);

        // Then
        assertNotNull(result);
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    void voteReview_WithNullVotes_ShouldInitializeVotes() {
        // Given
        String reviewIdStr = reviewId.toString();
        testReview.setHelpfulVotes(null);
        testReview.setTotalVotes(null);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        Review result = reviewService.voteReview(reviewIdStr, false);

        // Then
        assertNotNull(result);
        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).save(testReview);
    }

    @Test
    void voteReview_WithNonExistentId_ShouldThrowRuntimeException() {
        // Given
        String reviewIdStr = new ObjectId().toString();

        when(reviewRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.voteReview(reviewIdStr, true);
        });

        assertTrue(exception.getMessage().contains("Review not found"));
        verify(reviewRepository, times(1)).findById(any(ObjectId.class));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview_WithValidId_ShouldDeleteReview() {
        // Given
        String reviewIdStr = reviewId.toString();

        // When
        reviewService.deleteReview(reviewIdStr);

        // Then
        verify(reviewRepository, times(1)).deleteById(reviewId);
    }

    @Test
    void updateReview_WithRatingOnly_ShouldSetIsRecommendedBasedOnRating() {
        // Given
        String reviewIdStr = reviewId.toString();
        Integer newRating = 5; // Less than 6, should set isRecommended to false

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // When
        Review result = reviewService.updateReview(reviewIdStr, null, null, newRating, null, null, null);

        // Then
        assertNotNull(result);
        verify(reviewRepository, times(1)).save(testReview);
    }
}
