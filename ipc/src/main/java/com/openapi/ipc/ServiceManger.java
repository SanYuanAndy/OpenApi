package com.openapi.ipc;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public class ServiceManger {

    private static ServiceManger sInstance = new ServiceManger();

    private Map<String, IBinder> mServiceMap = null;

    private ServiceManger() {
        mServiceMap = new HashMap<>();
    }

    public static ServiceManger getsInstance() {
        return sInstance;
    }

    public boolean registerService(String serviceName, IBinder binder) {
        mServiceMap.put(serviceName, binder);
        return true;
    }

    public IBinder getService(String serviceName) {
        return mServiceMap.get(serviceName);
    }

}
