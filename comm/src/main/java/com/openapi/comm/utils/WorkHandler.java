package com.openapi.comm.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class WorkHandler {
    private static Handler mWorkHandler;
    private static Handler mUiHandler;

    public static void init() {
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

}
