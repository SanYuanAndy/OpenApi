package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;

import com.openapi.comm.utils.LogUtil;
import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;

public class IPCProviderActivity extends DebuggerActivity {
    private final static String TAG = IPCProviderActivity.class.getSimpleName();

    private final static Uri CallUri = Uri.parse("content://com.openapi.provider");

    @Override
    protected void init() {
        addAction(new ActionAdapter.Action("getService") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                extras.putString("service_name", "IPCService");
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "getService", "", extras);
                LogUtil.d(TAG, "ret:" + ret.getBoolean("ret"));
                return false;
            }
        });

        addAction(new ActionAdapter.Action("setLevel") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                extras.putInt("level", 0);
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "setLevel", "", extras);
                LogUtil.d(TAG, "ret:" + ret.getBoolean("ret"));
                return false;
            }
        });
    }

    public static String getDebugLabel() {
        return TAG;
    }
}
