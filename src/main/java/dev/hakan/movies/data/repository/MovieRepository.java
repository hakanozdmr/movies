package dev.hakan.movies.data.repository;

import dev.hakan.movies.data.model.Movie;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableMongoRepositories
public interface MovieRepository extends MongoRepository<Movie, ObjectId> {
         Movie findByImdbId(String id);

         List<Movie> findAllByTitle(String title);
}
