package com.openapi.debugger;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class  DebuggerActivity extends Activity {
    private List<ActionAdapter.Action> mActionList = new ArrayList<>();
    private ActionAdapter mAcitonAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAcitonAdapter = new ActionAdapter(this, mActionList);
        RecyclerView recyclerView = new RecyclerView(this);
        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        recyclerView.setLayoutManager(new GridLayoutManager(this, landscape ? 2 : 1));
        recyclerView.setAdapter(mAcitonAdapter);

        addContentView(recyclerView,
                new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        addAction(new ActionAdapter.Action("返回上一级") {
            @Override
            public boolean invoke() {
                finish();
                return false;
            }
        });

        init();

        mAcitonAdapter.notifyDataSetChanged();

    }

    protected void addAction(ActionAdapter.Action actionInfo) {
        mActionList.add(actionInfo);
    }


    protected abstract void init();

};
