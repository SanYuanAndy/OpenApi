package com.openapi.multitheme;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openapi.comm.utils.DefaultActivityLifeCallBack;
import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.WorkHandler;

public class MultiThemeSDK {

    private static final String TAG = MultiThemeSDK.class.getSimpleName();

    private static MultiThemeSDK sInstance = new MultiThemeSDK();

    private MultiThemeSDK() {

    }

    public static MultiThemeSDK getInstance() {
        return sInstance;
    }

    public void initial(Application app) {
        LayoutInflater layoutInflater = (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.setFactory2(MultiTheme.LayoutInflaterFactory.create(layoutInflater));

       app.registerActivityLifecycleCallbacks(new DefaultActivityLifeCallBack() {
           @Override
           public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
               LayoutInflater layoutInflater = activity.getLayoutInflater();
               layoutInflater.setFactory2(MultiTheme.LayoutInflaterFactory.create(layoutInflater));
           }

       });
    }

    public void refreshTheme(final int themeId) {
        MultiTheme.setThemeIndex(themeId);
    }

}
