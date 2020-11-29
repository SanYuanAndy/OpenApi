package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.openapi.multitheme.MultiThemeSDK;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    int mIndex = 0;
    public void onThemeChange(View view) {
        mIndex++;
        mIndex = mIndex % 3;
        MultiThemeSDK.getInstance().refreshTheme(mIndex);
    }
}