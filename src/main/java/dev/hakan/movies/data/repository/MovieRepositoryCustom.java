package dev.hakan.movies.data.repository;

import com.mongodb.client.result.UpdateResult;
import dev.hakan.movies.data.model.Movie;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepositoryCustom {

    List<Movie> findAllByQuery(@Param("query") Query query);

    <T> ExecutableUpdateMovie<T> update();
    public interface ExecutableUpdateMovie<T>{

        public ExecutableUpdateMovie<T> matching(CriteriaDefinition criteriaDefinition) ;

        public ExecutableUpdateMovie<T> apply(UpdateDefinition update);
        public UpdateResult first();
    }
}
