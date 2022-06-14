package com.openapi.debugger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.openapi.comm.utils.ForegroundService;
import com.openapi.comm.utils.LogUtil;
import com.openapi.ipc.sdk.IPCProviderSDK;

public class DaemonService extends ForegroundService {
    public static final String TAG = DaemonService.class.getSimpleName();
    private View mFloatView = null;
    private WindowManager.LayoutParams mLayoutParams = null;
    private static final int MSG_LABEL_MAX_WIDTH = 600;
    private static final int MSG_LABEL_ALIVE_MAX_TIME_MILL = 60000;

    private static final int MSG_HANDLE_EVENT = 1;
    private static final int MSG_SHOW_MSG_LABEL = 10;
    private static final int MSG_DISMISS_MSG_LABEL = 11;

    private Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HANDLE_EVENT:
                    handleEvent(msg.arg1, (byte[]) msg.obj);
                    break;
                case MSG_SHOW_MSG_LABEL:
                    showMsgLabel((String) msg.obj);
                    break;
                case MSG_DISMISS_MSG_LABEL:
                    dismissMsgLabel();
                    break;
                default:
                    break;
            }
        }
    };

    private IBinder mBinder = new IFloatingManager.Stub() {
        @Override
        public void send(final int cmd, final byte[] data) throws RemoteException {
            Message msg = Message.obtain();
            msg.what = MSG_HANDLE_EVENT;
            msg.arg1 = cmd;
            msg.obj = data;
            mUiHandler.sendMessage(msg);
        }
    };

    private void handleEvent(int cmd, byte[] data) {
        switch (cmd) {
            case 0:
                String progress = new String(data);
                Message msg = Message.obtain();
                msg.what = MSG_SHOW_MSG_LABEL;
                msg.obj = progress;
                mUiHandler.sendMessage(msg);
                break;
        }
    }

    @Override
    public void sub_onCreate() {
        LogUtil.d(TAG, "sub_onCreate");
        IPCProviderSDK.getInstance().addService("FloatingManager", mBinder);
        showFloatingWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int opt = intent.getIntExtra("opt", -1);
            LogUtil.d(TAG, "opt:" + opt);
            if (opt == 1) {
                showFloatingWindow();
            } else if (opt == 0) {
                dismiss();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (!check()) {
            LogUtil.e(TAG, "NO PERMISSION");
            return;
        }

        if (mFloatView == null) {

            final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.width = getResources().getDimensionPixelSize(R.dimen.floatIconSize);
            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.floatIconSize);
            layoutParams.x = 0;
            layoutParams.y = 0;
            layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;

            View root = ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.floating, null);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.d(TAG, "onClick" );
                    launch();
                }
            });
            root.findViewById(R.id.tv_msg).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // done nothing for avoid drag
                }
            });

            windowManager.addView(root, layoutParams);
            mFloatView = root;
            mLayoutParams = layoutParams;

            mFloatView.setOnTouchListener(new View.OnTouchListener() {
                float mInitX = 0;
                int mCurrX = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            mInitX = event.getRawX();
                            mCurrX = mLayoutParams.x;
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            int offset = (int)(event.getRawX() - mInitX);
                            mLayoutParams.x = mCurrX + offset;
                            windowManager.updateViewLayout(mFloatView, mLayoutParams);
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            mInitX = 0;
                            break;
                    }
                    return false;
                }
            });
        }
    }

    private void dismiss() {
        View view = mFloatView;
        mFloatView = null;
        if (view != null) {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.removeView(view);
        }
    }

    private void launch() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {

        }
    }

    private boolean check() {
        boolean ret = true;
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                ret = false;
            }
        }
        return ret;
    }

    private void update(int width) {
        if (mFloatView == null) {
            return;
        }

        if (mLayoutParams == null) {
            return;
        }

        if (mLayoutParams.width == width) {
            return;
        }

        mLayoutParams.width = width;
        final WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.updateViewLayout(mFloatView, mLayoutParams);
    }

    private void showMsgLabel(String msg) {
        mUiHandler.removeMessages(MSG_DISMISS_MSG_LABEL);

        TextView tv = mFloatView.findViewById(R.id.tv_msg);
        tv.setText(msg);

        update(MSG_LABEL_MAX_WIDTH);

        mUiHandler.sendEmptyMessageDelayed(MSG_DISMISS_MSG_LABEL, MSG_LABEL_ALIVE_MAX_TIME_MILL);
    }

    private void dismissMsgLabel() {
        int iconSize = getResources().getDimensionPixelSize(R.dimen.floatIconSize);
        update(iconSize);
    }


    public static void start(Context context) {
        Intent i = new Intent(context, DaemonService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }

}
