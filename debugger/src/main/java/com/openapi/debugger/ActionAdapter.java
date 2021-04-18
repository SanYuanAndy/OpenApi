package com.openapi.debugger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public abstract static class Action {
        public String label;

        public Action(String label) {
            this.label = label;
        }

        public abstract boolean invoke();
    }

    public static class  CaseViewHolder extends RecyclerView.ViewHolder {

        private Button tvName;

        public CaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
        }

    }

    private List<Action> mData = null;
    private Context mContext = null;

    public ActionAdapter(Context context, List<Action> cases) {
        mData = cases;
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_case, null);
        return new CaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        CaseViewHolder caseViewHolder = (CaseViewHolder) holder;
        caseViewHolder.tvName.setText(mData.get(position).label);
        caseViewHolder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Action action = mData.get(position);
                if (action != null) {
                    action.invoke();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        List<Action> list = mData;
        return list == null ? 0 : list.size();
    }
}
