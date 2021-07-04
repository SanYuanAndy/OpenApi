package com.openapi.comm.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import java.lang.reflect.Field;

public class MarqueeTextView extends TextView {
    private static final String TAG = MarqueeTextView.class.getSimpleName();
    private static final float DEFAULT_MARQUEE_SPEED_LEVEL = 1.0f;
    private float mMarqueeSpeedLevel = DEFAULT_MARQUEE_SPEED_LEVEL;
    private float mSetSpeedLevel = DEFAULT_MARQUEE_SPEED_LEVEL;

    private Field mMarqueeObjField = null;
    private Field mSpeedObjField = null;

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateMarqueeSpeed(mSetSpeedLevel);
    }

    private void init() {
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setHorizontallyScrolling(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

        try {
            mMarqueeObjField = TextView.class.getDeclaredField("mMarquee");
            mMarqueeObjField.setAccessible(true);

            Class<?> clazz = Class.forName("android.widget.TextView$Marquee");
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                mSpeedObjField = clazz.getDeclaredField("mPixelsPerSecond");
            } else {
                mSpeedObjField = clazz.getDeclaredField("mPixelsPerMs");
            }
            mSpeedObjField.setAccessible(true);

        } catch (Exception e) {
            LogUtil.e(TAG, "no support set marquee speed");
            e.printStackTrace();
        }
    }

    private void updateMarqueeSpeed(float speedLevel) {
        if (mSpeedObjField == null) {
            return;
        }

        if (mMarqueeSpeedLevel == speedLevel) {
            return;
        }

        try {
            Object marquee = mMarqueeObjField.get(this);
            if (marquee == null) {
                return;
            }

            Float old = mSpeedObjField.getFloat(marquee);
            LogUtil.e(TAG, "old :" + old);
            if (old != null) {
                mSpeedObjField.set(marquee, old * speedLevel);
            }
            mMarqueeSpeedLevel = speedLevel;
        } catch (Exception e) {

        }
    }

    public void setSpeedLevel(float level) {
        mSetSpeedLevel = level;
    }

}
