package com.openapi.comm.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import java.util.List;

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
}
