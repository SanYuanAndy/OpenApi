package com.openapi.comm.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.openapi.comm.R;

import java.util.ArrayList;
import java.util.List;

public class CommDialog extends Dialog implements View.OnClickListener {
    private Builder mBuilder;

    public static class Builder {
       private Context mContext;
       private DialogCallBack mCallBack;
       private int mLayoutId;
       private String mTitle;
       private String mContent;

       public Builder (Context context) {
           mContext = context;
       }

       public Builder callBack(DialogCallBack callBack) {
           mCallBack = callBack;
           return this;
       }

       public Builder layoutId(int layoutId) {
           mLayoutId = layoutId;
           return this;
       }

        public Builder title(String title) {
            mTitle = title;
            return this;
        }

        public Builder content(String content) {
            mContent = content;
            return this;
        }

       public CommDialog build() {
           CommDialog dialog = new CommDialog(this);
           return dialog;
       }
    }

    public static class DialogCallBack {
        public void onClickOk() {

        }

        public void onClickCancel() {

        }

    }

    public CommDialog(Builder builder) {
        super(builder.mContext);
        mBuilder = builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        window.requestFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        setContentView(mBuilder.mLayoutId);

        List<View> clickViews = new ArrayList<>();
        clickViews.add(findViewById(R.id.btn_cancel));
        clickViews.add(findViewById(R.id.btn_ok));
        for (View v : clickViews) {
            if (v != null) {
                v.setOnClickListener(this);
            }
        }

        TextView titleView = findViewById(R.id.tv_title);
        if (titleView != null) {
            if (mBuilder.mTitle != null) {
                titleView.setText(mBuilder.mTitle);
            }
        }

        TextView contentView = findViewById(R.id.tv_content);
        if (contentView != null) {
            if (mBuilder.mContent != null) {
                contentView.setText(mBuilder.mContent);
            }
        }
    }

    @Override
    public void onClick(View view) {
        DialogCallBack cb = mBuilder.mCallBack;
        int id = view.getId();

        if (id == R.id.btn_ok) {
            if (cb != null) {
                cb.onClickOk();
            }
        } else if (id == R.id.btn_cancel) {
            if (cb != null) {
                cb.onClickCancel();
            }
        }

        dismiss();
    }
}
