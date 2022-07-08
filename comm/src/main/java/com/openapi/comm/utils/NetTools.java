package com.openapi.comm.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class NetTools {
    private static final String TAG = NetTools.class.getSimpleName();
    private static NetTools sInstance = new NetTools();
    private BroadcastReceiver mReceiver = null;
    private List<INetListener> mListeners = new ArrayList<>();

    public static NetTools getInstance() {
        return sInstance;
    }

    public static class NetInfo {
        public boolean isConnected;
        public int type;
    }

    public static interface INetListener {
        void onChanged(boolean isConnected, int type);
    }

    public static boolean isConnected(Context context) {
        return getNetInfo(context).isConnected;
    }

    private static NetInfo getNetInfo(Context context) {
        NetInfo netInfo = new NetInfo();

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                netInfo.isConnected = true;
                netInfo.type = info.getType();
            }
        }

        return netInfo;
    }

    private synchronized void onChanged(boolean isConnected, int type) {
        for (INetListener listener : mListeners) {
            if (listener == null) {
                continue;
            }
            listener.onChanged(isConnected, type);
        }
    }


    public synchronized void init(Context context) {
        if (context == null) {
            return;
        }

        if (mReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.e(TAG, "action:" + intent.getAction());
                NetInfo info = getNetInfo(context);
                onChanged(info.isConnected, info.type);
            }
        };
        context.getApplicationContext().registerReceiver(mReceiver, filter);
    }

    public synchronized void addListener(INetListener listener) {
        if (listener == null) {
            return;
        }

        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public synchronized void rmListener(INetListener listener) {
        if (listener == null) {
            return;
        }
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }
}
