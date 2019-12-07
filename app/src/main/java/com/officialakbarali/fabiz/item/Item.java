package com.officialakbarali.fabiz.item;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.bottomSheets.ItemFilterBottomSheet;
import com.officialakbarali.fabiz.customer.sale.data.Cart;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.item.adapter.ItemAdapter;
import com.officialakbarali.fabiz.item.data.ItemDetail;
import com.officialakbarali.fabiz.item.data.UnitData;


import java.util.ArrayList;
import java.util.List;

import static com.officialakbarali.fabiz.bottomSheets.ItemFilterBottomSheet.ITEM_FILTER_TAG;
import static com.officialakbarali.fabiz.customer.sale.Sales.cartItems;
import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;


public class Item extends AppCompatActivity implements ItemAdapter.ItemAdapterOnClickListener, ItemFilterBottomSheet.ItemFilterListener {
    RecyclerView recyclerView;
    ItemAdapter itemAdapter;
    Toast toast;


    private boolean FOR_SALE = false;

    EditText searchEditText;

    String filterSelection = FabizContract.Item.COLUMN_NAME + " LIKE ?";

    List<UnitData> unitData;

    UnitData myUnitData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        fillUnitData();

        FOR_SALE = getIntent().getBooleanExtra("fromSales", false);

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
        itemAdapter = new ItemAdapter(this, this);
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
    public void onClick(ItemDetail itemDetail) {
        if (FOR_SALE) {
            enterQtyDialogue(itemDetail);
        }
    }

    private void fillUnitData() {
        FabizProvider provider = new FabizProvider(this, false);
        Cursor cursor = provider.query(FabizContract.ItemUnit.TABLE_NAME, new String[]{FabizContract.ItemUnit.FULL_COLUMN_ID, FabizContract.ItemUnit.COLUMN_UNIT_NAME,
                FabizContract.ItemUnit.COLUMN_QTY}, null, null, null);
        unitData = new ArrayList<>();
        while (cursor.moveToNext()) {
            unitData.add(new UnitData(cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_UNIT_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_QTY))));
        }
        myUnitData = unitData.get(0);
    }


    private void enterQtyDialogue(final ItemDetail itemDetail) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_customer_sale_item_qty);

        //SETTING SCREEN WIDTH
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************

        final TextView labelText = dialog.findViewById(R.id.cust_sale_add_item_label_pop);
        labelText.setText(String.format("%s / %s / %s", itemDetail.getName(), itemDetail.getBrand(), itemDetail.getCategory()));

        final EditText priceText = dialog.findViewById(R.id.cust_sale_add_item_price);
        double iPrice = itemDetail.getPrice() * myUnitData.getQty();
        priceText.setText(TruncateDecimal(iPrice + ""));


        final EditText quantityText = dialog.findViewById(R.id.cust_sale_add_item_qty);
        quantityText.setText("1");
        final TextView totalText = dialog.findViewById(R.id.cust_sale_add_item_total);
        totalText.setText(TruncateDecimal(itemDetail.getPrice() + ""));

        priceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String priceS = priceText.getText().toString().trim();
                String qtyS = quantityText.getText().toString().trim();
                String totS = totalText.getText().toString().trim();

                if (conditionsForDialogue(priceS, qtyS, totS)) {
                    double priceToCart = Double.parseDouble(priceS);
                    int quantityToCart = Integer.parseInt(qtyS);
                    double totalToCart = priceToCart * quantityToCart;
                    totalText.setText(TruncateDecimal(totalToCart + ""));
                }
            }
        });

        quantityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String priceS = priceText.getText().toString().trim();
                String qtyS = quantityText.getText().toString().trim();
                String totS = totalText.getText().toString().trim();

                if (conditionsForDialogue(priceS, qtyS, totS)) {
                    double priceToCart = Double.parseDouble(priceS);
                    int quantityToCart = Integer.parseInt(qtyS);
                    double totalToCart = priceToCart * quantityToCart;
                    totalText.setText(TruncateDecimal(totalToCart + ""));
                }
            }
        });


        Button addItemButton = dialog.findViewById(R.id.cust_sale_add_item_add);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String priceS = priceText.getText().toString().trim();
                String qtyS = quantityText.getText().toString().trim();
                String totS = totalText.getText().toString().trim();
                if (conditionsForDialogue(priceS, qtyS, totS)) {
                    if (checkAlreadyExist(itemDetail.getId(), myUnitData.getId())) {
                        showToast(itemDetail.getName() + " Replaced Successfully");
                    }
                    cartItems.add(new Cart("", "", itemDetail.getId(), myUnitData.getId(), myUnitData.getUnitName(), itemDetail.getName(), itemDetail.getBrand(), itemDetail.getCategory(),
                            Double.parseDouble(priceS), Integer.parseInt(qtyS), Double.parseDouble(totS), 0));
                    finish();
                } else {
                    showToast("Please enter valid number");
                }
                dialog.dismiss();
            }
        });

        Button cancelDialogue = dialog.findViewById(R.id.cust_sale_add_item_cancel);
        cancelDialogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


