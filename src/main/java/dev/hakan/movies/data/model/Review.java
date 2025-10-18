package dev.hakan.movies.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "reviews")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
        @Id
        private ObjectId id;
        private String title;          // Review başlığı
        private String body;           // Review içeriği
        private Integer rating;        // 1-10 arası rating
        private String movieId;        // Hangi film için review
        private String userId;
        private String userName;
        private String userEmail;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<String> tags;     // #action #recommendation gibi etiketler
        private Boolean isRecommended; // Film öneriliyor mu?
        private Integer helpfulVotes;  // Helpful oy sayısı
        private Integer totalVotes;    // Toplam oy sayısı
        private String reviewType;     // "spoiler", "non-spoiler"
        private Boolean isVerified;    // Verified purchase/review

        public Review(String body, String userId, String userName, String userEmail) {
                this.body = body;
                this.userId = userId;
                this.userName = userName;
                this.userEmail = userEmail;
                this.createdAt = LocalDateTime.now();
                this.updatedAt = LocalDateTime.now();
                this.helpfulVotes = 0;
                this.totalVotes = 0;
                this.isRecommended = true;
                this.isVerified = false;
                this.reviewType = "non-spoiler";
        }

        public Review(String body) {
                this.body = body;
                this.createdAt = LocalDateTime.now();
                this.updatedAt = LocalDateTime.now();
                this.helpfulVotes = 0;
                this.totalVotes = 0;
                this.isRecommended = true;
                this.isVerified = false;
                this.reviewType = "non-spoiler";
        }

        public Review(String title, String body, Integer rating, String movieId, String userId, String userName, String userEmail) {
                this.title = title;
                this.body = body;
                this.rating = rating;
                this.movieId = movieId;
                this.userId = userId;
                this.userName = userName;
                this.userEmail = userEmail;
                this.createdAt = LocalDateTime.now();
                this.updatedAt = LocalDateTime.now();
                this.helpfulVotes = 0;
                this.totalVotes = 0;
                this.isRecommended = rating != null && rating >= 6;
                this.isVerified = false;
                this.reviewType = "non-spoiler";
        }
}
