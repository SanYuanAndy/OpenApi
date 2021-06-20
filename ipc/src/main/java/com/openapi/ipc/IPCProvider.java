package com.openapi.ipc;

import android.os.Bundle;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openapi.comm.utils.LogUtil;

import java.io.OutputStream;

public class IPCProvider extends EmptyContentProvider {
    private static final String TAG = IPCProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        CommandManager.getInstance().regMethod("getService", new ICommand() {
            @Override
            public Bundle invoke(String arg, Bundle extras) {
                String strServiceName = extras.getString("service_name");
                Bundle b = new Bundle();
                b.putBinder("binder", ServiceManger.getsInstance().getService(strServiceName));
                b.putBoolean("ret", true);
                return b;
            }
        });

        CommandManager.getInstance().regMethod("addService", new ICommand() {
            @Override
            public Bundle invoke(String arg, Bundle extras) {
                String strServiceName = extras.getString("service_name");
                IBinder binder = extras.getBinder("binder");
                Bundle b = new Bundle();
                b.putBoolean("ret", ServiceManger.getsInstance().registerService(strServiceName, binder));
                return b;
            }
        });

        CommandManager.getInstance().regMethod("print", new ICommand() {
            @Override
            public Bundle invoke(String arg, Bundle extras) {
                int level = extras.getInt("level", Log.VERBOSE);
                String tag = extras.getString("tag", "");
                String content = extras.getString("content", "");
                Log.println(level, tag, content);
                Bundle b = new Bundle();
                b.putBoolean("ret", true);
                return b;
            }
        });

        CommandManager.getInstance().regMethod("getSharedMemory", new ICommand() {
            MemoryFile mMemoryFile = null;
            OutputStream out = null;
            ParcelFileDescriptor parcelFileDescriptor = null;
            int count = 0;
            @Override
            public Bundle invoke(String arg, Bundle extras) {
                Bundle b = new Bundle();
                if (mMemoryFile == null) {
                    synchronized (this) {
                        if (mMemoryFile == null) {
                            mMemoryFile = SharedMemoryUtils.create();
                            out = mMemoryFile.getOutputStream();
                            parcelFileDescriptor = SharedMemoryUtils.getParcelFileDescriptor(mMemoryFile);
                            LogUtil.d(TAG, "fd:" + SharedMemoryUtils.getFileDescriptor(mMemoryFile));
                        }
                    }
                }
                byte[] data = ("共享内存测试初始数据" + count++).getBytes();
                SharedMemoryUtils.write(out, data, 0, data.length);
                b.putParcelable("memory", parcelFileDescriptor);
                return b;
            }
        });


        return true;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        LogUtil.d(TAG, "callingPackage:" + getCallingPackage() + ",method : " + method + ", " + extras);
        return CommandManager.getInstance().invoke(method, arg, extras);
    }
}
