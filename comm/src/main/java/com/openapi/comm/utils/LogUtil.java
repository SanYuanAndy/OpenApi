package com.openapi.comm.utils;

import android.util.Log;

public class LogUtil {
    private static int sLevel = Log.VERBOSE;

    public static void v(String tag, String content) {
        print(Log.VERBOSE, tag, content);
    }

    public static void d(String tag, String content) {
        print(Log.DEBUG, tag, content);
    }

    public static void i(String tag, String content) {
        print(Log.INFO, tag, content);
    }

    public static void w(String tag, String content) {
        print(Log.WARN, tag, content);
    }

    public static void e(String tag, String content) {
        print(Log.ERROR, tag, content);
    }

    private static void print(int level, String tag, String content) {
        if (level >= sLevel) {
            Log.println(level, tag, content);
        }
    }

    public static void setLevel(int level) {
        sLevel = level;
    }

}
