package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.openapi.comm.utils.LogUtil;
import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;
import com.openapi.ipc.ILocationManager;

public class IPCProviderRemoteActivity extends DebuggerActivity {
    private final static String TAG = IPCProviderRemoteActivity.class.getSimpleName();
    private final static Uri CallUri = Uri.parse("content://com.openapi.provider");

    @Override
    protected void init() {
        addAction(new ActionAdapter.Action("addService") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                extras.putString("service_name", "LocationManager");
                extras.putBinder("binder", new ILocationManager.Stub() {
                    @Override
                    public void start() throws RemoteException {
                        LogUtil.d(TAG, "start call :" + getCallingPid());
                    }

                    @Override
                    public double[] getLastLocation() throws RemoteException {
                        return new double[0];
                    }
                });
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "addService", "", extras);
                LogUtil.d(TAG, "ret:" + ret.getBoolean("ret"));
                return false;
            }
        });

    }

    public static String getDebugLabel() {
        return IPCProviderRemoteActivity.class.getSimpleName();
    }
}
