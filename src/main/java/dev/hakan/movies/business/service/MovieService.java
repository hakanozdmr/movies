package dev.hakan.movies.business.service;

import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.enums.SortingDirection;
import dev.hakan.movies.data.model.Movie;
import dev.hakan.movies.data.repository.MovieRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.hakan.movies.util.QueryUtil.*;
import static dev.hakan.movies.util.Utility.getDeclaredFieldFromObject;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final MongoTemplate mongoTemplate;

    public MovieService(MovieRepository movieRepository, MongoTemplate mongoTemplate) {
        this.movieRepository = movieRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Movie> getAllMovies(){
        return  movieRepository.findAll();
    }
    public Movie getMovieByImdbId(String id){
        return  movieRepository.findByImdbId(id);
    }
    public List<Movie> findAllByTitle(String title){
        return  movieRepository.findAllByTitle(title);
    }
    public  List<Movie> searchOneParamWithLike(HttpServletRequest request){
        return mongoTemplate.find(getOneParamWithLike(requestToMap(request)), Movie.class);
    }
    public  List<Movie> searchAllParamsWithLike(HttpServletRequest request){
        return mongoTemplate.find(getAllParamWithLike(requestToMap(request)), Movie.class);
    }
    public  List<Movie> searchAndSortAllParamsWithLike(SearchAndSortDto request){
        Query query = getAllParamWithLike(getDeclaredFieldFromObject(request.getMovie()));
        query = request.getDirection().equals(SortingDirection.DESC)
                ? query.with(Sort.by(request.getSortedField()).descending())
                : query.with(Sort.by(request.getSortedField()).ascending());
        return mongoTemplate.find(query, Movie.class);
    }
    public  List<Movie> searchAllParamsWithIs(Map<String,String> map){
        return mongoTemplate.find(getAllParamWithIs(map), Movie.class);
    }
    public Map<String,String> requestToMap(HttpServletRequest request){
        Map<String,String> map = new LinkedHashMap<>();
        java.util.Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            map.put(paramName,paramValue);
        }
        return map;
    }
}
