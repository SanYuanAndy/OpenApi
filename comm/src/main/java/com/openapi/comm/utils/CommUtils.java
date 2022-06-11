package com.openapi.comm.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;

public class CommUtils {

    private static final String TAG = CommUtils.class.getSimpleName();

    public static void notifyStaticReceiver(Context context, String action) {
        try {
            notifyAllStaticReceiverInner(context, action);
        } catch (Exception e) {

        }
    }

    private static void notifyAllStaticReceiverInner(Context context, String action) {
        if (context == null) {
            return;
        }

        if (TextUtils.isEmpty(action)) {
            return;
        }

        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return;
        }

        List<ResolveInfo> list = pm.queryBroadcastReceivers(new Intent(action), 0);

        if (list == null) {
            return;
        }


        for (ResolveInfo info : list) {
            String strPkgName = info.activityInfo.applicationInfo.packageName;
            String strName = info.activityInfo.name;
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setComponent(new ComponentName(strPkgName, strName));
            context.sendBroadcast(intent);
            LogUtil.e(TAG, "notify : " + strPkgName + "," + strName);
        }
    }

    public static byte[] int2Byte(int value) {
        byte[] ret = new byte[4];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = (byte)(0x00ff & (value >> (i * 8)));
        }
        return ret;
    }

    public static int byte2Int(byte[] value) {
        if (value == null || value.length != 4) {
            return 0;
        }

        int ret = 0;
        for (int i = 0; i < value.length; ++i) {
            ret += value[i] << ( i* 8);
        }
        return ret;
    }


    public static <T> void println(List<T> data) {
        for (T t : data) {
            LogUtil.e(TAG, "" + t);
        }
    }

    public static String toString(Object obj) {
        StringBuilder sb = new StringBuilder();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        sb.append(clazz.getSimpleName() + "{");
        for (Field field : fields) {
            try {
                Object value = field.get(obj);
                sb.append(field.getName() + "=" + value + ",");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sb.append("}");

        return sb.toString();
    }

    public static boolean isValidDns(String dns) {
        boolean ret = false;
        do {
            if (TextUtils.isEmpty(dns)) {
                break;
            }

            if (dns.startsWith("*")) {
                break;
            }

            ret = true;
        } while (false);
        return ret;
    }

    public static boolean isIp(String dns) {
        boolean ret = false;
        return ret;
    }
}
