package dev.hakan.movies.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private ObjectId id;
    
    @Indexed(unique = true)
    private String email;
    
    private String name;
    private String password;
    private boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastLoginAt;
    
    // OAuth2 Google fields
    private String googleId;
    private String picture;
    private String provider = "local"; // "local" or "google"
    
    // Watchlist için referans
    private List<String> watchlistMovieIds;
}
