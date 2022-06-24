package com.openapi.comm.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class JSONParser {
    public static final String TAG = "JSONParser";

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
            LogUtil.e(TAG, "" + e);
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.set(obj, jsonObj.get(field.getName()));
            } catch (Exception e) {
                LogUtil.e(TAG, "" + e);
            }
        }

        return obj;
    }

    public static <T> T parseDeep(Class<T> clazz, JSONObject jsonObj) {
        T obj = null;
        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            LogUtil.e(TAG, "" + e);
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                Object val = deep(field, jsonObj.get(field.getName()));
                field.setAccessible(true);
                field.set(obj, val);
            } catch (Exception e) {
                LogUtil.e(TAG, "" + e);
            }
        }

        return obj;
    }

    public static <T> List<T> parseDeep(Class<T> clazz, JSONArray jsonArray) {
        List<T> objList = new ArrayList<>();

        for(int i = 0; i < jsonArray.length(); ++i) {
            try {
                Object value = jsonArray.get(i);
                T obj = null;
                if (value instanceof JSONObject) {
                    obj = parseDeep(clazz, (JSONObject) value);
                } else if (value instanceof JSONArray) {
                    // todo
                } else {
                    obj = (T) value;
                }
                objList.add(obj);
            } catch (Exception e) {

            }
        }
        return objList;
    }

    public static <T> T parseDeep(Class<T> clazz, String strJson) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(strJson);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject = new JSONObject();
        }

        return parseDeep(clazz, jsonObject);
    }

    public static <T> List<T> parseDeepArray(Class<T> clazz, String strJson) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(strJson);
        } catch (Exception e) {
            e.printStackTrace();
            jsonArray = new JSONArray();
        }

        return parseDeep(clazz, jsonArray);
    }

    private static Object deep(Field field, Object val) {
        if (val instanceof JSONObject) {
            return parseDeep(field.getType(), (JSONObject) val);
        } else if (val instanceof JSONArray) {
            return parseDeep(field.getType().getComponentType(),(JSONArray) val);
        } else {
            return val;
        }
    }
}
