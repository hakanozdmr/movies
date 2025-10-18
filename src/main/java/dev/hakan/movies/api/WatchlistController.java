package dev.hakan.movies.api;

import dev.hakan.movies.business.service.WatchlistService;
import dev.hakan.movies.data.dto.WatchlistRequest;
import dev.hakan.movies.data.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/watchlist")
@CrossOrigin(origins = "*", maxAge = 3600)
public class WatchlistController {

    private final WatchlistService watchlistService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Movie>> getUserWatchlist(@PathVariable String userId) {
        try {
            List<Movie> watchlist = watchlistService.getUserWatchlist(userId);
            return ResponseEntity.ok(watchlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToWatchlist(
            @RequestHeader(value = "UserId", required = false) String userId,
            @Valid @RequestBody WatchlistRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null || userId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "User ID is required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean success = watchlistService.addToWatchlist(userId, request.getMovieId());
        
        if (success) {
            response.put("success", true);
            response.put("message", "Movie added to watchlist");
        } else {
            response.put("success", false);
            response.put("message", "Failed to add movie to watchlist or movie already exists");
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromWatchlist(
            @RequestHeader(value = "UserId", required = false) String userId,
            @Valid @RequestBody WatchlistRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null || userId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "User ID is required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean success = watchlistService.removeFromWatchlist(userId, request.getMovieId());
        
        if (success) {
            response.put("success", true);
            response.put("message", "Movie removed from watchlist");
        } else {
            response.put("success", false);
            response.put("message", "Failed to remove movie from watchlist");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{userId}/{movieId}")
    public ResponseEntity<Map<String, Object>> checkInWatchlist(
            @PathVariable String userId,
            @PathVariable String movieId) {
        
        Map<String, Object> response = new HashMap<>();
        boolean isInWatchlist = watchlistService.isInWatchlist(userId, movieId);
        
        response.put("inWatchlist", isInWatchlist);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
}
