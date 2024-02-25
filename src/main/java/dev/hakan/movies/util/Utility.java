package dev.hakan.movies.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Utility {

    public static String[] getOneParam(Map.Entry<String,String> entry){
        String[] strings = new String[2];
        strings[0] = entry.getKey();
        strings[1] = entry.getValue();
        return strings;
    }
    public static Map<String, String> getDeclaredFieldFromObject(Object obj) {
        Map<String, String> map = new HashMap<>();

        // Sınıfın tüm alanlarını döngüyle geç
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true); // Erişilebilirliği aç
            try {
                Object value = field.get(obj); // Alanın değerini al

                // Eğer alanın değeri null değilse, map'e ekle
                if (value != null) {
                    map.put(field.getName(), value.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }




}
