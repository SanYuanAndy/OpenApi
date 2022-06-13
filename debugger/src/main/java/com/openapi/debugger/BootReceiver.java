package com.openapi.debugger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.openapi.comm.utils.LogUtil;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.d(BootReceiver.class.getSimpleName(), "receive:" + intent.getAction());
        DaemonService.start(context);
    }
}
