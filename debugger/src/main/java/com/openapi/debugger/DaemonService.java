package com.openapi.debugger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.openapi.comm.utils.ForegroundService;
import com.openapi.comm.utils.LogUtil;

public class DaemonService extends ForegroundService {
    public static final String TAG = DaemonService.class.getSimpleName();
    private View mFloatView = null;
    private WindowManager.LayoutParams mLayoutParams = null;

    @Override
    public void sub_onCreate() {
        LogUtil.d(TAG, "sub_onCreate");
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

            ImageView iv = new ImageView(this);
            iv.setImageResource(R.mipmap.ic_show);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.d(TAG, "onClick" );
                    launch();
                }
            });
            windowManager.addView(iv, layoutParams);
            mFloatView = iv;
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



}
