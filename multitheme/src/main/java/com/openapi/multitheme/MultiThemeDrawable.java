/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openapi.multitheme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.graphics.drawable.TintAwareDrawable;

import com.openapi.comm.utils.LogUtil;

import java.util.HashMap;
import java.util.Map;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

/**
 * Drawable which delegates all calls to its wrapped {@link Drawable}.
 * <p>
 * The wrapped {@link Drawable} <em>must</em> be fully released from any {@link View}
 * before wrapping, otherwise internal {@link Callback} may be dropped.
 */
public class MultiThemeDrawable extends Drawable implements Drawable.Callback {

    @Override
    public void draw(Canvas canvas) {
        getDrawable().draw(canvas);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        getDrawable().setBounds(bounds);
    }

    @Override
    public void setChangingConfigurations(int configs) {
        getDrawable().setChangingConfigurations(configs);
    }

    @Override
    public int getChangingConfigurations() {
        return getDrawable().getChangingConfigurations();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setDither(boolean dither) {
        getDrawable().setDither(dither);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        getDrawable().setFilterBitmap(filter);
    }

    @Override
    public void setAlpha(int alpha) {
        getDrawable().setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        getDrawable().setColorFilter(cf);
    }

    @Override
    public boolean isStateful() {
        return getDrawable().isStateful() || true;
    }

    @Override
    public boolean setState(final int[] stateSet) {
        return getDrawable().setState(stateSet) || true;
    }

    @Override
    public int[] getState() {
        return getDrawable().getState();
    }

    @Override
    public void jumpToCurrentState() {
        getDrawable().jumpToCurrentState();
    }

    @Override
    public Drawable getCurrent() {
        return getDrawable().getCurrent();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart) || getDrawable().setVisible(visible, restart);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getOpacity() {
        return getDrawable().getOpacity();
    }

    @Override
    public Region getTransparentRegion() {
        return getDrawable().getTransparentRegion();
    }

    @Override
    public int getIntrinsicWidth() {
        return getDrawable().getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return getDrawable().getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return getDrawable().getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return getDrawable().getMinimumHeight();
    }

    @Override
    public boolean getPadding(Rect padding) {
        return getDrawable().getPadding(padding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return getDrawable().setLevel(level);
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        getDrawable().setAutoMirrored(mirrored);
    }

    @Override
    public boolean isAutoMirrored() {
        return getDrawable().isAutoMirrored();
    }

    @Override
    public void setTint(int tint) {
        if (Build.VERSION.SDK_INT >= 21) {
            getDrawable().setTint(tint);
        }
    }

    @Override
    public void setTintList(ColorStateList tint) {
        if (Build.VERSION.SDK_INT >= 21) {
            getDrawable().setTintList(tint);
        }
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        if (Build.VERSION.SDK_INT >= 21) {
            getDrawable().setTintMode(tintMode);
        }
    }

    @Override
    public void setHotspot(float x, float y) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        getDrawable().setHotspot(x, y);
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        getDrawable().setHotspotBounds(left, top, right, bottom);
    }

    public void setWrappedDrawable(Drawable drawable, Context context) {

    }

    private Context mContext = null;
    private Resources mResources = null;
    private int mCurrThemeIndex = -1;
    private String mResName;
    private String mResType;

    private Map<Integer, Drawable> map = new HashMap<>();

    private Drawable mDrawable;

    public MultiThemeDrawable(Context context, String resEntryName, String resType) {
        mResName = resEntryName;
        mResType = resType;
        mContext = context;
        mResources = context.getResources();
        setWrappedDrawable(null, context);
    }

    private Drawable getDrawable() {
//        Exception e = new Exception();
//        e.printStackTrace();

        int index = MultiTheme.getThemeIndex();
        if (index != mCurrThemeIndex) {
            mCurrThemeIndex = index;
            String strIndex = "";
            if (mCurrThemeIndex > 0) {
                strIndex = "_" + mCurrThemeIndex;
            }
            int resId = mResources.getIdentifier(mResName + strIndex, mResType, mContext.getPackageName());
            if (resId == 0) {
                resId = mResources.getIdentifier(mResName, mResType, mContext.getPackageName());
            }

            Drawable dr = mResources.getDrawable(resId);
            if (mDrawable != null) {
                dr.setCallback(this);
                dr.setVisible(mDrawable.isVisible(), true);
                dr.setState(mDrawable.getState());
                dr.setLevel(mDrawable.getLevel());
                dr.setBounds(mDrawable.getBounds());
            }
            mDrawable = dr;
            LogUtil.d("Drawable", mDrawable.getClass().toString());
        }

        return mDrawable;
    }

}
