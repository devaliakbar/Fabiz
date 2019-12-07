package com.officialakbarali.fabiz.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.data.CustomerDetail;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    private Context mContext;
    private CustomerAdapterOnClickListener mClickHandler;
    private List<CustomerDetail> customerList;
    private String currentDay;

    public interface CustomerAdapterOnClickListener {
        void onClick(CustomerDetail customer);
    }

    public CustomerAdapter(Context context, CustomerAdapterOnClickListener customerAdapterOnClickListener, String currentDay) {
        mContext = context;
        mClickHandler = customerAdapterOnClickListener;
        this.currentDay = currentDay;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.customer_home_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {

        holder.mainParent.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_scale_animation));

        CustomerDetail customer = customerList.get(position);

        String id = "" + customer.getId();
        if (id.length() > 10) {
            holder.custId.setText(id.substring(0, 6) + "...");
        } else {
            holder.custId.setText(id);
        }

        String name = customer.getName();
        if (name.length() > 40) {
            holder.custName.setText(name.substring(0, 36) + "...");
        } else {
            holder.custName.setText(name);
        }

        String phone = customer.getPhone();
        if (phone.length() > 13) {
            holder.custPhone.setText(phone.substring(0, 9) + "...");
        } else {
            holder.custPhone.setText(phone);
        }


        String address = customer.getAddress();
        if (address.length() > 33) {
            holder.custAddress.setText(address.substring(0, 29) + "...");
        } else {
            holder.custAddress.setText(address);
        }

        String email = customer.getEmail();
        if (email.length() > 30) {
            holder.custEmail.setText(email.substring(0, 26) + "...");
        } else {
            holder.custEmail.setText(email);
        }

        if (currentDay != null) {
            if (customer.getDay() == null) {
                holder.rmvOrAddOrSelect.setImageResource(R.drawable.ic_add);
            } else {
                if (currentDay.matches(customer.getDay())) {
                    holder.rmvOrAddOrSelect.setImageResource(R.drawable.ic_remove);
                } else {
                    holder.rmvOrAddOrSelect.setImageResource(R.drawable.ic_add);
                }
            }
        }


    }

    @Override
    public int getItemCount() {
        if (customerList == null) return 0;
        return customerList.size();
    }

    public List<CustomerDetail> swapAdapter(List<CustomerDetail> c) {
        List<CustomerDetail> temp = customerList;
        customerList = c;
        notifyDataSetChanged();
        return temp;
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView custId, custName, custPhone, custEmail, custAddress;
        ImageButton rmvOrAddOrSelect;
        RelativeLayout mainParent;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            custId = itemView.findViewById(R.id.id);
            custName = itemView.findViewById(R.id.name);
            custPhone = itemView.findViewById(R.id.phone);
            custEmail = itemView.findViewById(R.id.email);
            custAddress = itemView.findViewById(R.id.address);

            mainParent = itemView.findViewById(R.id.customer_home_item);

            rmvOrAddOrSelect = itemView.findViewById(R.id.remove_or_add);
            rmvOrAddOrSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickHandler.onClick(customerList.get(getAdapterPosition()));
                }
            });
        }
    }
}
