package dev.hakan.movies.util;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Map;

public class QueryUtil {

    public static Query getAllParamWithLike(Map<String,String> map){
        String key;
        String value;
        Query query = new Query();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if (value.startsWith("[") && value.endsWith("]")) {
                String[] values = value.substring(1, value.length() - 1).split(",");
                query.addCriteria(Criteria.where(key).in(values));
            }else {
                query.addCriteria(Criteria.where(String.format("%s", key)).regex(value));
            }
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
            if (value.startsWith("[") && value.endsWith("]")) {
                String[] values = value.substring(1, value.length() - 1).split(",");
                query.addCriteria(Criteria.where(key).in(values));
            }else {
                query.addCriteria(Criteria.where(String.format("%s", key)).is(value));
            }

        }
        return query;
    }
}
