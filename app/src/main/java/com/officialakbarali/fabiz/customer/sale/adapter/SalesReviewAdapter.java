package com.officialakbarali.fabiz.customer.sale.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.sale.SalesReview;
import com.officialakbarali.fabiz.customer.sale.data.SalesReviewDetail;

import java.text.ParseException;
import java.util.List;

import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.getCurrency;

public class SalesReviewAdapter extends RecyclerView.Adapter<SalesReviewAdapter.SalesReviewHolder> {
    private Context mContext;
    private SalesReviewAdapterOnClickListener mClickHandler;

    private List<SalesReviewDetail> salesList;

    private boolean FROM_PAYMENT_PAGE;

    public interface SalesReviewAdapterOnClickListener {
        void onClick(SalesReviewDetail salesReviewDetail);
    }

    public SalesReviewAdapter(Context context, SalesReviewAdapterOnClickListener salesReviewAdapterOnClickListener, boolean forPayment) {
        this.mContext = context;
        this.mClickHandler = salesReviewAdapterOnClickListener;
        this.FROM_PAYMENT_PAGE = forPayment;
    }


    @NonNull
    @Override
    public SalesReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.sales_review_view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new SalesReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalesReviewHolder holder, int position) {
        holder.mainParent.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_animation));

        SalesReviewDetail salesReview = salesList.get(position);

        String salesIdS = salesReview.getId() + "";
        if (salesIdS.length() > 10) {
            holder.billIdV.setText("Bill Id :" + salesIdS.substring(0, 7) + "...");
        } else {
            holder.billIdV.setText("Bill Id :" + salesIdS);
        }

        String dateS = salesReview.getDate();
        if (dateS.length() > 24) {
            holder.dateV.setText(dateS.substring(0, 16) + "...");
        } else {
            holder.dateV.setText(dateS);
        }


        String qtyS = salesReview.getQty() + "";
        if (qtyS.length() > 4) {
            holder.totQtyV.setText(qtyS.substring(0, 1) + "..");
        } else {
            holder.totQtyV.setText(qtyS);
        }


        String totalS = TruncateDecimal(salesReview.getTotal() + "");
        if (totalS.length() > 17) {
            holder.totV.setText(totalS.substring(0, 13) + "...");
        } else {
            holder.totV.setText(totalS+ " " + getCurrency());
        }


        if (FROM_PAYMENT_PAGE) {
            holder.viewB.setVisibility(View.GONE);
            holder.payButton.setVisibility(View.VISIBLE);


            holder.paidCont.setVisibility(View.VISIBLE);
            String paidS = TruncateDecimal(salesReview.getPaid() + "");
            if (paidS.length() > 13) {
                holder.paidV.setText(paidS.substring(0, 13) + "...");
            } else {
                holder.paidV.setText(paidS+ " " + getCurrency());
            }

            holder.dueCont.setVisibility(View.VISIBLE);
            String dueS = TruncateDecimal(salesReview.getDue() + "");
            if (dueS.length() > 13) {
                holder.dueV.setText(dueS.substring(0, 13) + "...");
            } else {
                holder.dueV.setText(dueS+ " " + getCurrency());
            }

            holder.returnC.setVisibility(View.VISIBLE);
            String returnS = TruncateDecimal(salesReview.getReturnedAmount() + "");
            if (returnS.length() > 13) {
                holder.returnV.setText(returnS.substring(0, 13) + "...");
            } else {
                holder.returnV.setText(returnS+ " " + getCurrency());
            }


            holder.currentCont.setVisibility(View.VISIBLE);
            String currentTotalS = TruncateDecimal(salesReview.getCurrentTotal() + "");
            if (currentTotalS.length() > 13) {
                holder.cTotalV.setText(currentTotalS.substring(0, 13) + "...");
            } else {
                holder.cTotalV.setText(currentTotalS+ " " + getCurrency());
            }

            holder.discCont.setVisibility(View.VISIBLE);
            String discountS = TruncateDecimal(salesReview.getDiscount() + "");
            if (discountS.length() > 13) {
                holder.cDiscountV.setText(discountS.substring(0, 13) + "...");
            } else {
                holder.cDiscountV.setText(discountS+ " " + getCurrency());
            }
        }
    }

    @Override
    public int getItemCount() {
        if (salesList == null) return 0;
        return salesList.size();
    }

    public List<SalesReviewDetail> swapAdapter(List<SalesReviewDetail> c) {
        List<SalesReviewDetail> temp = salesList;
        salesList = c;
        notifyDataSetChanged();
        return temp;
    }


    class SalesReviewHolder extends RecyclerView.ViewHolder {
        TextView billIdV, dateV, totQtyV, totV, paidV, dueV, returnV, cTotalV, cDiscountV;
        ImageButton viewB;
        LinearLayout mainParent;

        Button payButton;

        LinearLayout returnC, discCont, currentCont, paidCont, dueCont;
        public SalesReviewHolder(@NonNull View itemView) {
            super(itemView);



            mainParent = itemView.findViewById(R.id.main_parent);

            billIdV = itemView.findViewById(R.id.sales_review_view_bill_id);
            dateV = itemView.findViewById(R.id.sales_review_view_date);
            totQtyV = itemView.findViewById(R.id.sales_review_view_tot_items);
            totV = itemView.findViewById(R.id.sales_review_view_total);


            paidV = itemView.findViewById(R.id.sales_review_view_paid);
            dueV = itemView.findViewById(R.id.sales_review_due);
            returnV = itemView.findViewById(R.id.sales_review_view_return_total);
            cTotalV = itemView.findViewById(R.id.sales_review_view_current_total);

            cDiscountV = itemView.findViewById(R.id.sales_review_view_discount);

            viewB = itemView.findViewById(R.id.sales_review_view_view);
            viewB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickHandler.onClick(salesList.get(getAdapterPosition()));
                }
            });


            returnC = itemView.findViewById(R.id.return_cont);
            discCont = itemView.findViewById(R.id.disc_cont);
            currentCont = itemView.findViewById(R.id.current_cont);
            paidCont = itemView.findViewById(R.id.paid_cont);
            dueCont = itemView.findViewById(R.id.due_cont);

            payButton = itemView.findViewById(R.id.pay_btn);
            payButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickHandler.onClick(salesList.get(getAdapterPosition()));
                }
            });

        }
    }
}
