package com.openapi.comm.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class JSONParser {
    public static <T> List<T> parse(Class<T> clazz, JSONArray jsonArray) {
        List<T> objList = new ArrayList<>();

        for(int i = 0; i < jsonArray.length(); ++i) {
            try {
                T obj = parse(clazz, jsonArray.getJSONObject(i));
                objList.add(obj);
            } catch (Exception e) {

            }
        }
        return objList;
    }

    public static <T> T parse(Class<T> clazz, JSONObject jsonObj) {
        T obj = null;
        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.set(obj, jsonObj.get(field.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return obj;
    }
}
