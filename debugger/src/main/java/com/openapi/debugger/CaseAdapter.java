package com.openapi.debugger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class CaseInfo {
        public String label;
        public String classFullName;
        public Class<?> clazz;
    }

    public static class  CaseViewHolder extends RecyclerView.ViewHolder {

        private Button tvName;

        public CaseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
        }

    }

    private List<CaseInfo> mData = null;
    private Context mContext = null;

    public CaseAdapter(Context context, List<CaseInfo> cases) {
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
                CaseInfo caseInfo = mData.get(position);
                Intent intent = new Intent(mContext, caseInfo.clazz);
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        List<CaseInfo> list = mData;
        return list == null ? 0 : list.size();
    }
}
