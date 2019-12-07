package com.officialakbarali.fabiz.network.syncInfo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;

import java.util.List;

public class SyncFromAppAdapter extends RecyclerView.Adapter<SyncFromAppAdapter.SyncFromAppViewHolder> {
    private Context mContext;
    private List<SyncLogDetail> syncList;

    public SyncFromAppAdapter(Context context) {
        this.mContext = context;
    }

    public List<SyncLogDetail> swapAdapter(List<SyncLogDetail> c) {
        List<SyncLogDetail> temp = syncList;
        syncList = c;
        notifyDataSetChanged();
        return temp;
    }

    @NonNull
    @Override
    public SyncFromAppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.sync_from_app_view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new SyncFromAppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SyncFromAppViewHolder holder, int position) {
        SyncLogDetail syncLog = syncList.get(position);
        holder.tbNameV.setText("Table Name :" + syncLog.getTableName());
        holder.rowIdV.setText("Row Id :" + syncLog.getRawId());
        holder.operationV.setText(syncLog.getOperation());
    }

    @Override
    public int getItemCount() {
        if (syncList == null) return 0;
        return syncList.size();
    }


    class SyncFromAppViewHolder extends RecyclerView.ViewHolder {

        TextView tbNameV, rowIdV, operationV;

        public SyncFromAppViewHolder(@NonNull View itemView) {
            super(itemView);

            tbNameV = itemView.findViewById(R.id.sync_from_app_view_tbname);
            rowIdV = itemView.findViewById(R.id.sync_from_app_view_row_id);
            operationV = itemView.findViewById(R.id.sync_from_app_view_op);
        }
    }
}

