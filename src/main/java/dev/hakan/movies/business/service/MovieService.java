package dev.hakan.movies.business.service;

import dev.hakan.movies.data.dto.SearchAndSortDto;
import dev.hakan.movies.data.enums.SortingDirection;
import dev.hakan.movies.data.model.Movie;
import dev.hakan.movies.data.repository.MovieRepository;
import dev.hakan.movies.data.repository.MovieRepositoryCustom;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static dev.hakan.movies.util.QueryUtil.getAllParamWithIs;
import static dev.hakan.movies.util.QueryUtil.getAllParamWithLike;
import static dev.hakan.movies.util.Utility.getDeclaredFieldFromObject;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;

    }
    public List<Movie> findAllMovies(){
        return movieRepository.findAll();
    }
    public   List<?> searchAndSortAllParamsWithLike(SearchAndSortDto request){
        if (request.getSelectedField() != null){
            return searchOneFieldAndSortWithLike(request);
        }
        Query query = new Query();
        if (request.getMovie() != null ){
            query = getAllParamWithLike(getDeclaredFieldFromObject(request.getMovie()));
        }
        query.with(SortedFieldIsNull(request));
        return movieRepository.findAllByQuery(query);
    }
    private List<?> searchOneFieldAndSortWithLike(SearchAndSortDto request) {
        Query query = new Query();
        if (request.getMovie() != null ){
            query = getAllParamWithLike(getDeclaredFieldFromObject(request.getMovie()));
        }
        query.with(SortedFieldIsNull(request));
        query.fields().include(request.getSelectedField());
        List<Movie> list = movieRepository.findAllByQuery(query);

        return selectOneField(list, request);
    }
    private List<?> searchOneFieldAndSortWithIs(SearchAndSortDto request) {
        Query query = new Query();
        if (request.getMovie() != null ){
            query = getAllParamWithIs(getDeclaredFieldFromObject(request.getMovie()));
        }
        query.with(SortedFieldIsNull(request));
        query.fields().include(request.getSelectedField());
        List<Movie> list = movieRepository.findAllByQuery(query);

        return selectOneField(list, request);
    }
    public List<?> searchAllParamsWithIs(SearchAndSortDto request){
        if (request.getSelectedField() != null){
            return searchOneFieldAndSortWithIs(request);
        }
        Query query = new Query();
        if (request.getMovie() != null ){
            query = getAllParamWithIs(getDeclaredFieldFromObject(request.getMovie()));
        }
        query.with(SortedFieldIsNull(request));
        return movieRepository.findAllByQuery(query);
    }
    private Map<String,String> requestToMap(HttpServletRequest request){
        Map<String,String> map = new LinkedHashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            map.put(paramName,paramValue);
        }
        return map;
    }
    private PageRequest SortedFieldIsNull(SearchAndSortDto request){
        PageRequest pageRequest;
        if (request.getPageableDto() != null ){
            int pageSize = request.getPageableDto().getPageSize() == 0 ? Integer.MAX_VALUE : request.getPageableDto().getPageSize();
            if (request.getPageableDto().getSortedField() != null){
                SortingDirection sortingDirection = request.getPageableDto().getDirection() == null ? SortingDirection.ASC : request.getPageableDto().getDirection();
                pageRequest = SortingDirection.ASC.name().equals(sortingDirection.name())
                        ? PageRequest.of(request.getPageableDto().getPageNumber(),pageSize, Sort.Direction.ASC,request.getPageableDto().getSortedField())
                        : PageRequest.of(request.getPageableDto().getPageNumber(),pageSize, Sort.Direction.DESC,request.getPageableDto().getSortedField());
            }
            else {
                pageRequest=PageRequest.of(request.getPageableDto().getPageNumber(),request.getPageableDto().getPageSize());
            }
        }else {
            pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        }
        return pageRequest;
    }
    private <T> List<T>  selectOneField(List<T> list , SearchAndSortDto request){
        List<T> fieldList =  list.stream()
                .map(movie -> {
                    try {
                        Field field = Movie.class.getDeclaredField(request.getSelectedField());
                        field.setAccessible(true);
                        Object value = field.get(movie);
                        if (value != null) {
                            return (T) value;
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return fieldList;
    }

    public <T> MovieRepositoryCustom.ExecutableUpdateMovie<T> updateMovie(){
        return movieRepository.update();
    }

    public Movie updateMovieById(String id, Movie updatedMovie) {
        return movieRepository.save(updatedMovie);
    }

    public Movie updateMovieByImdbId(String imdbId, String description, String imdbRating) {
        Movie movie = movieRepository.findByImdbId(imdbId);
        if (movie != null) {
            if (description != null) {
                movie.setDescription(description);
            }
            if (imdbRating != null) {
                movie.setImdbRating(imdbRating);
            }
            return movieRepository.save(movie);
        }
        return null;
    }

    public List<Movie> getAllMoviesForUpdate() {
        return movieRepository.findAll();
    }

    public List<Movie> updateMoviesWithSampleData() {
        List<Movie> movies = movieRepository.findAll();
        List<Movie> updatedMovies = new java.util.ArrayList<>();
        
        // Sample IMDB ratings and descriptions for common movies
        // In a real application, you would fetch this data from IMDB API or OMDB API
        for (Movie movie : movies) {
            boolean updated = false;
            
            // Add sample IMDB rating if not present
            if (movie.getImdbRating() == null || movie.getImdbRating().isEmpty()) {
                movie.setImdbRating(generateSampleRating(movie.getTitle()));
                updated = true;
            }
            
            // Add sample description if not present
            if (movie.getDescription() == null || movie.getDescription().isEmpty()) {
                movie.setDescription(generateSampleDescription(movie.getTitle()));
                updated = true;
            }
            
            // Add sample director if not present
            if (movie.getDirector() == null || movie.getDirector().isEmpty()) {
                movie.setDirector(generateSampleDirector(movie.getTitle()));
                updated = true;
            }
            
            // Add sample cast if not present
            if (movie.getCast() == null || movie.getCast().isEmpty()) {
                movie.setCast(generateSampleCast(movie.getTitle()));
                updated = true;
            }
            
            if (updated) {
                Movie savedMovie = movieRepository.save(movie);
                updatedMovies.add(savedMovie);
            }
        }
        
        return updatedMovies;
    }
    
    private String generateSampleDirector(String title) {
        if (title != null) {
            String lowerTitle = title.toLowerCase();
            if (lowerTitle.contains("spider") || lowerTitle.contains("batman")) return "Christopher Nolan";
            if (lowerTitle.contains("avengers")) return "Anthony & Joe Russo";
            if (lowerTitle.contains("star wars")) return "George Lucas";
            return "Steven Spielberg";
        }
        return "Unknown Director";
    }
    
    private String generateSampleCast(String title) {
        if (title != null) {
            String lowerTitle = title.toLowerCase();
            if (lowerTitle.contains("spider")) return "Tobey Maguire, Kirsten Dunst, James Franco";
            if (lowerTitle.contains("batman")) return "Christian Bale, Heath Ledger, Aaron Eckhart";
            if (lowerTitle.contains("iron man")) return "Robert Downey Jr., Gwyneth Paltrow, Jeff Bridges";
            if (lowerTitle.contains("avengers")) return "Robert Downey Jr., Chris Evans, Mark Ruffalo";
            return "Ensemble Cast, Supporting Actors, Lead Actors";
        }
        return "Feature Film Cast";
    }
    
    private String generateSampleRating(String title) {
        // This is sample data - in real app, fetch from IMDB API or OMDB API
        if (title != null) {
            String lowerTitle = title.toLowerCase();
            if (lowerTitle.contains("spider") || lowerTitle.contains("batman") || lowerTitle.contains("superman")) return "8.1";
            if (lowerTitle.contains("avengers") || lowerTitle.contains("iron man")) return "8.0";
            if (lowerTitle.contains("drama")) return "8.2";
            if (lowerTitle.contains("action")) return "7.5";
            if (lowerTitle.contains("comedy")) return "7.1";
            if (lowerTitle.contains("horror")) return "6.8";
            if (lowerTitle.contains("sci-fi") || lowerTitle.contains("sci fi") || lowerTitle.contains("star wars")) return "8.5";
            if (lowerTitle.contains("romance") || lowerTitle.contains("love")) return "7.3";
            if (lowerTitle.contains("thriller")) return "7.7";
            if (lowerTitle.contains("adventure")) return "7.6";
        }
        return "7.2"; // Default rating
    }
    
    private String generateSampleDescription(String title) {
        // This is sample data - in real app, fetch from IMDB API or OMDB API
        if (title != null) {
            return "Experience the captivating world of " + title + 
                   ". This exceptional film delivers outstanding performances, stunning cinematography, and a compelling storyline that will keep you engaged from start to finish. " +
                   "Featuring remarkable character development and masterful storytelling, this movie is a must-watch for cinema enthusiasts.";
        }
        return "Experience an incredible cinematic journey with this remarkable film that features outstanding performances, compelling storylines, and exceptional production values that showcase the best of modern filmmaking.";
    }
}
