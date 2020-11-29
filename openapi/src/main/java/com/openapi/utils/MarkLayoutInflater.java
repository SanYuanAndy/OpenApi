package com.openapi.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MarkLayoutInflater extends LayoutInflater {

    private static class MarkContextWrapper extends ContextWrapper {
        private static final String TAG = MarkContextWrapper.class.getSimpleName();
        private LayoutInflater mInflater = null;

        public MarkContextWrapper(Context context){
            super(context);
        }

        @Override
        public Object getSystemService(String name) {
            if (TextUtils.equals(Context.LAYOUT_INFLATER_SERVICE, name)){
                if (mInflater == null){
                    synchronized (this) {
                        if (mInflater == null) {
                            mInflater = new MarkLayoutInflater(this);
                        }
                    }
                }
                return mInflater;
            }
            return super.getSystemService(name);
        }
    }


    private static final String TAG = MarkLayoutInflater.class.getSimpleName();
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app."
    };

    /**
     * Instead of instantiating directly, you should retrieve an instance
     * through {@link Context#getSystemService}
     *
     * @param context The Context in which in which to find resources and other
     *                application-specific things.
     * @see Context#getSystemService
     */
    public MarkLayoutInflater(Context context) {
        super(context);
    }

    protected MarkLayoutInflater(LayoutInflater original, Context newContext) {
        super(original, newContext);
    }

    /**
     * Override onCreateView to instantiate names that correspond to the
     * widgets known to the Widget factory. If we don't find a match,
     * call through to our super class.
     */

    @Override
    protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
        for (String prefix : sClassPrefixList) {
            try {
                View view = createView(name, prefix, attrs);
                if (view != null) {
                    return view;
                }
            } catch (ClassNotFoundException e) {
                // In this case we want to let the base class take a crack
                // at it.
            }
        }

        return super.onCreateView(name, attrs);
    }

    public LayoutInflater cloneInContext(Context newContext) {
        return new MarkLayoutInflater(this, newContext);
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        String layoutXmlName = getContext().getResources().getResourceName(resource);
        layoutXmlName = layoutXmlName.replace(getContext().getPackageName() + ":layout/", "");
        layoutXmlName = layoutXmlName.replace("android:layout/", "");
        print(TAG, "layout xml : " + layoutXmlName + ", " + getContext());
        View view = super.inflate(resource, root, attachToRoot);

        if (!sConfig.isIgnored(layoutXmlName)) {
            Drawable drawable = new TextDrawable(layoutXmlName, view);
            view.setTag(TAG_ID, drawable);
            view.getOverlay().add(drawable);
        }

        return view;
    }

    private static Field getFiledInSingleClass(Class<?> clazz, String name){
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        }catch (Exception e){
            //print(TAG,"getField err : " + e.toString());
        }
        return field;
    }

    public static Field getField(Class<?> clazz, String name) {
        Field field = null;
        String className = clazz != null ? clazz.getSimpleName() : null;
        while (clazz != null) {
            print(TAG, "clazz : " + clazz.getSimpleName());
            field = getFiledInSingleClass(clazz, name);
            if (field != null){
                break;
            }
            clazz = clazz.getSuperclass();
        }
        print(TAG, className + ":" + field);
        return field;
    }

    private static int TAG_ID = 0;

    /**初始化sdk
     * @param application application实例
     * @param resID 任意有效的资源id
     */
    public static void initial(Application application, @IdRes int resID){
        initial(application, resID, "");
    }

    /**初始化sdk
     * @param application application实例
     * @param resID 任意有效的资源id
     * @param propertyName 系统属性字段，如果系统没有设置该字段，则初始化会失败, 用来控制是否显示布局名称
     * @note propertyName可以使用adb root, adb shell setprop propertyName propertyName
     */
    public static void initial(Application application, @IdRes int resID, String propertyName){
        if (!TextUtils.isEmpty(propertyName)) {
            String property = getSystemProperty(propertyName);
            if (!TextUtils.equals(propertyName, property)) {
                print(TAG, "can not find " + propertyName + " in property");
                return;
            }
        }

        TAG_ID = resID;
        replaceBaseContext(application);
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                print(TAG, "onActivityCreated : " + activity.getComponentName());
                MarkLayoutInflater.replaceBaseContext(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                //MarkLayoutInflater.showTag(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });

    }
    /*
        LayoutInflater=LayoutInflater.from(Context)=Context.getSystemService()
        1、替换Context中的mBase,并且重写mBase的getSystemService方法
        2、PhoneWindow中有单独的mLayoutInflater,并且比创建时机早于Activity的onCreate方法，所以要
          单独再次替换

     */
    public static int replaceBaseContext(Context context){
        Class<?> clazz = context.getClass();
        //替换Activity的mInflater
        {
            Field field = getField(clazz, "mInflater");
            if (field != null){
                field.setAccessible(true);
                try {
                    field.set(context, null);
                    print(TAG, "replace mInflater : " + context);
                }catch (Exception e){
                    print(TAG, "replace mInflater error: " + e.toString());
                }
            }
        }
        ////替换Activity中Window的mInflater
        {
            Field field = getField(clazz, "mWindow");
            if (field != null){
                field.setAccessible(true);
                try {
                    Window window = (Window)field.get(context);
                    Field winFiled = getField(window.getClass(), "mLayoutInflater");
                    if (winFiled != null){
                        winFiled.setAccessible(true);
                        winFiled.set(window, new MarkLayoutInflater(context));
                        print(TAG, "replace window mInflater : " + context);
                    }
                }catch (Exception e){
                    print(TAG, "replace mInflater error: " + e.toString());
                }
            }
        }

        //替换Context中的mBase
        {
            Field field = getField(clazz, "mBase");
            if (field != null){
                field.setAccessible(true);
                try {
                    Context baseContext = (Context)field.get(context);
                    field.set(context, new MarkContextWrapper(baseContext));
                }catch (Exception e){
                    print(TAG, "replace mBase error: " + e.toString());
                }
            }
        }


        return 0;

    }

    public static class Config{
        private int mTextSize = 21;
        private List<String> mIgoreList = new ArrayList<>();
        public Config setTextSize(int size){
            mTextSize = size;
            return this;
        }

        public int getTextSize(){
            return mTextSize;
        }

        public Config addIgoreXml(String name){
            mIgoreList.add(name);
            return  this;
        }

        public boolean isIgnored(String name){
            boolean ret = false;
            for(String item : mIgoreList){
                boolean b = false;
                try {
                    b = Pattern.matches(item, name);
                } catch (Exception e) {

                }
                if (b){
                    ret = true;
                    break;
                }
            }
            return ret;
        }

    }

    private static Config sConfig = new Config();

    private static class TextDrawable extends PaintDrawable {
        private View mView = null;
        private String mText = "";
        public int h = 0;
        public int w = 0;

        public TextDrawable(String text, View view){
            mText = text;
            mView = view;
            init();
        }

        private void init(){
            Paint paint = new Paint();//防止锯齿
            int textSize = sConfig.getTextSize();
            paint.setTextSize(textSize);
            Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
            h = fontMetrics.bottom-fontMetrics.top;

            //2.用bounds计算宽度
            Rect bounds = new Rect();
            paint.getTextBounds(mText, 0, mText.length(), bounds);
            w = bounds.right-bounds.left;
        }


        @Override
        public void draw(@NonNull Canvas canvas) {

            int marginTop = getMarginTop(mView);
            int x = 0;
            int y = 0;

            y += marginTop;

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);//防止锯齿
            paint.setTextAlign(Paint.Align.LEFT);  //从右边开始draw
            int textSize = sConfig.getTextSize();
            paint.setTextSize(textSize);
            paint.setColor(0xffffffff);
            RectF rect = new RectF(x, y, x + w, y + h - 2);
            canvas.drawRect(rect, paint);

            paint.setColor(0xff000000);
            canvas.drawText(mText, x, y + h - 5, paint);
        }

        private int getMarginTop(View view){
            int margin = 0;
            View top = getFirstTagParent(view);
            if (top == null){
                return 0;
            }

            int[] parentloc = new int[2];
            top.getLocationInWindow(parentloc);

            int[] childloc = new int[2];
            view.getLocationInWindow(childloc);

            int rest = childloc[1] - parentloc[1] - getMarginTop(top);

            margin = rest > h ? 0 : (h - rest);

            Object drawable = top.getTag(TAG_ID);
            if (drawable instanceof TextDrawable){
                int w = ((TextDrawable)drawable).w;
                int ret = childloc[0] - parentloc[0];
                if (ret > w){
                    return 0;
                }
            }

            return margin;
        }
    }

    private static View getFirstTagParent(View view){
        View result = null;
        ViewParent parent = view.getParent();
        while(parent != null){
             if (parent instanceof View) {
                 Object tag = ((View)parent).getTag(TAG_ID);
                 if (tag instanceof TextDrawable) {
                     result = (View) parent;
                     break;
                 }
             }
             parent = parent.getParent();
        }

        return result;
    }


    private static boolean DEBUG = false;
    public static void setDebug(boolean isDebug){
        DEBUG = isDebug;
    }

    private static void print(String tag, String log){
        if (DEBUG) {
            Log.d(TAG, log);
        }
    }

    public static void setConfig(Config config){
        sConfig = config;
    }


    public static String getSystemProperty(String key){
        String value = null;
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getMethod("get", String.class);
            value = (String)method.invoke(clazz, key);
        }catch (Exception e){
            print(TAG, "getSystemProperty err : " + e.toString());
        }

        return value;
    }

}

