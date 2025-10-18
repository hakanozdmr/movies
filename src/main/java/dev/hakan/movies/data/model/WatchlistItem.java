package dev.hakan.movies.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Document(collection = "watchlist")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WatchlistItem {
    @Id
    private ObjectId id;
    
    private ObjectId userId;
    
    @DocumentReference
    private Movie movie;
    
    private LocalDateTime addedAt = LocalDateTime.now();
    private boolean isActive = true;
}

