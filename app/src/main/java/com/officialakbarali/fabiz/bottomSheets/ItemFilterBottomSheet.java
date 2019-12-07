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
import com.officialakbarali.fabiz.data.db.FabizContract;

public class ItemFilterBottomSheet extends BottomSheetDialogFragment {
    public static final String ITEM_FILTER_TAG = "item_filter_dialogue";

    String selectedFilter = FabizContract.Item.COLUMN_NAME;
    RadioGroup radioGroup;

    private ItemFilterListener mListener;

    public static ItemFilterBottomSheet newInstance() {
        return new ItemFilterBottomSheet();
    }

    public interface ItemFilterListener {
        void onFilterSelect(String filterItem);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter_item, container,
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
        if (context instanceof ItemFilterListener) {
            mListener = (ItemFilterListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ItemFilterListener");
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
            case R.id.item_filter_id:
                selectedFilter = FabizContract.Item._ID;
                break;
            case R.id.item_filter_brand:
                selectedFilter = FabizContract.Item.COLUMN_BRAND;
                break;
            case R.id.item_filter_cate:
                selectedFilter = FabizContract.Item.COLUMN_CATEGORY;
                break;
            default:
                selectedFilter = FabizContract.Item.COLUMN_NAME;
                break;
        }
        mListener.onFilterSelect(selectedFilter);
    }

}