package com.officialakbarali.fabiz.bottomSheets;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.officialakbarali.fabiz.R;

public class CustomerFilterBottomSheet extends BottomSheetDialogFragment {
    public static final String CUSTOMER_FILTER_TAG = "customer_filter_dialogue";

    String selectedFilter = "Name";
    RadioGroup radioGroup;

    private CustomerFilterListener mListener;

    public static CustomerFilterBottomSheet newInstance() {
        return new CustomerFilterBottomSheet();
    }

    public interface CustomerFilterListener {
        void onFilterSelect(String filterItem);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter_customer, container,
                false);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radioGroup = view.findViewById(R.id.cust_filter_group);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CustomerFilterListener) {
            mListener = (CustomerFilterListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CustomerFilterListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        switch (selectedId) {
            case R.id.cust_filter_id:
                selectedFilter = "Id";
                break;
            case R.id.cust_filter_phone:
                selectedFilter = "Phone";
                break;
            case R.id.cust_filter_email:
                selectedFilter = "Email";
                break;
            default:
                selectedFilter = "Name";
                break;
        }
        mListener.onFilterSelect(selectedFilter);
    }

}