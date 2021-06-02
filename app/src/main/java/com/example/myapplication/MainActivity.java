package com.example.myapplication;

import android.app.Activity;
import android.app.Dialog;
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
        onThemeChange();
    }

    public void onBack(View view) {
        finish();
    }

    public void onShowDialog(View view) {
        showDialog();
    }

    public void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_main);
        dialog.show();
    }

    private void onThemeChange() {
        mIndex++;
        mIndex = mIndex % 3;
        MultiThemeSDK.getInstance().refreshTheme(mIndex);
    }

    public static String getDebugLabel() {
        return "MultiTheme";
    }

}