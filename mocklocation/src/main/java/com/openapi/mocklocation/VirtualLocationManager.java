package com.openapi.mocklocation;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class VirtualLocationManager {
    private static final String TAG = VirtualLocationManager.class.getSimpleName();
    private static VirtualLocationManager sInstance = new VirtualLocationManager();

    private static final String PROVIDER_NAME = LocationManager.GPS_PROVIDER;
    private double mLat = 39.9635575629816;
    private double mLng = 116.32850354186871;
    private static final double LAT= 39.9635575629816;
    private static final double LNG = 116.32850354186871;
    private Context mContext = null;
    private int mCount = -1;
    private static final int COUNT_MAX = 3000;

    private VirtualLocationManager() {

    }

    public static VirtualLocationManager getInstance() {
        return sInstance;
    }


    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive:" + intent);
        int opt = intent.getIntExtra("opt", 0);
        if (opt == 1) {
            String lat = intent.getStringExtra("lat");
            String lon = intent.getStringExtra("lng");
            double[] latlng = parseLatLng(lat, lon);
            if (latlng == null) {
                return;
            }
            DataManager.getInstance().save("lat", lat);
            DataManager.getInstance().save("lng", lon);
            mLat = latlng[0];
            mLng = latlng[1];
            start(mContext);
        } else if (opt == 2){
            DataManager.getInstance().save("switch", "open");
        } else if (opt == 0){
            DataManager.getInstance().save("switch", "");
        } else if (opt == 3) {
            mCount = COUNT_MAX + 1000;
        }
    }

    public void init(Context context) {
        Log.e(TAG, "init");
        mContext = context;
        DataManager.getInstance().init((Application) context.getApplicationContext());
        String switchKey = DataManager.getInstance().getValue("switch", "close");
        if ("open".equals(switchKey)) {

            String lat = DataManager.getInstance().getValue("lat", "" + LAT);
            String lng = DataManager.getInstance().getValue("lng", "" + LNG);

            double[] latlng = parseLatLng(lat, lng);
            if (latlng == null) {
                return;
            }
            mLat = latlng[0];
            mLng = latlng[1];
            start(mContext);
        }
    }

    private double[] parseLatLng(String sLat, String sLng) {
        double[] latlng = null;
        do {
            if (TextUtils.isEmpty(sLat)) {
                break;
            }

            if (TextUtils.isEmpty(sLng)) {
                break;
            }

            double lat = 0.0;
            try {
                lat = Double.parseDouble(sLat);
            } catch (Exception e) {
                break;
            }

            double lng = 0.0;
            try {
                lng = Double.parseDouble(sLng);
            } catch (Exception e) {
                break;
            }

            latlng = new double[2];
            latlng[0] = lat;
            latlng[1] = lng;

        } while (false);

        return latlng;
    }

    private void start(Context context) {
        mLat = mLat - 0.005;
        run(context);
    }

    private void run(Context context) {
        if (mCount != -1) {
            return;
        }

        mCount = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mock();
                } catch (Exception e) {
                    e.printStackTrace();
                    DataManager.getInstance().showToast("没有模拟定位的权限，请先打开该权限");
                }
                mCount = -1;
            }

        }).start();
    }

    private void mock() {
        Log.d(TAG, "start mock location");

        LocationManager locationManager;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider(PROVIDER_NAME, true, false, false, false, true, true, true, Criteria.POWER_HIGH, Criteria.ACCURACY_FINE);
        locationManager.setTestProviderEnabled(PROVIDER_NAME, true);

        for (; mCount < 2000; mCount++) {
            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
            newLocation.setLatitude(mLat);
            newLocation.setLongitude(mLng);
            newLocation.setAccuracy(30);
            newLocation.setTime(System.currentTimeMillis());
            newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            newLocation.setSpeed(5);
            locationManager.setTestProviderLocation(PROVIDER_NAME, newLocation);

            mLat += 0.001;
            try {
                Thread.sleep(3000);
            } catch (Exception e) {

            }

            if (mCount >> 3 == 0) {
                Log.d(TAG, "mock location...");
            }
        }

        locationManager.setTestProviderEnabled(PROVIDER_NAME, false);
        locationManager.removeTestProvider(PROVIDER_NAME);
        mCount = 0;
        Log.d(TAG, "stop mock location");
    }

}
