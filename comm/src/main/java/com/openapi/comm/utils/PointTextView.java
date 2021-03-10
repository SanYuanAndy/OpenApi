package com.openapi.comm.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

public class PointTextView extends TextView {

    private Drawable mMarkDrawable = null;

    public PointTextView(Context context) {
        this(context, null);
    }

    public PointTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDotDrawable();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDot(canvas, mMarkDrawable);
    }

    private void initDotDrawable() {
        Drawable[] drawables = getCompoundDrawables();
        if (drawables == null) {
            return;
        }

        if (drawables.length < 4) {
            return;
        }

        final Drawable dr = mMarkDrawable = drawables[0];
        if (dr != null) {
            //设置空Drawable维持padding数值
            Drawable empty = new ShapeDrawable();
            empty.setAlpha(0);
            empty.setBounds(0, 0, dr.getMinimumWidth(), dr.getMinimumHeight());
            setCompoundDrawables(empty, drawables[1], drawables[2], drawables[3]);
        }
    }

    private void drawDot(Canvas canvas, Drawable dr) {
        if (dr == null) {
            return;
        }
        final int compoundPaddingTop = getCompoundPaddingTop();
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        int mPaddingLeft = getPaddingLeft();

        Layout layout = getLayout();
        int lineHeight = layout.getLineBottom(0) - layout.getLineTop(0);

        canvas.save();
        canvas.translate(scrollX + mPaddingLeft,
                    scrollY + compoundPaddingTop + (lineHeight - dr.getMinimumHeight()) / 2);
        dr.draw(canvas);
        canvas.restore();

    }

}
