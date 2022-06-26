package com.openapi.alldemo;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

import com.openapi.comm.utils.WorkHandler;
import com.openapi.debugger.DaemonService;
import com.openapi.ipc.sdk.IPCProviderSDK;
import com.openapi.multitheme.MultiTheme;
import com.openapi.multitheme.MultiThemeSDK;

//import com.openapi.utils.MarkLayoutInflater;

public class BaseApplication extends Application {
    private static Context sContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        WorkHandler.init(this);
        MultiThemeSDK.getInstance().initial(this, 0);
        IPCProviderSDK.getInstance().init(this);
        DaemonService.start(this);
    }

    public static Context getApp(){
        return sContext;
    }

}