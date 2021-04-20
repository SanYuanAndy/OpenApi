package com.openapi.mocklocation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

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
        keepAlive();
        if (intent != null) {
            VirtualLocationManager.getInstance().onReceive(this, (Intent) intent.getParcelableExtra("intent"));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void  keepAlive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "mock_location";
            NotificationChannel channel = new NotificationChannel(channelId, "mock_location", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            Notification notification = new Notification.Builder(this, channelId).build();
            startForeground(1, notification);
        }
    }

}
