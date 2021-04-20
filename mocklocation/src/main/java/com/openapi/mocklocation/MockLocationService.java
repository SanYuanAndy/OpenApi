package com.openapi.mocklocation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MockLocationService extends Service {
    public MockLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        VirtualLocationManager.getInstance().init(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VirtualLocationManager.getInstance().onReceive(this, (Intent)intent.getParcelableExtra("intent"));
        return super.onStartCommand(intent, flags, startId);
    }
}
