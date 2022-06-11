package com.openapi.ipc.sdk;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import com.openapi.ipc.ILocationManager;
import com.openapi.ipc.R;

public class IPCProviderSDK {
    public static IPCProviderSDK sInstance = new IPCProviderSDK();
    private Application mApp = null;
    private Uri mCallUri = null;

    private IPCProviderSDK() {

    }

    public static IPCProviderSDK getInstance() {
        return sInstance;
    }

    public void init(Application application) {
        mApp = application;
        mCallUri = Uri.parse(String.format("content://%s", mApp.getString(R.string.ipc_provider_authorities)));
    }

    public IBinder getService(String serviceName) {
        Bundle extras = new Bundle();
        extras.putString("service_name", serviceName);
        Bundle ret = mApp.getContentResolver().call(mCallUri, "getService", "", extras);
        IBinder binder = ret.getBinder("binder");
        return binder;
    }

    public void addService(String serviceName, IBinder binder) {
        Bundle extras = new Bundle();
        extras.putString("service_name", serviceName);
        extras.putBinder("binder", binder);
        Bundle ret = mApp.getContentResolver().call(mCallUri, "addService", "", extras);
        ret.getBoolean("ret", false);
    }

}

