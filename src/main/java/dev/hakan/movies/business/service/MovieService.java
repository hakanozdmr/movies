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
}
