package com.openapi.mocklocation;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class DataManager {
    private static DataManager sInstance = new DataManager();
    private Context mContext = null;

    private DataManager() {

    }

    void init(Application context) {
        mContext = context;
    }

    public static DataManager getInstance() {
        return sInstance;
    }

    public void save(String key, String value) {
        if (mContext == null) {
            return;
        }
        SharedPreferences  sp = mContext.getSharedPreferences("latlng", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getValue(String key, String defValue) {
        if (mContext == null) {
            return null;
        }
        SharedPreferences  sp = mContext.getSharedPreferences("latlng", Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    public void showToast(String content) {
        Toast.makeText(mContext, content, Toast.LENGTH_SHORT);
    }

}
