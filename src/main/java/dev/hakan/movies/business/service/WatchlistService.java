package dev.hakan.movies.business.service;

import dev.hakan.movies.data.model.Movie;
import dev.hakan.movies.data.model.User;
import dev.hakan.movies.data.model.WatchlistItem;
import dev.hakan.movies.data.repository.MovieRepository;
import dev.hakan.movies.data.repository.UserRepository;
import dev.hakan.movies.data.repository.WatchlistRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    @Autowired
    public WatchlistService(WatchlistRepository watchlistRepository, 
                           MovieRepository movieRepository,
                           UserRepository userRepository) {
        this.watchlistRepository = watchlistRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
    }

    public List<Movie> getUserWatchlist(String userId) {
        ObjectId userObjectId = new ObjectId(userId);
        List<WatchlistItem> watchlistItems = watchlistRepository.findByUserIdAndIsActiveTrue(userObjectId);
        
        return watchlistItems.stream()
                .map(WatchlistItem::getMovie)
                .toList();
    }

    public boolean addToWatchlist(String userId, String movieId) {
        try {
            // Kullanıcı var mı kontrol et
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return false;
            }

            // Film var mı kontrol et - önce imdbId ile, sonra ObjectId ile dene
            Movie movie = movieRepository.findByImdbId(movieId);
            if (movie == null) {
                // imdbId ile bulunamadıysa ObjectId ile dene
                try {
                    ObjectId movieObjectId = new ObjectId(movieId);
                    Optional<Movie> movieOpt = movieRepository.findById(movieObjectId);
                    if (movieOpt.isEmpty()) {
                        return false;
                    }
                    movie = movieOpt.get();
                } catch (Exception e) {
                    return false;
                }
            }

            ObjectId userObjectId = new ObjectId(userId);
            ObjectId movieObjectId = movie.getId();

            // Zaten watchlist'te var mı kontrol et
            if (watchlistRepository.existsByUserIdAndMovie_IdAndIsActiveTrue(userObjectId, movieObjectId)) {
                return false; // Zaten ekli
            }

            // Watchlist'e ekle
            WatchlistItem watchlistItem = new WatchlistItem();
            watchlistItem.setUserId(userObjectId);
            watchlistItem.setMovie(movie);
            watchlistItem.setActive(true);

            watchlistRepository.save(watchlistItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean removeFromWatchlist(String userId, String movieId) {
        try {
            ObjectId userObjectId = new ObjectId(userId);
            
            // Film var mı kontrol et - önce imdbId ile, sonra ObjectId ile dene
            Movie movie = movieRepository.findByImdbId(movieId);
            if (movie == null) {
                try {
                    ObjectId movieObjectId = new ObjectId(movieId);
                    Optional<Movie> movieOpt = movieRepository.findById(movieObjectId);
                    if (movieOpt.isEmpty()) {
                        return false;
                    }
                    movie = movieOpt.get();
                } catch (Exception e) {
                    return false;
                }
            }

            // Watchlist item'ı bul
            Optional<WatchlistItem> watchlistItemOpt = watchlistRepository.findByUserIdAndMovie_IdAndIsActiveTrue(userObjectId, movie.getId());
            
            if (watchlistItemOpt.isPresent()) {
                WatchlistItem item = watchlistItemOpt.get();
                item.setActive(false); // Soft delete
                watchlistRepository.save(item);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isInWatchlist(String userId, String movieId) {
        try {
            ObjectId userObjectId = new ObjectId(userId);
            
            // Film var mı kontrol et - önce imdbId ile, sonra ObjectId ile dene
            Movie movie = movieRepository.findByImdbId(movieId);
            if (movie == null) {
                try {
                    ObjectId movieObjectId = new ObjectId(movieId);
                    Optional<Movie> movieOpt = movieRepository.findById(movieObjectId);
                    if (movieOpt.isEmpty()) {
                        return false;
                    }
                    movie = movieOpt.get();
                } catch (Exception e) {
                    return false;
                }
            }

            return watchlistRepository.existsByUserIdAndMovie_IdAndIsActiveTrue(userObjectId, movie.getId());
        } catch (Exception e) {
            return false;
        }
    }
}
