package com.officialakbarali.fabiz.bottomSheets;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.officialakbarali.fabiz.R;

public class SalesReviewFilterBottomSheet extends BottomSheetDialogFragment {
    public static final String SALES_REVIEW_FILTER_TAG = "sales_review_filter_dialogue";

    String selectedFilter = "BillId";
    RadioGroup radioGroup;

    private SalesReviewFilterBottomSheet.SalesReviewFilterListener mListener;

    public static SalesReviewFilterBottomSheet newInstance() {
        return new SalesReviewFilterBottomSheet();
    }

    public interface SalesReviewFilterListener {
        void onFilterSelect(String filterItem);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter_sales_review, container,
                false);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radioGroup = view.findViewById(R.id.item_filter_group);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SalesReviewFilterListener) {
            mListener = (SalesReviewFilterListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FilterListener");
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
            case R.id.item_filter_name:
                selectedFilter = "Name";
                break;
            case R.id.item_filter_id:
                selectedFilter = "ItemId";
                break;
            case R.id.item_filter_brand:
                selectedFilter = "Brand";
                break;
            case R.id.item_filter_cate:
                selectedFilter = "Category";
                break;
            default:
                selectedFilter = "BillId";
                break;

        }
        mListener.onFilterSelect(selectedFilter);
    }
}