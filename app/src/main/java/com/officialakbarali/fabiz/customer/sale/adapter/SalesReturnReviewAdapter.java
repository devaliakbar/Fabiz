package com.officialakbarali.fabiz.customer.sale.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.sale.data.SalesReturnReviewItem;

import java.util.List;

import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.getCurrency;


public class SalesReturnReviewAdapter extends RecyclerView.Adapter<SalesReturnReviewAdapter.SalesReturnReviewViewHolder> {
    private Context mContext;
    List<SalesReturnReviewItem> salesReturnList;

    public SalesReturnReviewAdapter(Context context) {
        this.mContext = context;
    }

    @NonNull
    @Override
    public SalesReturnReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.sales_return_review_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new SalesReturnReviewAdapter.SalesReturnReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesReturnReviewViewHolder holder, int position) {
        holder.mainParent.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_animation));

        SalesReturnReviewItem salesReturnReviewItem = salesReturnList.get(position);

        String returnIdS = salesReturnReviewItem.getId() + "";
        if (returnIdS.length() > 17) {
            holder.returnIdT.setText("Return Id : " + returnIdS.substring(0, 13));
        } else {
            holder.returnIdT.setText("Return Id : " + returnIdS);
        }


        String billIdS = salesReturnReviewItem.getBillId() + "";
        if (billIdS.length() > 17) {
            holder.billIdT.setText("Bill Id : " + billIdS.substring(0, 13));
        } else {
            holder.billIdT.setText("Bill Id : " + billIdS);
        }

        String dateS = salesReturnReviewItem.getDate();
        if (dateS.length() > 24) {
            holder.dateT.setText(dateS.substring(0, 20));
        } else {
            holder.dateT.setText(dateS);
        }


        String itemDetail = salesReturnReviewItem.getItemId() + " / " + salesReturnReviewItem.getName() + " / "
                + salesReturnReviewItem.getBrand() + " / " + salesReturnReviewItem.getCatagory();
        holder.itemNameT.setText(itemDetail);


        String priceS = TruncateDecimal(salesReturnReviewItem.getPrice() + "");
        if (priceS.length() > 12) {
            holder.priceT.setText(priceS.substring(0, 8));
        } else {
            holder.priceT.setText(priceS+ " " + getCurrency());
        }

        String qtyS = salesReturnReviewItem.getQty() + "";
        if (qtyS.length() > 6) {
            holder.qtyT.setText(qtyS.substring(0, 2));
        } else {
            holder.qtyT.setText(qtyS);
        }

        String totalS = TruncateDecimal(salesReturnReviewItem.getTotal() + "");
        if (totalS.length() > 12) {
            holder.totalT.setText(totalS.substring(0, 8));
        } else {
            holder.totalT.setText(totalS+ " " + getCurrency());
        }

        holder.unitT.setText(salesReturnReviewItem.getUnitName());

    }

    @Override
    public int getItemCount() {
        if (salesReturnList == null) return 0;
        return salesReturnList.size();
    }

    public List<SalesReturnReviewItem> swapAdapter(List<SalesReturnReviewItem> c) {
        List<SalesReturnReviewItem> temp = salesReturnList;
        salesReturnList = c;
        notifyDataSetChanged();
        return temp;
    }

    class SalesReturnReviewViewHolder extends RecyclerView.ViewHolder {
        TextView billIdT, dateT, itemNameT, priceT, qtyT, totalT, returnIdT, unitT;
        LinearLayout mainParent;

        public SalesReturnReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            mainParent = itemView.findViewById(R.id.main_parent);

            billIdT = itemView.findViewById(R.id.sales_return_review_item_billid);
            dateT = itemView.findViewById(R.id.sales_return_review_item_date);
            itemNameT = itemView.findViewById(R.id.sales_return_review_item_name);
            priceT = itemView.findViewById(R.id.sales_return_review_item_price);
            qtyT = itemView.findViewById(R.id.sales_return_review_item_qty);
            totalT = itemView.findViewById(R.id.sales_return_review_item_tot);
            returnIdT = itemView.findViewById(R.id.sales_return_review_item_return_id);
            unitT = itemView.findViewById(R.id.sales_return_review_item_qty_unit);
        }
    }
}
