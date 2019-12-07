package com.officialakbarali.fabiz.requestStock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.requestStock.data.PickItemData;

import java.util.List;

import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.getCurrency;

public class PickItemAdapter extends RecyclerView.Adapter<PickItemAdapter.ItemViewHolder> {
    private Context mContext;
    private PickItemAdapter.ItemAdapterOnClickListener mClickHandler;
    private List<PickItemData> itemDetailList;

    public interface ItemAdapterOnClickListener {
        void onClick(PickItemData itemDetail, int index);
    }

    @NonNull
    @Override
    public PickItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.item_pick_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new PickItemAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickItemAdapter.ItemViewHolder holder, int position) {
        holder.mainParent.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_animation));

        PickItemData itemDetail = itemDetailList.get(position);

        String id = "" + itemDetail.getId();
        if (id.length() > 20) {
            holder.itemId.setText("Id: " + id.substring(0, 16) + "...");
        } else {
            holder.itemId.setText("Id: " + id);
        }

        String name = itemDetail.getName();
        if (name.length() > 40) {
            holder.itemName.setText("Name: " + name.substring(0, 36) + "...");
        } else {
            holder.itemName.setText("Name: " + name);
        }

        String brand = itemDetail.getBrand();
        if (brand.length() > 15) {
            holder.itemBrand.setText("Brand: " + brand.substring(0, 11) + "...");
        } else {
            holder.itemBrand.setText("Brand: " + brand);
        }


        String category = itemDetail.getCategory();
        if (category.length() > 15) {
            holder.itemCategory.setText("Category: " + category.substring(0, 11) + "...");
        } else {
            holder.itemCategory.setText("Category: " + category);
        }

        String price = TruncateDecimal(itemDetail.getPrice() + "");
//        if (price.length() > 20) {
//            holder.itemPrice.setText("Price: " + price.substring(0, 16) + "...");
//        } else {
//            holder.itemPrice.setText("Price: " + TruncateDecimal(price + "") + " " + getCurrency());
//        }

        String qty = itemDetail.getQty() + "";
        holder.qtyText.setText(qty);
    }

    @Override
    public int getItemCount() {
        if (itemDetailList == null) return 0;
        return itemDetailList.size();
    }

    public PickItemAdapter(Context context, PickItemAdapter.ItemAdapterOnClickListener itemAdapterOnClickListener) {
        this.mContext = context;
        this.mClickHandler = itemAdapterOnClickListener;
    }

    public List<PickItemData> swapAdapter(List<PickItemData> c) {
        List<PickItemData> temp = itemDetailList;
        itemDetailList = c;
        notifyDataSetChanged();
        return temp;
    }


    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemId, itemName, itemBrand, itemCategory;
        EditText qtyText;
        Button addButton, rmvBtn;
        LinearLayout mainParent;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mainParent = itemView.findViewById(R.id.main_parent);

            itemId = itemView.findViewById(R.id.id);
            itemName = itemView.findViewById(R.id.name);
            itemBrand = itemView.findViewById(R.id.brand);
            itemCategory = itemView.findViewById(R.id.category);

            qtyText = itemView.findViewById(R.id.qty_for_req);
            addButton = itemView.findViewById(R.id.pos);
            rmvBtn = itemView.findViewById(R.id.neg);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fromQtyText = qtyText.getText().toString().trim();
                    int currentQty = Integer.parseInt(fromQtyText);
                    currentQty++;
                    qtyText.setText(currentQty + "");

                    int adapterPosition = getAdapterPosition();
                    PickItemData itemDetail = itemDetailList.get(adapterPosition);
                    itemDetail.setQty(currentQty);
                    itemDetailList.set(adapterPosition, itemDetail);
                    mClickHandler.onClick(itemDetail, adapterPosition);
                }
            });

            rmvBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fromQtyText = qtyText.getText().toString().trim();
                    int currentQty = Integer.parseInt(fromQtyText);
                    currentQty--;
                    if (currentQty < 1) {
                        currentQty = 0;
                        qtyText.setText(currentQty + "");
                        mClickHandler.onClick(itemDetailList.get(getAdapterPosition()), -1);
                    } else {
                        qtyText.setText(currentQty + "");
                        int adapterPosition = getAdapterPosition();
                        PickItemData itemDetail = itemDetailList.get(adapterPosition);
                        itemDetail.setQty(currentQty);
                        itemDetailList.set(adapterPosition, itemDetail);
                        mClickHandler.onClick(itemDetail, adapterPosition);
                    }
                }
            });
        }
    }
}
