package dev.hakan.movies.data.repository;

import dev.hakan.movies.data.model.WatchlistItem;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends MongoRepository<WatchlistItem, String> {
    List<WatchlistItem> findByUserIdAndIsActiveTrue(ObjectId userId);
    Optional<WatchlistItem> findByUserIdAndMovie_IdAndIsActiveTrue(ObjectId userId, ObjectId movieId);
    boolean existsByUserIdAndMovie_IdAndIsActiveTrue(ObjectId userId, ObjectId movieId);
    void deleteByUserIdAndMovie_Id(ObjectId userId, ObjectId movieId);
}
