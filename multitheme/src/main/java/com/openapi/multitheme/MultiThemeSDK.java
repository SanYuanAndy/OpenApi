package com.openapi.multitheme;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.openapi.comm.utils.DefaultActivityLifeCallBack;


public class MultiThemeSDK {

    private static final String TAG = MultiThemeSDK.class.getSimpleName();

    private static MultiThemeSDK sInstance = new MultiThemeSDK();

    private MultiThemeSDK() {

    }

    public static MultiThemeSDK getInstance() {
        return sInstance;
    }

    public void initial(Application app, int themeIndex) {

        MultiTheme.setThemeIndex(themeIndex);

        LayoutInflater layoutInflater = (LayoutInflater) app.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.setFactory2(MultiTheme.LayoutInflaterFactory.create(layoutInflater));

       app.registerActivityLifecycleCallbacks(new DefaultActivityLifeCallBack() {
           @Override
           public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
               LayoutInflater layoutInflater = activity.getLayoutInflater();
               layoutInflater.setFactory2(MultiTheme.LayoutInflaterFactory.create(layoutInflater));
           }

           @Override
           public void onActivityDestroyed(@NonNull Activity activity) {
               MultiTheme.LayoutInflaterFactory.destroyFactory(activity);
           }

       });
    }

    public void refreshTheme(final int themeId) {
        MultiTheme.setThemeIndex(themeId);
    }

}
