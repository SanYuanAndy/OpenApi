package com.openapi.multitheme;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openapi.comm.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiTheme {

    private static final String TAG = MultiThemeResource.class.getSimpleName();
    private static int sThemeIndex = 0;

    public static class MultiThemeResource extends Resources {
        public MultiThemeResource(AssetManager assets, DisplayMetrics metrics, Configuration config) {
            super(assets, metrics, config);
        }

        @NonNull
        @Override
        public XmlResourceParser getLayout(int id) throws NotFoundException {
            XmlResourceParser parser = super.getLayout(id);
            LogUtil.d(TAG, "getLayout : " + getResourceName(id));
            return parser;
        }

        Drawable loadDrawable(TypedValue value, int id, int density, Theme theme) throws NotFoundException {
            LogUtil.d(TAG, "loadDrawable : " + "1111" + density);
            return null;
        }
    }

    static int getThemeIndex() {
        return sThemeIndex;
    }

    static void setThemeIndex(int index) {
        sThemeIndex = index;
        LayoutInflaterFactory.onThemeChanged();
    }

    public static class MultiColorList extends ColorStateList {

        private static final int DEFAULT_COLOR = Color.RED;
        private static final int[][] EMPTY = new int[][] { new int[0] };
        private ColorStateList mColorStateList;
        private Resources mResources;
        private Context mContext;
        private String mResName;
        private int mThemeIndex = -1;

        public MultiColorList(Context context, int resId, String resEntryName) {
            super(EMPTY, new int[] {DEFAULT_COLOR});
            mContext = context;
            mResources = context.getResources();
            mResName = resEntryName;

        }

        @Override
        public int getColorForState(@Nullable int[] stateSet, int defaultColor) {
            // LogUtil.d(TAG, "zzz :" + Arrays.toString(stateSet));
            int color = defaultColor;

            int index = getThemeIndex();
            if (index != mThemeIndex) {
                mThemeIndex = index;
                String strIndex = "";
                if (mThemeIndex > 0) {
                    strIndex = "_" + mThemeIndex;
                }
                int resId = mResources.getIdentifier(mResName + strIndex, "color", mContext.getPackageName());
                mColorStateList = mResources.getColorStateList(resId);
            }

            ColorStateList colorStateList = mColorStateList;
            if (colorStateList != null) {
                color = colorStateList.getColorForState(stateSet, defaultColor);
            }
            return color;
        }

        @Override
        public boolean isStateful() {
            return true;
        }
    }

    public static class  LayoutInflaterFactory implements LayoutInflater.Factory2 {
        private static List<LayoutInflaterFactory> factories = new ArrayList<>();

        private LayoutInflater mLayoutInflater = null;

        private LayoutInflaterFactory(LayoutInflater inflater) {
            mLayoutInflater = inflater;
        }
        private View mTopView;

        public static LayoutInflaterFactory create(LayoutInflater inflater) {
            LayoutInflaterFactory inflaterFactory = new LayoutInflaterFactory(inflater);
            factories.add(inflaterFactory);
            return inflaterFactory;
        }

        public static void onThemeChanged() {
            for (LayoutInflaterFactory factory : factories) {
                factory.onRefreshTheme();
            }
        }
        private void onRefreshTheme() {
            View view = mTopView;
            if (view != null) {
                boolean hasFocus = view.hasWindowFocus();
                view.dispatchWindowFocusChanged(hasFocus);
            }
        }

        @Override
        public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
            return null;
        }

        @Override
        public View onCreateView(@Nullable final View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
            // LogUtil.d(TAG, "parent:" + parent + ", view:" + name);
            View view = null;
            try {
                view = mLayoutInflater.onCreateView(context, parent, name, attrs);
            } catch (Exception e) {

            }

            if (parent ==  null) {
                mTopView = view;
            }

            Resources resources = mLayoutInflater.getContext().getResources();

            for (int i = 0; i < attrs.getAttributeCount(); ++i) {
                String attrName = attrs.getAttributeName(i);
                if (!isThemeAttr(attrName)) {
                    continue;
                }

                int resId = attrs.getAttributeResourceValue(i, 0);

                if (resId == 0) {
                    continue;
                }

                String entryName = resources.getResourceEntryName(resId);
                if (!entryName.startsWith("theme_")) {
                    continue;
                }

                String typeName = resources.getResourceTypeName(resId);
                LogUtil.d(TAG, attrName + ", " + resId + ":" + entryName + ":" + typeName);
                update(attrName, typeName, resId, entryName, view);
            }
            return view;
        }

        private boolean isThemeAttr(String strAttrName) {
            boolean ret = false;
            ret = strAttrName.contains("Color")|| strAttrName.contains("background") || strAttrName.contains("src");
            return ret;
        }

        private void update(String attrName, String attrType, int resId, String resEntryName, View view) {
            switch (attrName) {
                case "textColor": {
                    ColorStateList colorList = new MultiColorList(mLayoutInflater.getContext(), resId, resEntryName);
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(colorList);
                    }
                }
                    break;
                case "background": {
                    MultiThemeDrawable dr = new MultiThemeDrawable(mLayoutInflater.getContext(), resEntryName, attrType);
                    view.setBackground(dr);
                }
                    break;
                case "src": {
                    MultiThemeDrawable dr = new MultiThemeDrawable(mLayoutInflater.getContext(), resEntryName, attrType);
                    ((ImageView) view).setImageDrawable(dr);
                }
                    break;
                default:
                    ;
            }

        }

    }
}
