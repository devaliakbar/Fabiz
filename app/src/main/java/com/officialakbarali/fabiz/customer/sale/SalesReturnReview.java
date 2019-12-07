package com.officialakbarali.fabiz.customer.sale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.bottomSheets.SalesReviewFilterBottomSheet;
import com.officialakbarali.fabiz.customer.sale.adapter.SalesReturnReviewAdapter;
import com.officialakbarali.fabiz.customer.sale.data.SalesReturnReviewItem;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.officialakbarali.fabiz.bottomSheets.SalesReviewFilterBottomSheet.SALES_REVIEW_FILTER_TAG;
import static com.officialakbarali.fabiz.data.CommonInformation.convertDateToSearchFormat;

public class SalesReturnReview extends AppCompatActivity implements SalesReviewFilterBottomSheet.SalesReviewFilterListener {
    private String custId;

    SalesReturnReviewAdapter reviewAdapter;
    EditText searchEditText;
    String filterSelection = FabizContract.SalesReturn.COLUMN_BILL_ID + " LIKE ?";
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_return_review);

        custId = getIntent().getStringExtra("id");

        recyclerView = findViewById(R.id.sales_return_review_recycler);
        reviewAdapter = new SalesReturnReviewAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(reviewAdapter);

        ImageButton showCalenderForFilter = findViewById(R.id.sales_return_review_date);
        showCalenderForFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        searchEditText = findViewById(R.id.sales_return_review_search);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!searchEditText.getText().toString().trim().matches("")) {
                    showReturnedItems(filterSelection, new String[]{searchEditText.getText().toString().trim() + "%"});
                } else {
                    showReturnedItems(null, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setUpDrawableEndEditext();
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
                        SalesReviewFilterBottomSheet salesReviewFilterBottomSheet = SalesReviewFilterBottomSheet.newInstance();
                        salesReviewFilterBottomSheet.show(getSupportFragmentManager(), SALES_REVIEW_FILTER_TAG);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }

    private void showReturnedItems(String Fselection, String[] FselectionArg) {


        FabizProvider provider = new FabizProvider(this, false);

        String tableName = FabizContract.SalesReturn.TABLE_NAME + " INNER JOIN " + FabizContract.BillDetail.TABLE_NAME + " ON " + FabizContract.SalesReturn.FULL_COLUMN_BILL_ID
                + " = " + FabizContract.BillDetail.FULL_COLUMN_ID + " INNER JOIN " + FabizContract.Cart.TABLE_NAME + " ON "
                + FabizContract.Cart.FULL_COLUMN_BILL_ID + " = " + FabizContract.BillDetail.FULL_COLUMN_ID
                + " INNER JOIN " + FabizContract.ItemUnit.TABLE_NAME + " ON "
                + FabizContract.SalesReturn.FULL_COLUMN_UNIT_ID + " = " + FabizContract.ItemUnit.FULL_COLUMN_ID;

        String[] projection = {FabizContract.SalesReturn.FULL_COLUMN_ID,
                FabizContract.SalesReturn.FULL_COLUMN_BILL_ID,
                FabizContract.SalesReturn.FULL_COLUMN_DATE
                , FabizContract.Cart.FULL_COLUMN_ITEM_ID,
                FabizContract.Cart.FULL_COLUMN_NAME,
                FabizContract.Cart.FULL_COLUMN_BRAND,
                FabizContract.Cart.FULL_COLUMN_CATAGORY
                , FabizContract.SalesReturn.FULL_COLUMN_PRICE,
                FabizContract.SalesReturn.FULL_COLUMN_QTY,
                FabizContract.SalesReturn.FULL_COLUMN_TOTAL,
                FabizContract.ItemUnit.FULL_COLUMN_UNIT_NAME
        };

        String selection = FabizContract.SalesReturn.FULL_COLUMN_ITEM_ID + " = " + FabizContract.Cart.FULL_COLUMN_ITEM_ID + " AND " + FabizContract.BillDetail.FULL_COLUMN_CUST_ID + "=?";

        String[] selectionArg;

        if (Fselection != null) {
            selection += " AND " + Fselection;
            selectionArg = new String[]{custId + "", FselectionArg[0]};
        } else {
            selectionArg = new String[]{custId + ""};
        }

        Cursor returnCursor = provider.queryExplicit(true, tableName, projection, selection, selectionArg, null, null, null, null);

        List<SalesReturnReviewItem> salesReturnReviewItems = new ArrayList<>();
        while (returnCursor.moveToNext()) {
            salesReturnReviewItems.add(new SalesReturnReviewItem(
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.SalesReturn._ID)),
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_BILL_ID)),
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_DATE)),
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_ITEM_ID)),
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_NAME)),
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_BRAND)),
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_CATEGORY)),
                    returnCursor.getDouble(returnCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_PRICE)),
                    returnCursor.getInt(returnCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_QTY)),
                    returnCursor.getDouble(returnCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_TOTAL)),
                    returnCursor.getString(returnCursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_UNIT_NAME))
            ));
        }
        recyclerView.setVisibility(View.VISIBLE);
        reviewAdapter.swapAdapter(salesReturnReviewItems);
        if (salesReturnReviewItems.size() > 0) {
            displayEmptyView(false);
        } else {
            displayEmptyView(true);
        }

    }


    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year,
                                          final int monthOfYear, final int dayOfMonth) {
                        String fromDateTime = year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth) + "T";

                        try {
                            showReturnedItems(FabizContract.SalesReturn.COLUMN_DATE + " LIKE ?", new String[]{convertDateToSearchFormat(fromDateTime) + "%"});
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @Override
    public void onFilterSelect(String filterItem) {
        filterSelection = getSelection(filterItem);
        if (!searchEditText.getText().toString().trim().matches("")) {
            showReturnedItems(filterSelection, new String[]{searchEditText.getText().toString().trim() + "%"});
        }
    }

    private String getSelection(String filterFromForm) {
        String caseSelection;

        switch (filterFromForm) {
            case "Name":
                caseSelection = FabizContract.Cart.COLUMN_NAME;
                break;
            case "ItemId":
                caseSelection = FabizContract.SalesReturn.COLUMN_ITEM_ID;
                break;
            case "Brand":
                caseSelection = FabizContract.Cart.COLUMN_BRAND;
                break;
            case "Category":
                caseSelection = FabizContract.Cart.COLUMN_CATEGORY;
                break;
            default:
                caseSelection = FabizContract.SalesReturn.COLUMN_BILL_ID;
        }
        return caseSelection + " LIKE ?";
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideViews();
    }

    private void hideViews() {
        LinearLayout seatchCont = findViewById(R.id.search_cont);
        recyclerView.setVisibility(View.INVISIBLE);
        seatchCont.setVisibility(View.INVISIBLE);

        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideViews();
        final LinearLayout searchCont = findViewById(R.id.search_cont);

        YoYo.with(Techniques.FadeInDown).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                searchCont.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        showReturnedItems(null, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).duration(400).repeat(0).playOn(                searchCont
                );
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(400).repeat(0).playOn(recyclerView);

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
