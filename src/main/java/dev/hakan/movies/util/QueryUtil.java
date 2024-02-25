package dev.hakan.movies.util;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Map;

import static dev.hakan.movies.util.Utility.getOneParam;

public class QueryUtil {

    public static Query getAllParamWithLike(Map<String,String> map){
        String key;
        String value;
        Query query = new Query();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            query.addCriteria(Criteria.where(String.format("%s", key)).regex(value));
        }
        return query;
    }


    public static Query getAllParamWithIs(Map<String,String> map){
        String key;
        String value;
        Query query = new Query();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            query.addCriteria(Criteria.where(String.format("%s", key)).is(value));
        }
        return query;
    }
    public static Query getOneParamWithLike(Map<String,String> map){
        if (map.size() >= 2){
            throw new RuntimeException("Give me one parameter");
        }
        String[] strings= getOneParam(map.entrySet().stream().findFirst().get());
        return new Query().addCriteria(Criteria.where(String.format("%s", strings[0])).regex(strings[1]));
    }
    public static Query getOneParamWithIs(Map<String,String> map){
        String[] strings= getOneParam(map.entrySet().stream().findFirst().get());
        return new Query().addCriteria(Criteria.where(String.format("%s", strings[0])).is(strings[1]));
    }
}
