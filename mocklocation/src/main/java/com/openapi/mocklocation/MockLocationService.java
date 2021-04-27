package com.openapi.mocklocation;

import android.content.Intent;
import com.openapi.comm.utils.ForegroundService;

public class MockLocationService extends ForegroundService {

    public void sub_onCreate() {
        VirtualLocationManager.getInstance().init(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            VirtualLocationManager.getInstance().onReceive(this, (Intent) intent.getParcelableExtra("intent"));
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
