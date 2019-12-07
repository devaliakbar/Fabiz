package com.officialakbarali.fabiz.customer.payment.adapter;

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
import com.officialakbarali.fabiz.customer.data.PaymentReviewDetail;

import java.util.List;

import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.getCurrency;


public class PaymentReviewAdapter extends RecyclerView.Adapter<PaymentReviewAdapter.PaymentReviewHolder> {
    private List<PaymentReviewDetail> paymentReviewDetailList;
    private Context context;

    public PaymentReviewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PaymentReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.item_payment_review;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new PaymentReviewAdapter.PaymentReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentReviewHolder holder, int position) {
        holder.mainParent.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_scale_animation));

        PaymentReviewDetail detail = paymentReviewDetailList.get(position);

        String idS = "" + detail.getId();
        if (idS.length() > 10) {
            holder.idV.setText(idS.substring(0, 6));
        } else {
            holder.idV.setText(idS);
        }

        String dateS = detail.getDate();
        if (dateS.length() > 24) {
            holder.dateV.setText(dateS.substring(0, 20));
        } else {
            holder.dateV.setText(dateS);
        }

        String amountS = TruncateDecimal("" + detail.getAmount());
        if (amountS.length() > 17) {
            holder.amountV.setText(amountS.substring(0, 13));
        } else {
            holder.amountV.setText(amountS+ " " + getCurrency());
        }


        String billId = detail.getBillId();
        if (billId.length() > 25) {
            holder.billId.setText(billId.substring(0, 21) + "...");
        } else {
            holder.billId.setText(billId);
        }

    }

    @Override
    public int getItemCount() {
        if (paymentReviewDetailList == null) return 0;
        return paymentReviewDetailList.size();
    }

    public List<PaymentReviewDetail> swapAdapter(List<PaymentReviewDetail> c) {
        List<PaymentReviewDetail> temp = paymentReviewDetailList;
        paymentReviewDetailList = c;
        notifyDataSetChanged();
        return temp;
    }

    class PaymentReviewHolder extends RecyclerView.ViewHolder {
        LinearLayout mainParent;
        TextView idV, dateV, amountV, billId;

        PaymentReviewHolder(@NonNull View itemView) {
            super(itemView);

            mainParent = itemView.findViewById(R.id.main_parent);
            idV = itemView.findViewById(R.id.payment_review_item_id);
            dateV = itemView.findViewById(R.id.payment_review_item_date);
            amountV = itemView.findViewById(R.id.payment_review_item_amt);
            billId = itemView.findViewById(R.id.payment_review_item_billid);
        }
    }
}
