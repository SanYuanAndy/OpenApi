package com.openapi.ipc;

import android.os.Binder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openapi.comm.utils.LogUtil;

public class IPCProvider extends EmptyContentProvider {
    private static final String TAG = IPCProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        CommandManager.getInstance().regMethod("getService", new ICommand() {
            @Override
            public Bundle invoke(String arg, Bundle extras) {
                String strServiceName = extras.getString("service_name");
                Bundle b = new Bundle();
                b.putBinder("binder", new Binder());
                return b;
            }
        });

        CommandManager.getInstance().regMethod("setLevel", new ICommand() {
            @Override
            public Bundle invoke(String arg, Bundle extras) {
                int level = extras.getInt("level", -1);
                Bundle b = new Bundle();
                b.putBoolean("ret", level > 0);
                return b;
            }
        });


        return true;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        LogUtil.d(TAG, "callingPackage:" + getCallingPackage() + ",method : " + method + ", " + extras);
        return CommandManager.getInstance().invoke(method, arg, extras);
    }
}
