package dev.hakan.movies.data.repository;

import com.mongodb.client.result.UpdateResult;
import dev.hakan.movies.data.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.List;

@Repository
public class MovieRepositoryCustomImpl implements MovieRepositoryCustom {

    private static final Query ALL_QUERY = new Query();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<Movie> findAllByQuery(Query query) {
        return mongoTemplate.find(query, Movie.class);
    }

    public <T> ExecutableUpdateMovie<T> update() {
        return new MovieUptaderSupport<>(this.mongoTemplate, ALL_QUERY, null, null, null, null, null);
    }

    public static class MovieUptaderSupport<T> implements ExecutableUpdateMovie<T> {
        private final MongoTemplate template;
        private final Query query;
        @Nullable
        private final UpdateDefinition update;
        @Nullable
        private final String collection;
        @Nullable
        private final FindAndModifyOptions findAndModifyOptions;
        @Nullable
        private final FindAndReplaceOptions findAndReplaceOptions;
        @Nullable
        private final Object replacement;

        public MovieUptaderSupport(MongoTemplate template, Query query, @Nullable UpdateDefinition update, @Nullable String collection, @Nullable FindAndModifyOptions findAndModifyOptions, @Nullable FindAndReplaceOptions findAndReplaceOptions, @Nullable Object replacement) {
            this.template = template;
            this.query = query;
            this.update = update;
            this.collection = collection;
            this.findAndModifyOptions = findAndModifyOptions;
            this.findAndReplaceOptions = findAndReplaceOptions;
            this.replacement = replacement;
        }

        public MovieUptaderSupport matching(CriteriaDefinition criteriaDefinition) {
            Assert.notNull(query, "Query must not be null");
            return new MovieUptaderSupport(this.template, Query.query(criteriaDefinition), this.update, this.collection, this.findAndModifyOptions, this.findAndReplaceOptions, this.replacement);
        }

        public MovieUptaderSupport apply(UpdateDefinition update) {
            Assert.notNull(update, "Update must not be null");
            return new MovieUptaderSupport(this.template, this.query, update, this.collection, this.findAndModifyOptions, this.findAndReplaceOptions, this.replacement);
        }

        public UpdateResult first() {
            return this.doUpdate();
        }
        private UpdateResult doUpdate() {
            return this.template.updateFirst(this.query,this.update,Movie.class);
        }

    }
}
