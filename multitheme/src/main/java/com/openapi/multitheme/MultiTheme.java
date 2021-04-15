package com.openapi.multitheme;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openapi.comm.utils.LogUtil;
import com.openapi.comm.utils.ReflectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        private static Map<Context, LayoutInflaterFactory> factories = new HashMap();
        private static Window sCurrWindow;
        private Method mOnCreateViewMethod = null;

        private LayoutInflater mLayoutInflater = null;
        private static Set<View> mTopViews = new HashSet<View>();

        private LayoutInflaterFactory(LayoutInflater inflater) {
            mLayoutInflater = inflater;

            Class<?>[] paramsType = new Class<?>[] {View.class, String.class, AttributeSet.class};
            try {
                mOnCreateViewMethod = LayoutInflater.class.getDeclaredMethod("onCreateView", paramsType);
                mOnCreateViewMethod.setAccessible(true);
            } catch (Exception e) {

            }
        }

        public static LayoutInflaterFactory create(LayoutInflater inflater) {
            LayoutInflaterFactory inflaterFactory = new LayoutInflaterFactory(inflater);
            factories.put(inflater.getContext(), inflaterFactory);
            return inflaterFactory;
        }

        public static void destroyFactory(Context context) {
            factories.remove(context);
        }

        public static void onThemeChanged() {
            for (View view : mTopViews) {
                if (view == null) {
                    continue;
                }
                if (view.getWindowVisibility() != View.VISIBLE) {
                    continue;
                }

                view = view.getRootView();
                view.dispatchWindowFocusChanged(view.hasWindowFocus());
            }
        }

        private void addTopView(final View view) {
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    LogUtil.d(TAG, "Attach:" + v);
                    mTopViews.add(view);
                    LogUtil.d(TAG, "top view size : " + mTopViews.size());
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    LogUtil.d(TAG, "Detach:" + v);
                    mTopViews.remove(view);
                    LogUtil.d(TAG, "top view size : " + mTopViews.size());
                }
            });
        }


        @Override
        public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
            return null;
        }

        @Override
        public View onCreateView(@Nullable final View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
            LogUtil.d(TAG, "parent:" + parent + ", view:" + name);
            View view = null;
            try {
                if (-1 == name.indexOf('.')) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        view = mLayoutInflater.onCreateView(context, parent, name, attrs);
                    } else {
                        view = (View) mOnCreateViewMethod.invoke(mLayoutInflater, parent, name, attrs);
                    }
                } else {
                    view = mLayoutInflater.createView(name, null, attrs);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (view == null) {
                return null;
            }

            if (view.getId() == android.R.id.content) {
                addTopView(view);
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
