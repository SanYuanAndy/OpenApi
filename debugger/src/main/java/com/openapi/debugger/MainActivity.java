package com.openapi.debugger;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.openapi.comm.utils.LogUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<CaseAdapter.CaseInfo> mCaseList = new ArrayList<>();
    private RecyclerView mCaseRecyclerView = null;
    private CaseAdapter mCaseAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.case_home);
        mCaseRecyclerView = findViewById(R.id.recyclerview_cases);
        TextView tv = findViewById(R.id.tv_back);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mCaseRecyclerView.setLayoutManager(layoutManager);
        mCaseAdapter = new CaseAdapter(this, mCaseList);
        mCaseRecyclerView.setAdapter(mCaseAdapter);
        init();
    }

    private void init() {
        PackageManager packageManager = getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (packageInfo == null) {
            return;
        }

        for (ActivityInfo activity : packageInfo.activities) {
            String name = activity.name;
            LogUtil.d(TAG, "activity name:" + name);
            try {
                Class<?> clazz = Class.forName(name);
                String label = null;
                try {
                    Method method = clazz.getMethod("getDebugLabel");
                    label = (String) method.invoke(clazz);
                } catch (Exception e) {

                }
                if (TextUtils.isEmpty(label)) {
                    if (clazz.getSuperclass() == DebuggerActivity.class) {
                        label = clazz.getSimpleName();
                    }
                }

                if (TextUtils.isEmpty(label)) {
                    continue;
                }

                CaseAdapter.CaseInfo info = new CaseAdapter.CaseInfo();
                info.label = label;
                info.classFullName = name;
                info.clazz = clazz;
                mCaseList.add(info);

            } catch (Exception e) {
                // e.printStackTrace();
            }
        }

        mCaseAdapter.notifyDataSetChanged();
    }




}
