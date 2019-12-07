package com.officialakbarali.fabiz.requestStock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.animation.Animator;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.bottomSheets.ItemFilterBottomSheet;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;


import com.officialakbarali.fabiz.requestStock.adapter.PickItemAdapter;
import com.officialakbarali.fabiz.requestStock.data.PickItemData;

import java.util.ArrayList;
import java.util.List;

import static com.officialakbarali.fabiz.bottomSheets.ItemFilterBottomSheet.ITEM_FILTER_TAG;
import static com.officialakbarali.fabiz.requestStock.RequestStock.itemsForRequest;

public class RequestItem extends AppCompatActivity implements PickItemAdapter.ItemAdapterOnClickListener, ItemFilterBottomSheet.ItemFilterListener {
    Toast toast;

    RecyclerView recyclerView;
    PickItemAdapter itemAdapter;

    List<PickItemData> fullItem, itemList;

    EditText searchEditText;
    String filterSelection = FabizContract.Item.COLUMN_NAME + " LIKE ?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_item);

        searchEditText = findViewById(R.id.item_search);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!searchEditText.getText().toString().trim().matches("")) {
                    showItem(filterSelection, new String[]{searchEditText.getText().toString().trim() + "%"});
                } else {
                    showItem(null, null);
                }
            }
        });
        setUpDrawableEndEditext();

        recyclerView = findViewById(R.id.item_recycler);
        itemAdapter = new PickItemAdapter(this, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(itemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }

    @Override
    public void onClick(PickItemData itemDetail, int index) {
        if (index == -1) {
            String itemNameToUpdate = itemDetail.getName() + " / " + itemDetail.getBrand() + " / " +
                    itemDetail.getCategory();
            for (int i = 0; i < itemsForRequest.size(); i++) {
                com.officialakbarali.fabiz.requestStock.data.RequestItem checkItem = itemsForRequest.get(i);
                if (checkItem.getName().matches(itemNameToUpdate)) {
                    itemsForRequest.remove(i);
                    showToast("Removed Item:" + itemNameToUpdate);
                    break;
                }
            }

        } else {
            itemList.set(index, itemDetail);
            insertThisItem(itemDetail);
        }
    }

    private void insertThisItem(final PickItemData itemDetail) {
        String itemNameToUpdate = itemDetail.getName() + " / " + itemDetail.getBrand() + " / " +
                itemDetail.getCategory();
        boolean alreadyExist = false;
        for (int i = 0; i < itemsForRequest.size(); i++) {
            com.officialakbarali.fabiz.requestStock.data.RequestItem checkItem = itemsForRequest.get(i);
            if (checkItem.getName().matches(itemNameToUpdate)) {
                checkItem.setQty(itemDetail.getQty() + "");
                itemsForRequest.set(i, checkItem);
                showToast("Updated Item :" + itemNameToUpdate + ", QTY :" + itemDetail.getQty());
                alreadyExist = true;
                break;
            }
        }
        if (!alreadyExist) {
            itemsForRequest.add(new com.officialakbarali.fabiz.requestStock.data.RequestItem(itemNameToUpdate, itemDetail.getQty() + ""));
            showToast("Added Item :" + itemNameToUpdate + ", QTY :" + itemDetail.getQty());
        }
    }

    private void showItem(String selection, String[] selectionArg) {
        FabizProvider provider = new FabizProvider(this, false);
        String[] projection = {FabizContract.Item._ID, FabizContract.Item.COLUMN_NAME, FabizContract.Item.COLUMN_BRAND,
                FabizContract.Item.COLUMN_CATEGORY, FabizContract.Item.COLUMN_PRICE};
        Cursor iCursor = provider.query(FabizContract.Item.TABLE_NAME, projection,
                selection, selectionArg
                , FabizContract.Item.COLUMN_NAME + " ASC");

        itemList = new ArrayList<>();
        while (iCursor.moveToNext()) {
            itemList.add(new PickItemData(iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item._ID)),
                    iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_NAME)),
                    iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_BRAND)),
                    iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_CATEGORY)),
                    Double.parseDouble(iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_PRICE)))
            ));
        }

        if (fullItem == null) {
            setPreviousQty();
            fullItem = itemList;
        } else {
            makeQty();
        }
        recyclerView.setVisibility(View.VISIBLE);
        itemAdapter.swapAdapter(itemList);

        if (itemList.size() > 0) {
            displayEmptyView(false);
        } else {
            displayEmptyView(true);
        }
    }

    private void makeQty() {

        for (int j = 0; j < itemList.size(); j++) {
            PickItemData currentItem = itemList.get(j);
            for (int i = 0; i < fullItem.size(); i++) {
                PickItemData fullCheckItem = fullItem.get(i);
                if (fullCheckItem.getId() == currentItem.getId()) {
                    currentItem.setQty(fullCheckItem.getQty());
                    break;
                }
            }
        }
    }

    private String getSelection(String filterFromForm) {
        String caseSelection;

        switch (filterFromForm) {
            case "Id":
                caseSelection = FabizContract.Item._ID;
                break;
            case "Brand":
                caseSelection = FabizContract.Item.COLUMN_BRAND;
                break;
            case "Category":
                caseSelection = FabizContract.Item.COLUMN_CATEGORY;
                break;
            default:
                caseSelection = FabizContract.Item.COLUMN_NAME;
        }

        return caseSelection + " LIKE ?";
    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
    }

    private void setPreviousQty() {
        for (int i = 0; i < itemList.size(); i++) {
            PickItemData itemDetail = itemList.get(i);
            String itemNameToUpdate = itemDetail.getName() + " / " + itemDetail.getBrand() + " / " +
                    itemDetail.getCategory();
            for (int j = 0; j < itemsForRequest.size(); j++) {
                com.officialakbarali.fabiz.requestStock.data.RequestItem checkItem = itemsForRequest.get(j);
                if (checkItem.getName().matches(itemNameToUpdate)) {
                    itemDetail.setQty(Integer.parseInt(checkItem.getQty()));
                    itemList.set(i, itemDetail);
                    break;
                }
            }
        }
    }

    @Override
    public void onFilterSelect(String filterItem) {
        filterSelection = filterItem + " LIKE ?";
        if (!searchEditText.getText().toString().trim().matches("")) {
            showItem(filterSelection, new String[]{searchEditText.getText().toString().trim() + "%"});
        }
    }

    private void setUpDrawableEndEditext() {
        searchEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (searchEditText.getRight() - searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        ItemFilterBottomSheet itemFilterBottomSheet = ItemFilterBottomSheet.newInstance();
                        itemFilterBottomSheet.show(getSupportFragmentManager(), ITEM_FILTER_TAG);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onPause() {
        super.onPause();
        recyclerView.setVisibility(View.INVISIBLE);
        searchEditText.setVisibility(View.INVISIBLE);
        displayEmptyView(false);
    }


    private void setUpAnimation() {
        recyclerView.setVisibility(View.INVISIBLE);
        searchEditText.setVisibility(View.INVISIBLE);

        YoYo.with(Techniques.SlideInLeft).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                searchEditText.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showItem(null, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).duration(400).repeat(0).playOn(searchEditText);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(400).repeat(0).playOn(searchEditText);
    }
    private void displayEmptyView(boolean setOn) {
        LinearLayout emptyView = findViewById(R.id.empty_view);
        if (setOn) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

    }
}
