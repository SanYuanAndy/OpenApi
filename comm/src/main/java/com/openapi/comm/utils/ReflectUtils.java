package com.openapi.comm.utils;

import android.util.Log;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static void replaceField(Object targetObj, String sFieldName, Object fieldValue) {
        Field field = getField(targetObj.getClass(), sFieldName);
        if (field != null) {
            field.setAccessible(true);
            try {
                field.set(targetObj, fieldValue);
            } catch (Exception e) {

            }
        }
    }

    private static Field getFiledInSingleClass(Class<?> clazz, String name){
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        }catch (Exception e){
            Log.d("ReflectUtils", "getField err : " + e.toString());
        }
        return field;
    }

    public static Field getField(Class<?> clazz, String name) {
        Field field = null;
        String className = clazz != null ? clazz.getSimpleName() : null;
        while (clazz != null) {
            field = getFiledInSingleClass(clazz, name);
            if (field != null){
                Log.d("ReflectUtils", field.toString());
                break;
            }
            clazz = clazz.getSuperclass();
        }
        return field;
    }

    public static Object getFieldValue(Object object, String fieldName) {
        Object value = null;
        try {
            Field field = getField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                value = field.get(object);
            }
        } catch (Exception e) {

        }
        return value;
    }

}
