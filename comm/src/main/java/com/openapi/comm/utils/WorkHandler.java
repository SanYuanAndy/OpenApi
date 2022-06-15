package com.openapi.comm.utils;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.widget.Toast;

public class WorkHandler {
    private static Handler mWorkHandler;
    private static Handler mUiHandler;
    private static Application sApp = null;

    public static void init(Application app) {
        sApp = app;

        HandlerThread thread = new HandlerThread("work");
        thread.start();
        mWorkHandler = new Handler(thread.getLooper());

        mUiHandler = new Handler(Looper.getMainLooper());
    }

    public static void runUiThread(Runnable runnable, long delayMills) {
        Handler handler = mUiHandler;
        if (handler != null) {
            handler.postDelayed(runnable, delayMills);
        }
    }

    public static void runBgThread(Runnable runnable, long delayMills) {
        Handler handler = mWorkHandler;
        if (handler != null) {
            handler.postDelayed(runnable, delayMills);
        }
    }

    public static void showToast(final String msg) {
        runUiThread(new Runnable() {
            @Override
            public void run() {
                Context cxt = sApp;
                if (cxt == null) {
                    return;
                }
                Toast.makeText(cxt, msg, Toast.LENGTH_LONG).show();
            }
        }, 0);

    }

}
