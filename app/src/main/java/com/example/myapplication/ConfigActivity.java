package com.example.myapplication;

import android.content.Intent;

import com.openapi.debugger.ActionAdapter;
import com.openapi.debugger.DaemonService;
import com.openapi.debugger.DebuggerActivity;

public class ConfigActivity extends DebuggerActivity {

    @Override
    protected void init() {
        addAction(new ActionAdapter.Action("显示悬浮框") {
            @Override
            public boolean invoke() {
                Intent intent = new Intent(ConfigActivity.this, DaemonService.class);
                intent.putExtra("opt", 1);
                startService(intent);
                return false;
            }
        });

        addAction(new ActionAdapter.Action("隐藏悬浮框") {
            @Override
            public boolean invoke() {
                Intent intent = new Intent(ConfigActivity.this, DaemonService.class);
                intent.putExtra("opt", 0);
                startService(intent);
                return false;
            }
        });
    }
    
}
