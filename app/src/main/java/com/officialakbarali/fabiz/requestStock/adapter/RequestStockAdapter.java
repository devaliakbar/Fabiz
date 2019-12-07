package com.officialakbarali.fabiz.requestStock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.requestStock.data.RequestItem;

import java.util.List;

public class RequestStockAdapter extends RecyclerView.Adapter<RequestStockAdapter.RequestStockViewHolder> {
    List<RequestItem> itemList;
    private Context context;
    private RequestStockOnClickListener listener;

    public interface RequestStockOnClickListener {
        void onClick(int indexToRemove);
    }

    public RequestStockAdapter(Context context, RequestStockOnClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestStockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.item_request_stock;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new RequestStockAdapter.RequestStockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestStockViewHolder holder, int position) {

        holder.mainParent.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_animation));

        RequestItem requestItem = itemList.get(position);

        if (requestItem.getName().length() > 100) {
            holder.nameV.setText(requestItem.getName().substring(0, 100));
        } else {
            holder.nameV.setText(requestItem.getName());
        }

        holder.qtyV.setText("QTY :" + requestItem.getQty());
    }

    @Override
    public int getItemCount() {
        if (itemList == null) return 0;
        return itemList.size();
    }

    public List<RequestItem> swapAdapter(List<RequestItem> c) {
        List<RequestItem> temp = itemList;
        itemList = c;
        notifyDataSetChanged();
        return temp;
    }

    class RequestStockViewHolder extends RecyclerView.ViewHolder {

        TextView nameV, qtyV;
        Button removeB;

        LinearLayout mainParent;

        public RequestStockViewHolder(@NonNull View itemView) {
            super(itemView);

            mainParent = itemView.findViewById(R.id.main_parent);

            nameV = itemView.findViewById(R.id.item_request_stock_name);
            qtyV = itemView.findViewById(R.id.item_request_stock_qty);
            removeB = itemView.findViewById(R.id.item_request_stock_rmv);
            removeB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
