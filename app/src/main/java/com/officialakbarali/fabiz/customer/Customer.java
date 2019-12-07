package com.officialakbarali.fabiz.customer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.LogIn;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.blockPages.AppVersion;
import com.officialakbarali.fabiz.blockPages.ForcePull;
import com.officialakbarali.fabiz.blockPages.UpdateData;
import com.officialakbarali.fabiz.bottomSheets.CustomerFilterBottomSheet;
import com.officialakbarali.fabiz.customer.adapter.CustomerAdapter;
import com.officialakbarali.fabiz.customer.data.CustomerDetail;
import com.officialakbarali.fabiz.customer.route.ManageRoute;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.data.barcode.FabizBarcode;
import com.officialakbarali.fabiz.network.syncInfo.services.SyncService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.officialakbarali.fabiz.bottomSheets.CustomerFilterBottomSheet.CUSTOMER_FILTER_TAG;
import static com.officialakbarali.fabiz.data.CommonInformation.convertToCamelCase;
import static com.officialakbarali.fabiz.data.CommonInformation.getDayNameFromNumber;
import static com.officialakbarali.fabiz.data.barcode.FabizBarcode.FOR_CUSTOMER;

public class Customer extends AppCompatActivity implements CustomerAdapter.CustomerAdapterOnClickListener, CustomerFilterBottomSheet.CustomerFilterListener {
    RecyclerView recyclerView;
    CustomerAdapter customerAdapter;
    EditText searcheditText;

    String filterSelection = "Name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        TextView dayText = findViewById(R.id.day_text);
        dayText.setText(convertToCamelCase(getDayNameFromNumber(getCurrentDay())));

        ImageButton scanFromBarcodeButton = findViewById(R.id.cust_barcode);
        scanFromBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanFromBarcodeIntent = new Intent(Customer.this, FabizBarcode.class);
                scanFromBarcodeIntent.putExtra("FOR_WHO", FOR_CUSTOMER + "");
                startActivity(scanFromBarcodeIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ImageButton addCustomerButton = findViewById(R.id.add_cust);
        addCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent CustomerIntent = new Intent(Customer.this, AddCustomer.class);
                startActivity(CustomerIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ImageButton manageRouteButton = findViewById(R.id.cust_manage_route);
        manageRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manageRouteIntent = new Intent(Customer.this, ManageRoute.class);
                startActivity(manageRouteIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ImageButton viewTodayButton = findViewById(R.id.cust_today);
        viewTodayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomer(FabizContract.Customer.COLUMN_DAY + "=?", new String[]{getCurrentDay()});
            }
        });

        ImageButton viewAllButton = findViewById(R.id.cust_all);
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomer(null, null);
            }
        });

        searcheditText = findViewById(R.id.cust_search);
        searcheditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!searcheditText.getText().toString().trim().matches("")) {
                    showCustomer(getSelection(filterSelection), new String[]{searcheditText.getText().toString().trim() + "%"});
                } else {
                    showCustomer(null, null);
                }
            }
        });
        setUpDrawableEndEditext();

        recyclerView = findViewById(R.id.cust_recycler);
        customerAdapter = new CustomerAdapter(this, this, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(customerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBeforeResume();
        setUpAnimation();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onClick(CustomerDetail customer) {
        Intent showHome = new Intent(Customer.this, Home.class);
        showHome.putExtra("id", customer.getId() + "");
        startActivity(showHome);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setUpDrawableEndEditext() {
        searcheditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (searcheditText.getRight() - searcheditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        CustomerFilterBottomSheet customerFilterBottomSheet = CustomerFilterBottomSheet.newInstance();
                        customerFilterBottomSheet.show(getSupportFragmentManager(), CUSTOMER_FILTER_TAG);
                        return true;
                    }
                }
                return false;
            }
        });
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
        customerAdapter.swapAdapter(customerList);

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

    private String getCurrentDay() {
        return String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
    }

    private void checkBeforeResume() {
        Context context = Customer.this;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean appVersionProblem = sharedPreferences.getBoolean("version", false);
        if (appVersionProblem) {
            Intent versionIntent = new Intent(context, AppVersion.class);
            versionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(versionIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            String userName = sharedPreferences.getString("my_username", null);
            String password = sharedPreferences.getString("my_password", null);
            if (userName == null || password == null) {
                Intent loginIntent = new Intent(context, LogIn.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(loginIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                boolean forcePullActivate = sharedPreferences.getBoolean("force_pull", false);
                if (forcePullActivate) {
                    Intent forcePullIntent = new Intent(context, ForcePull.class);
                    forcePullIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(forcePullIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    boolean updateData = sharedPreferences.getBoolean("update_data", false);
                    if (updateData) {
                        Intent updateDataIntent = new Intent(context, UpdateData.class);
                        updateDataIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(updateDataIntent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        boolean isServiceRunning = sharedPreferences.getBoolean("service_running", false);
                        if (!isServiceRunning) {
                            Intent serviceIntent = new Intent(getBaseContext(), SyncService.class);
                            ContextCompat.startForegroundService(getBaseContext(), serviceIntent);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onFilterSelect(String filterItem) {
        filterSelection = filterItem;
        if (!searcheditText.getText().toString().trim().matches("")) {
            showCustomer(getSelection(filterItem), new String[]{searcheditText.getText().toString().trim() + "%"});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        hideViews();
    }

    private void hideViews() {

        LinearLayout custDayContainer, barcodeCont, viewallCont, manageCont, addCont;

        custDayContainer = findViewById(R.id.cust_day_cont);
        barcodeCont = findViewById(R.id.cust_barcode_cont);
        viewallCont = findViewById(R.id.cust_view_all_cont);
        manageCont = findViewById(R.id.cuust_manage_cont);
        addCont = findViewById(R.id.cust_add_cont);

        custDayContainer.setVisibility(View.INVISIBLE);
        barcodeCont.setVisibility(View.INVISIBLE);
        viewallCont.setVisibility(View.INVISIBLE);
        manageCont.setVisibility(View.INVISIBLE);
        addCont.setVisibility(View.INVISIBLE);
        searcheditText.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideViews();
        final LinearLayout custDayContainer, barcodeCont, viewallCont, manageCont, addCont;

        custDayContainer = findViewById(R.id.cust_day_cont);
        barcodeCont = findViewById(R.id.cust_barcode_cont);
        viewallCont = findViewById(R.id.cust_view_all_cont);
        manageCont = findViewById(R.id.cuust_manage_cont);
        addCont = findViewById(R.id.cust_add_cont);


        YoYo.with(Techniques.SlideInLeft).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                custDayContainer.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewallCont.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInLeft).duration(300).repeat(0).playOn(viewallCont);

                        manageCont.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(300).repeat(0).playOn(manageCont);

                        barcodeCont.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInLeft).duration(300).repeat(0).playOn(barcodeCont);

                        addCont.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                searcheditText.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        showCustomer(FabizContract.Customer.COLUMN_DAY + "=?", new String[]{getCurrentDay()});
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).duration(400).repeat(0).playOn(searcheditText);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).duration(300).repeat(0).playOn(addCont);




                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).duration(400).repeat(0).playOn(custDayContainer);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(500).repeat(0).playOn(searcheditText);

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
