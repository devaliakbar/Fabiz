package com.officialakbarali.fabiz.customer.sale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.bottomSheets.ItemFilterBottomSheet;
import com.officialakbarali.fabiz.bottomSheets.SalesReviewFilterBottomSheet;
import com.officialakbarali.fabiz.customer.sale.adapter.SalesReviewAdapter;
import com.officialakbarali.fabiz.customer.sale.data.SalesReviewDetail;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.officialakbarali.fabiz.bottomSheets.ItemFilterBottomSheet.ITEM_FILTER_TAG;
import static com.officialakbarali.fabiz.bottomSheets.SalesReviewFilterBottomSheet.SALES_REVIEW_FILTER_TAG;
import static com.officialakbarali.fabiz.data.CommonInformation.convertDateToSearchFormat;

public class SalesReview extends AppCompatActivity implements SalesReviewAdapter.SalesReviewAdapterOnClickListener, SalesReviewFilterBottomSheet.SalesReviewFilterListener {
    SalesReviewAdapter salesReviewAdapter;
    private String custId;
    RecyclerView recyclerView;
    private boolean FROM_SALERS_RETURN = false;
    EditText searchEditText;
    String filterSelection = FabizContract.BillDetail.FULL_COLUMN_ID + " LIKE ?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_review);

        FROM_SALERS_RETURN = getIntent().getBooleanExtra("fromSalesReturn", false);
        custId = getIntent().getStringExtra("id");

        recyclerView = findViewById(R.id.sales_review_recycler);

        salesReviewAdapter = new SalesReviewAdapter(this, this, false);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(salesReviewAdapter);

        if (FROM_SALERS_RETURN) setUpThisPageForReturn();

        ImageButton showCalenderForFilter = findViewById(R.id.sales_review_date_filter_button);
        showCalenderForFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });


        searchEditText = findViewById(R.id.sales_review_search);
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
                    showBills(filterSelection, new String[]{searchEditText.getText().toString().trim() + "%"});
                } else {
                    showBills(null, null);
                }
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

    @Override
    public void onClick(SalesReviewDetail salesReviewDetail) {
        String idOfBill = salesReviewDetail.getId();
        Intent salesDetaiiilIntent = new Intent(SalesReview.this, com.officialakbarali.fabiz.customer.sale.SalesReviewDetail.class);
        salesDetaiiilIntent.putExtra("custId", custId + "");
        salesDetaiiilIntent.putExtra("billId", idOfBill + "");

        if (FROM_SALERS_RETURN) {
            salesDetaiiilIntent.putExtra("fromSalesReturn", true);
        } else {
            salesDetaiiilIntent.putExtra("fromSalesReturn", false);
        }


        startActivity(salesDetaiiilIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setUpThisPageForReturn() {
        TextView quoteText;
        quoteText = findViewById(R.id.sales_review_quote);
        quoteText.setVisibility(View.VISIBLE);
    }

    private void showBills(String Fselection, String[] FselectionArg) {
        FabizProvider provider = new FabizProvider(this, false);

        String tableName = FabizContract.BillDetail.TABLE_NAME + " INNER JOIN " + FabizContract.Cart.TABLE_NAME
                + " ON " + FabizContract.BillDetail.FULL_COLUMN_ID + " = " + FabizContract.Cart.FULL_COLUMN_BILL_ID;

        String selection = FabizContract.BillDetail.FULL_COLUMN_CUST_ID + "=?";

        String[] selectionArg;

        if (Fselection != null) {
            selection += " AND " + Fselection;
            selectionArg = new String[]{custId + "", FselectionArg[0]};
        } else {
            selectionArg = new String[]{custId + ""};
        }

        Cursor cursorBills = provider.queryExplicit(
                true, tableName,
                new String[]{FabizContract.BillDetail.FULL_COLUMN_ID, FabizContract.BillDetail.FULL_COLUMN_DATE,
                        FabizContract.BillDetail.FULL_COLUMN_QTY, FabizContract.BillDetail.FULL_COLUMN_PRICE,
                        FabizContract.BillDetail.FULL_COLUMN_PAID, FabizContract.BillDetail.FULL_COLUMN_DUE,
                        FabizContract.BillDetail.FULL_COLUMN_RETURNED_TOTAL, FabizContract.BillDetail.FULL_COLUMN_CURRENT_TOTAL
                        , FabizContract.BillDetail.FULL_COLUMN_DISCOUNT},
                selection, selectionArg, null, null, FabizContract.BillDetail.FULL_COLUMN_ID + " DESC", null);

        List<SalesReviewDetail> salesReviewList = new ArrayList<>();

        while (cursorBills.moveToNext()) {
            salesReviewList.add(new SalesReviewDetail(cursorBills.getString(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail._ID)),
                    cursorBills.getString(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DATE)),
                    cursorBills.getInt(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_QTY)),
                    cursorBills.getDouble(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PRICE)),
                    cursorBills.getDouble(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PAID)),
                    cursorBills.getDouble(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DUE)),
                    cursorBills.getDouble(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL)),
                    cursorBills.getDouble(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL)),
                    cursorBills.getDouble(cursorBills.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DISCOUNT))
            ));
        }
        recyclerView.setVisibility(View.VISIBLE);
        salesReviewAdapter.swapAdapter(salesReviewList);
        if (salesReviewList.size() > 0) {
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
                            showBills(FabizContract.BillDetail.COLUMN_DATE + " LIKE ?", new String[]{convertDateToSearchFormat(fromDateTime) + "%"});
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
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
        LinearLayout searchCont = findViewById(R.id.search_cont);

        recyclerView.setVisibility(View.INVISIBLE);
        searchCont.setVisibility(View.INVISIBLE);

        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideViews();

        final LinearLayout searchCont = findViewById(R.id.search_cont);

        YoYo.with(Techniques.SlideInLeft).withListener(new Animator.AnimatorListener() {
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
                        showBills(null, null);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).duration(400).repeat(0).playOn(searchCont);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(400).repeat(0).playOn(recyclerView);


    }

    @Override
    public void onFilterSelect(String filterItem) {
        filterSelection = getSelection(filterItem);
        if (!searchEditText.getText().toString().trim().matches("")) {
            showBills(filterSelection, new String[]{searchEditText.getText().toString().trim() + "%"});
        }
    }
    private String getSelection(String filterFromForm) {
        String caseSelection;

        switch (filterFromForm) {
            case "Name":
                caseSelection = FabizContract.Cart.FULL_COLUMN_NAME;
                break;
            case "ItemId":
                caseSelection = FabizContract.Cart.FULL_COLUMN_ITEM_ID;
                break;
            case "Brand":
                caseSelection = FabizContract.Cart.FULL_COLUMN_BRAND;
                break;
            case "Category":
                caseSelection = FabizContract.Cart.FULL_COLUMN_CATAGORY;
                break;
            default:
                caseSelection = FabizContract.BillDetail.FULL_COLUMN_ID;
        }

        return caseSelection + " LIKE ?";
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
