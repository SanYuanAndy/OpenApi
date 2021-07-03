package com.example.myapplication;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.SharedMemory;
import android.util.Log;
import android.widget.Toast;

import com.openapi.comm.utils.LogUtil;
import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;
import com.openapi.ipc.ILocationManager;
import com.openapi.ipc.SharedMemoryUtils;
import com.openapi.ipc.SharedMemoryWrapper;

public class IPCProviderActivity extends DebuggerActivity {
    private final static String TAG = IPCProviderActivity.class.getSimpleName();

    private final static Uri CallUri = Uri.parse("content://com.openapi.provider");

    @Override
    protected void init() {

        addAction(new ActionAdapter.Action("getService") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                extras.putString("service_name", "LocationManager");
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "getService", "", extras);
                IBinder binder = ret.getBinder("binder");
                ILocationManager locationManager = ILocationManager.Stub.asInterface(binder);
                try {
                    locationManager.start();
                } catch (Exception e) {

                }
                return false;
            }
        });

        addAction(new ActionAdapter.Action("打印日志") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                extras.putInt("level", Log.ERROR);
                extras.putString("tag", TAG);
                extras.putString("content", "日志打印接口调试");
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "print", "", extras);
                LogUtil.d(TAG, "ret:" + ret.getBoolean("ret"));
                return false;
            }
        });

        addAction(new ActionAdapter.Action("读共享内存") {
            @Override
            public boolean invoke() {
                Bundle extras = new Bundle();
                Bundle ret = getBaseContext().getContentResolver().call(CallUri, "getSharedMemory", "", extras);
                ParcelFileDescriptor parcelFileDescriptor = ret.getParcelable("memory");
                byte[] data = SharedMemoryUtils.read(parcelFileDescriptor.getFileDescriptor());
                String text = data == null ? null : new String(data);
                LogUtil.d(TAG, "data:" +  text);
                Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("读共享内存-advance") {
            @Override
            public boolean invoke() {
                SharedMemoryWrapper wrapper = getAdvanceSharedMemory();
                readSharedMemory(wrapper);
                wrapper.close();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("读共享内存-advance-persistent") {
            SharedMemoryWrapper mWrapper = null;
            @Override
            public boolean invoke() {
                if (mWrapper == null) {
                    mWrapper = getAdvanceSharedMemory();
                }
                readSharedMemory(mWrapper);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("注册Observer") {
            @Override
            public boolean invoke() {
                ContentResolver resolver = getContentResolver();
                Uri uri = Uri.parse("content://com.openapi.provider/status");
                resolver.registerContentObserver(uri, false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        LogUtil.d(TAG, "onChange:" + selfChange);
                    }
                });
                return false;
            }
        });
    }

    public static String getDebugLabel() {
        return TAG;
    }

    public SharedMemoryWrapper getAdvanceSharedMemory() {
        Bundle extras = new Bundle();
        Bundle ret = getBaseContext().getContentResolver().call(CallUri, "getSharedMemory-advance", "", extras);
        SharedMemory sharedMemory = ret.getParcelable("memory");
        SharedMemoryWrapper wrapper = SharedMemoryWrapper.create(sharedMemory);
        return wrapper;
    }

    private void readSharedMemory(SharedMemoryWrapper wrapper) {
        if (wrapper != null) {
            byte[] data = wrapper.read();
            String text = data == null ? null : new String(data);
            LogUtil.d(TAG, "data:" +  text);
            Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

}
