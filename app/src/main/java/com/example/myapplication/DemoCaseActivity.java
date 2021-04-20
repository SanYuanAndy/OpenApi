package com.example.myapplication;

import android.widget.Toast;

import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DebuggerActivity;

public class DemoCaseActivity extends DebuggerActivity {

    @Override
    protected void init() {
        addAction(new ActionAdapter.Action("显示Toast") {
            @Override
            public boolean invoke() {
                Toast.makeText(getBaseContext(), "invoke", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("第二个接口") {
            @Override
            public boolean invoke() {
                Toast.makeText(getBaseContext(), "invoke", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        addAction(new ActionAdapter.Action("Toast测试") {
            @Override
            public boolean invoke() {
                Toast.makeText(getBaseContext(), "invoke", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    public static String getDebugLabel() {
        return "DemoCaseActivity";
    }
}
