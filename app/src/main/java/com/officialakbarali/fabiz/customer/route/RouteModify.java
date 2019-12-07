package com.officialakbarali.fabiz.customer.route;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.bottomSheets.CustomerFilterBottomSheet;
import com.officialakbarali.fabiz.customer.adapter.CustomerAdapter;
import com.officialakbarali.fabiz.customer.data.CustomerDetail;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;

import java.util.ArrayList;
import java.util.List;

import static com.officialakbarali.fabiz.bottomSheets.CustomerFilterBottomSheet.CUSTOMER_FILTER_TAG;
import static com.officialakbarali.fabiz.data.CommonInformation.convertToCamelCase;
import static com.officialakbarali.fabiz.data.CommonInformation.getDayNameFromNumber;

public class RouteModify extends AppCompatActivity implements CustomerAdapter.CustomerAdapterOnClickListener, CustomerFilterBottomSheet.CustomerFilterListener {
    private String TODAY;

    CustomerAdapter adapter;
    RecyclerView recyclerView;
    EditText searchEditText;
    String filterSelection = "Name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_modify);

        TODAY = getIntent().getStringExtra("today");

        TextView labelText = findViewById(R.id.route_day);
        labelText.setText(convertToCamelCase(getDayNameFromNumber(TODAY)));

        searchEditText = findViewById(R.id.cust_search);
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
                    showCustomer(getSelection(filterSelection), new String[]{searchEditText.getText().toString().trim() + "%"});
                } else {
                    showCustomer(null, null);
                }
            }
        });
        setUpDrawableEndEditext();

        recyclerView = findViewById(R.id.cust_route_recycler);
        adapter = new CustomerAdapter(this, this, TODAY);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }

    @Override
    public void onClick(CustomerDetail customer) {
        if (customer.getDay() == null) {
            toggleCurrentDay(customer.getId(), true);
        } else {
            if (customer.getDay().matches(TODAY)) {
                toggleCurrentDay(customer.getId(), false);
            } else {
                toggleCurrentDay(customer.getId(), true);
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
                        CustomerFilterBottomSheet customerFilterBottomSheet = CustomerFilterBottomSheet.newInstance();
                        customerFilterBottomSheet.show(getSupportFragmentManager(), CUSTOMER_FILTER_TAG);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void toggleCurrentDay(String idToOperate, boolean add) {
        ContentValues updateValues = new ContentValues();
        if (add) {
            updateValues.put(FabizContract.Customer.COLUMN_DAY, TODAY);
        } else {
            updateValues.put(FabizContract.Customer.COLUMN_DAY, "NA");
        }

        FabizProvider provider = new FabizProvider(this, true);
        provider.createTransaction();
        int updatedRows = provider.update(FabizContract.Customer.TABLE_NAME, updateValues,
                FabizContract.Customer._ID + "=?", new String[]{idToOperate + ""});

        if (updatedRows > 0) {
            provider.successfulTransaction();
            showCustomer(null, null);
        }
        provider.finishTransaction();
    }

    private void showCustomer(String selection, String[] selectionArg) {
        FabizProvider provider = new FabizProvider(this, false);
        String[] projection = {FabizContract.Customer._ID, FabizContract.Customer.COLUMN_NAME, FabizContract.Customer.COLUMN_PHONE,
                FabizContract.Customer.COLUMN_EMAIL, FabizContract.Customer.COLUMN_ADDRESS, FabizContract.Customer.COLUMN_DAY
        };
        Cursor custCursor = provider.query(FabizContract.Customer.TABLE_NAME, projection,
                selection, selectionArg
                , FabizContract.Customer.COLUMN_NAME + " ASC");

        List<CustomerDetail> customerList = new ArrayList<>();
        while (custCursor.moveToNext()) {
            customerList.add(new CustomerDetail(custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer._ID)),
                    custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_NAME)),
                    custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_PHONE)),
                    custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_EMAIL)),
                    custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_ADDRESS)),
                    custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_DAY))
            ));
        }
        recyclerView.setVisibility(View.VISIBLE);
        adapter.swapAdapter(customerList);

        if (customerList.size() > 0) {
            displayEmptyView(false);
        } else {
            displayEmptyView(true);
        }
    }

    private String getSelection(String filterFromForm) {
        String caseSelection;

        switch (filterFromForm) {
            case "Id":
                caseSelection = FabizContract.Customer._ID;
                break;
            case "Phone":
                caseSelection = FabizContract.Customer.COLUMN_PHONE;
                break;
            case "Email":
                caseSelection = FabizContract.Customer.COLUMN_EMAIL;
                break;
            case "Address":
                caseSelection = FabizContract.Customer.COLUMN_ADDRESS;
                break;
            default:
                caseSelection = FabizContract.Customer.COLUMN_NAME;
        }

        return caseSelection + " LIKE ?";
    }

    @Override
    public void onFilterSelect(String filterItem) {
        filterSelection = filterItem;
        if (!searchEditText.getText().toString().trim().matches("")) {
            showCustomer(getSelection(filterItem), new String[]{searchEditText.getText().toString().trim() + "%"});
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
        hideAllViews();
    }

    private void hideAllViews() {
        TextView head = findViewById(R.id.route_day);
        head.setVisibility(View.INVISIBLE);
        searchEditText.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideAllViews();
        final TextView head = findViewById(R.id.route_day);

        YoYo.with(Techniques.SlideInRight).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                head.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchEditText.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                showCustomer(null, null);
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
                }).duration(400).repeat(0).playOn(head);
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
