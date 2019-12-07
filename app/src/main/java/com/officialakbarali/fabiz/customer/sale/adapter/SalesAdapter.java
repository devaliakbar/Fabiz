package com.officialakbarali.fabiz.customer.sale.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.sale.data.Cart;

import java.util.List;

import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;

public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SalesViewHolder> {
    private Context mContext;
    private SalesAdapterOnClickListener mClickHandler;

    private List<Cart> cartList;
    private boolean SET_SALES_REVIEW_VISIBILITY;
    private boolean SET_SALES_RETURN_VISIBILITY;

    public interface SalesAdapterOnClickListener {
        void onClick(int indexToBeRemoved, Cart cartITemList);
    }

    public SalesAdapter(Context context, SalesAdapterOnClickListener salesAdapterOnClickListener, boolean setVisibilityOfReview, boolean setVisibilityOfReturn) {
        this.mContext = context;
        this.mClickHandler = salesAdapterOnClickListener;
        this.SET_SALES_REVIEW_VISIBILITY = setVisibilityOfReview;
        this.SET_SALES_RETURN_VISIBILITY = setVisibilityOfReturn;
    }

    @NonNull
    @Override
    public SalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.customer_sales_view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new SalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesViewHolder holder, int position) {
        holder.mainParent.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_animation));

        Cart cart = cartList.get(position);

        String name = cart.getItemId() + " / " + cart.getName() + " / " + cart.getBrand() + " / " + cart.getCategory();
        if (name.length() > 130) {
            holder.itemDetail.setText(name.substring(0, 126) + "...");
        } else {
            holder.itemDetail.setText(name);
        }

        String price = TruncateDecimal(cart.getPrice() + "");
        if (price.length() > 80) {
            holder.itemPrice.setText(price.substring(0, 76) + "...");
        } else {
            holder.itemPrice.setText(price);
        }

        String quantity = cart.getQty() + "";
        if (quantity.length() > 30) {
            holder.itemQty.setText(quantity.substring(0, 26) + "...");
        } else {
            holder.itemQty.setText(quantity);
        }

        String total = TruncateDecimal(cart.getTotal() + "");
        if (total.length() > 80) {
            holder.itemTotal.setText(total.substring(0, 76) + "...");
        } else {
            holder.itemTotal.setText(total);
        }

        holder.itemUnit.setText(cart.getUnitName());

        if (SET_SALES_REVIEW_VISIBILITY) {
            if (SET_SALES_RETURN_VISIBILITY) {
                holder.removeBtn.setVisibility(View.VISIBLE);
            } else {
                holder.removeBtn.setVisibility(View.GONE);
            }

            holder.itemReturn.setVisibility(View.VISIBLE);

            String returnI = TruncateDecimal(cart.getReturnQty() + "");
            if (returnI.length() > 30) {
                holder.itemReturn.setText(returnI.substring(0, 26) + "...");
            } else {
                holder.itemReturn.setText(returnI);
            }

        } else {
            holder.removeBtn.setVisibility(View.VISIBLE);
            holder.itemReturn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (cartList == null) return 0;
        return cartList.size();
    }

    public List<Cart> swapAdapter(List<Cart> c) {
        List<Cart> temp = cartList;
        cartList = c;
        notifyDataSetChanged();
        return temp;
    }

    class SalesViewHolder extends RecyclerView.ViewHolder {
        TextView itemDetail, itemPrice, itemQty, itemTotal, itemReturn, itemUnit;
        ImageButton removeBtn;
        RelativeLayout mainParent;

        public SalesViewHolder(@NonNull View itemView) {
            super(itemView);

            mainParent = itemView.findViewById(R.id.main_parent);

            removeBtn = itemView.findViewById(R.id.cust_sale_view_rmv_btn);
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickHandler.onClick(getAdapterPosition(), cartList.get(getAdapterPosition()));
                }
            });

            itemUnit = itemView.findViewById(R.id.cust_sale_view_unit);

            itemDetail = itemView.findViewById(R.id.cust_sale_view_detail);
            itemPrice = itemView.findViewById(R.id.cust_sale_view_price);
            itemQty = itemView.findViewById(R.id.cust_sale_view_qty);
            itemTotal = itemView.findViewById(R.id.cust_sale_view_total);
            itemReturn = itemView.findViewById(R.id.cust_sale_view_return);
        }
    }
}
