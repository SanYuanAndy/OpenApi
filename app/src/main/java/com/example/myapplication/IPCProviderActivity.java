package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.openapi.comm.utils.LogUtil;
import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;
import com.openapi.ipc.ILocationManager;

public class IPCProviderActivity extends DebuggerActivity {
    private final static String TAG = IPCProviderActivity.class.getSimpleName();

    private final static Uri CallUri = Uri.parse("content://com.openapi.provider");

    @Override
    protected void init() {

        addAction(new ActionAdapter.Action("getService") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                extras.putString("service_name", "LocationManager");
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "getService", "", extras);
                IBinder binder = ret.getBinder("binder");
                ILocationManager locationManager = ILocationManager.Stub.asInterface(binder);
                try {
                    locationManager.start();
                } catch (Exception e) {

                }
                return false;
            }
        });

        addAction(new ActionAdapter.Action("打印日志") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                extras.putInt("level", Log.ERROR);
                extras.putString("tag", TAG);
                extras.putString("content", "日志打印接口调试");
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "print", "", extras);
                LogUtil.d(TAG, "ret:" + ret.getBoolean("ret"));
                return false;
            }
        });
    }

    public static String getDebugLabel() {
        return TAG;
    }
}
