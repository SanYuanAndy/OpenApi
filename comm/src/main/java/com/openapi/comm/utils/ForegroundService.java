package com.openapi.comm.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public abstract class ForegroundService extends Service {

    @Override
    final public void onCreate() {
        super.onCreate();
        keepAlive();
        sub_onCreate();
    }

    public abstract void sub_onCreate();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void  keepAlive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getOpPackageName();
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            Notification notification = new Notification.Builder(this, channelId).build();
            startForeground(1, notification);
        }
    }

}
