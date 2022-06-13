package com.openapi.debugger;

import android.os.IBinder;

import com.openapi.ipc.sdk.IPCProviderSDK;

public class UIManager {
    private static UIManager sManager = new UIManager();
    private IFloatingManager mFloatingManager = null;

    private UIManager() {
        IBinder binder = IPCProviderSDK.getInstance().getService("FloatingManager");
        mFloatingManager = IFloatingManager.Stub.asInterface(binder);
    }

    public static UIManager getInstance() {
        return sManager;
    }

    public void sendFloatingText(String data) {
        if (mFloatingManager == null) {
            return;
        }

        try {
            mFloatingManager.send(0, data.getBytes());
        } catch (Exception e) {

        }
    }

}