//**************************************SETTING UP SPINNER
        List<String> spinnerData = new ArrayList<>();
        for (int i = 0; i < unitData.size(); i++) {
            UnitData temp = unitData.get(i);
            spinnerData.add(temp.getUnitName());
        }


        final Spinner unitS = dialog.findViewById(R.id.spinner_unit);
        unitS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                myUnitData = unitData.get(position);
                double iPrice = itemDetail.getPrice() * myUnitData.getQty();
                priceText.setText(TruncateDecimal(iPrice + ""));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.custom_spinner_item, spinnerData);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        unitS.setAdapter(spinnerAdapter);

        dialog.show();
    }

    private void showItem(String selection, String[] selectionArg) {
        FabizProvider provider = new FabizProvider(this, false);
        String[] projection = {FabizContract.Item._ID, FabizContract.Item.COLUMN_UNIT_ID, FabizContract.Item.COLUMN_NAME, FabizContract.Item.COLUMN_BRAND,
                FabizContract.Item.COLUMN_CATEGORY, FabizContract.Item.COLUMN_PRICE};
        Cursor iCursor = provider.query(FabizContract.Item.TABLE_NAME, projection,
                selection, selectionArg
                , FabizContract.Item.COLUMN_NAME + " ASC");

        List<ItemDetail> itemList = new ArrayList<>();
        while (iCursor.moveToNext()) {
            itemList.add(new ItemDetail(iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item._ID)),
                    iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_UNIT_ID)),
                    iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_NAME)),
                    iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_BRAND)),
                    iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_CATEGORY)),
                    Double.parseDouble(iCursor.getString(iCursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_PRICE)))
            ));
        }
        recyclerView.setVisibility(View.VISIBLE);
        itemAdapter.swapAdapter(itemList);
        if (itemList.size() > 0) {
            displayEmptyView(false);
        } else {
            displayEmptyView(true);
        }
    }


    private void showToast(String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    private boolean conditionsForDialogue(String s1, String s2, String s3) {
        if (s1.matches("") || s2.matches("") ||
                s3.matches("")) {
            return false;
        } else {
            try {
                double priceToCart = Double.parseDouble(s1);
                int quantityToCart = Integer.parseInt(s2);
                double totalToCart = Double.parseDouble(s3);

                return priceToCart > 0 && quantityToCart > 0 && totalToCart > 0;
            } catch (Error e) {
                return false;
            }
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
    public void onFilterSelect(String filterItem) {
        filterSelection = filterItem + " LIKE ?";
        if (!searchEditText.getText().toString().trim().matches("")) {
            showItem(filterSelection, new String[]{searchEditText.getText().toString().trim() + "%"});
        }
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
                }).duration(300).repeat(0).playOn(searchEditText);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(300).repeat(0).playOn(searchEditText);
    }

    private boolean checkAlreadyExist(String itemId, String unitId) {
        boolean alredyExits = false;
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getItemId().matches(itemId) && cartItems.get(i).getUnitId().matches(unitId)) {
                alredyExits = true;
                cartItems.remove(i);
                break;
            }
        }
        return alredyExits;
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
