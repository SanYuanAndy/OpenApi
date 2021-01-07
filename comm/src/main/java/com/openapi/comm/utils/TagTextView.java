package com.openapi.comm.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

public class TagTextView extends LinearLayout {

    public TagTextView(Context context) {
        this(context, null);
    }

    public TagTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);

        String sStyleableName = "com.android.internal.R$styleable";
        int[] index_group = (int[]) getFiled(sStyleableName, "TextView");
        int index_drawableLeft  = (int)getFiled(sStyleableName, "TextView_drawableLeft");
        int index_drawablePadding  = (int)getFiled(sStyleableName, "TextView_drawablePadding");
        int index_textSize  = (int)getFiled(sStyleableName, "TextView_textSize");
        TypedArray a = context.obtainStyledAttributes(attrs, index_group, defStyleAttr, 0);

        ImageView imageView = new ImageView(context);
        imageView.setMinimumHeight(a.getDimensionPixelSize(index_textSize, 0) + 10);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageDrawable(a.getDrawable(index_drawableLeft));

        TextView textView = new TextView(context, attrs, defStyleAttr);
        textView.setCompoundDrawables(null, null, null, null);
        textView.setPadding(a.getDimensionPixelSize(index_drawablePadding, 0), 0, 0, 0);
        addView(imageView);
        addView(textView);
    }

    public Object getFiled(String className, String sFiled) {
        Object value = null;
        try {
            Class<?> clazz = Class.forName(className);
            Field field = clazz.getField(sFiled);
            field.setAccessible(true);
            value = field.get(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }

}
