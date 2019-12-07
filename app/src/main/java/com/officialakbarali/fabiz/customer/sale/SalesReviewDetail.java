package com.officialakbarali.fabiz.customer.sale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.sale.adapter.SalesAdapter;
import com.officialakbarali.fabiz.customer.sale.data.Cart;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.item.data.UnitData;
import com.officialakbarali.fabiz.network.syncInfo.SetupSync;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.officialakbarali.fabiz.data.CommonInformation.GET_DATE_FORMAT_REAL;
import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.convertDateToDisplayFormat;
import static com.officialakbarali.fabiz.data.CommonInformation.getCurrency;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_CODE_SALE_RETURN;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_INSERT;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_UPDATE;

public class SalesReviewDetail extends AppCompatActivity implements SalesAdapter.SalesAdapterOnClickListener {
    private Toast toast;
    private String custId, billId;

    private TextView dateView, totQtyView, totalView, billIdView, discountView, returnView, currentView, paidView, dueView;
    FabizProvider fabizProvider;

    private SalesAdapter salesAdapter;

    private boolean FROM_SALES_RETURN = false;
    RecyclerView recyclerView;

    //IF DUE BECOME NEGATIVE AFTER RETURN AN ITEM
    private boolean NEGATIVE_DUE = false;

    //***************************FOR DIALOGUE***************************
    private String DcurrentTime, DfromDateTime;
    private TextView DdateTextP;

    private Dialog paymentDialog;

    List<UnitData> unitData;

    UnitData myUnitData, currentUnitData;
    int indexOfdCurrentUnit;
    double currentMaxLimit;

    //******************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_review_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }

        fillUnitData();

        FROM_SALES_RETURN = getIntent().getBooleanExtra("fromSalesReturn", false);

        fabizProvider = new FabizProvider(this, false);

        discountView = findViewById(R.id.cust_sale_disc);

        billIdView = findViewById(R.id.cust_sale_billId);
        totQtyView = findViewById(R.id.cust_sale_tot_qty);
        totalView = findViewById(R.id.cust_sale_total);
        dateView = findViewById(R.id.cust_sale_time);

        returnView = findViewById(R.id.cust_sale_return);
        dueView = findViewById(R.id.cust_sale_due);
        currentView = findViewById(R.id.cust_sale_current);
        paidView = findViewById(R.id.cust_sale_paid);

        custId = getIntent().getStringExtra("custId");
        billId = getIntent().getStringExtra("billId");

        recyclerView = findViewById(R.id.cust_sale_recycler);


        if (FROM_SALES_RETURN) {
            salesAdapter = new SalesAdapter(this, this, true, true);
        } else {
            salesAdapter = new SalesAdapter(this, this, true, false);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(salesAdapter);

        setUpBillDetail();

        if (FROM_SALES_RETURN) setUpThisPageForReturn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }

    private void setUpThisPageForReturn() {
        //TODO IF ANYTHING NEED TO BE CHANGE IN FUTURE FOR SALES RETURN
        ImageButton returnButton = findViewById(R.id.return_icon);
        returnButton.setVisibility(View.INVISIBLE);
    }

    private void setUpBillDetail() {

        Cursor billCursor = fabizProvider.query(FabizContract.BillDetail.TABLE_NAME,
                new String[]{FabizContract.BillDetail._ID, FabizContract.BillDetail.COLUMN_QTY,
                        FabizContract.BillDetail.COLUMN_DATE
                        , FabizContract.BillDetail.COLUMN_PRICE
                        , FabizContract.BillDetail.COLUMN_RETURNED_TOTAL
                        , FabizContract.BillDetail.COLUMN_PAID
                        , FabizContract.BillDetail.COLUMN_DUE
                        , FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, FabizContract.BillDetail.COLUMN_DISCOUNT}
                , FabizContract.BillDetail._ID + "=?", new String[]{billId + ""}
                , null);

        if (billCursor.moveToNext()) {
            billIdView.setText("Bill Id : " + billId);

            dateView.setText(billCursor.getString(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DATE)));

            totQtyView.setText("Total Item : " + billCursor.getInt(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_QTY)));
            totalView.setText("Total : " + TruncateDecimal(billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PRICE)) + "") + " " + getCurrency());
            discountView.setText("Discount On Due : " + TruncateDecimal(billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DISCOUNT)) + "") + " " + getCurrency());

            currentView.setText("Current Total : " + TruncateDecimal(billCursor.getInt(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL)) + "") + " " + getCurrency());
            paidView.setText("Paid Amount : " + TruncateDecimal(billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PAID)) + "") + " " + getCurrency());
            returnView.setText("Returned Amount : " + TruncateDecimal(billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL)) + "") + " " + getCurrency());
            dueView.setText("Due Amount : " + TruncateDecimal(billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DUE)) + "") + " " + getCurrency());
        }
    }

    private void setUpBillItems() {
        List<Cart> cartItems = new ArrayList<>();

        Cursor billItemsCursor = fabizProvider.query(FabizContract.Cart.TABLE_NAME
                        + " INNER JOIN " + FabizContract.ItemUnit.TABLE_NAME + " ON " + FabizContract.Cart.FULL_COLUMN_UNIT_ID +
                        " = " + FabizContract.ItemUnit.FULL_COLUMN_ID
                , new String[]{
                        FabizContract.Cart.FULL_COLUMN_ID, FabizContract.Cart.FULL_COLUMN_BILL_ID, FabizContract.Cart.FULL_COLUMN_ITEM_ID,
                        FabizContract.Cart.FULL_COLUMN_UNIT_ID, FabizContract.Cart.FULL_COLUMN_NAME, FabizContract.Cart.FULL_COLUMN_BRAND,
                        FabizContract.Cart.FULL_COLUMN_CATAGORY,
                        FabizContract.Cart.FULL_COLUMN_PRICE, FabizContract.Cart.FULL_COLUMN_QTY,
                        FabizContract.ItemUnit.FULL_COLUMN_UNIT_NAME, FabizContract.Cart.FULL_COLUMN_TOTAL, FabizContract.Cart.FULL_COLUMN_RETURN_QTY
                }, FabizContract.Cart.FULL_COLUMN_BILL_ID + "=?",
                new String[]{billId + ""}, null);

        while (billItemsCursor.moveToNext()) {
            cartItems.add(new Cart(
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart._ID)),
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_BILL_ID)),
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_ITEM_ID)),
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_UNIT_ID)),
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_UNIT_NAME)),
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_NAME)),
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_BRAND)),
                    billItemsCursor.getString(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_CATEGORY)),
                    billItemsCursor.getDouble(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_PRICE)),
                    billItemsCursor.getInt(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_QTY)),
                    billItemsCursor.getDouble(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_TOTAL)),
                    billItemsCursor.getDouble(billItemsCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_RETURN_QTY))
            ));
        }
        recyclerView.setVisibility(View.VISIBLE);
        salesAdapter.swapAdapter(cartItems);
    }

    @Override
    public void onClick(int indexToBeRemoved, Cart cartITemList) {
        if (FROM_SALES_RETURN) {
            setUpReturnPop(cartITemList);
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

    private void setmyUnitData(String unitIdCart) {
        for (int i = 0; i < unitData.size(); i++) {
            if (unitData.get(i).getId().matches(unitIdCart)) {
                myUnitData = unitData.get(i);
                currentUnitData = myUnitData;
                indexOfdCurrentUnit = i;
                break;
            }
        }
    }

    private void setUpReturnPop(final Cart cartITemList) {

        final TextView nameTextP, maxQtyP, totAmountP;
        final EditText priceTextP, qtyTextP;
        final Button returnB, cancelB, changeDateP;

        paymentDialog = new Dialog(this);
        paymentDialog.setContentView(R.layout.pop_up_sales_return_item);

        //SETTING SCREEN WIDTH
        paymentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = paymentDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************


        nameTextP = paymentDialog.findViewById(R.id.sales_return_pop_item_name);
        maxQtyP = paymentDialog.findViewById(R.id.sales_return_pop_max);

        priceTextP = paymentDialog.findViewById(R.id.sales_return_pop_price);
        qtyTextP = paymentDialog.findViewById(R.id.sales_return_pop_qty);
        totAmountP = paymentDialog.findViewById(R.id.sales_return_pop_total);

        returnB = paymentDialog.findViewById(R.id.sales_return_pop_remove);
        cancelB = paymentDialog.findViewById(R.id.sales_return_pop_cancel);

        DdateTextP = paymentDialog.findViewById(R.id.sales_return_pop_date);
        changeDateP = paymentDialog.findViewById(R.id.sales_return_pop_date_change);

        nameTextP.setText(cartITemList.getName());

        final double maxLimitOfReturn = cartITemList.getQty() - cartITemList.getReturnQty();
        currentMaxLimit = maxLimitOfReturn;

        maxQtyP.setText("Qty\n(Maximum " + maxLimitOfReturn + ")");

        priceTextP.setText(TruncateDecimal(cartITemList.getPrice() + ""));

        qtyTextP.setText("1");

        totAmountP.setText(TruncateDecimal("" + cartITemList.getPrice()));

        //*****************SETTING SPINNER (UNIT)

        setmyUnitData(cartITemList.getUnitId());
        List<String> spinnerData = new ArrayList<>();
        for (int i = 0; i < unitData.size(); i++) {
            UnitData temp = unitData.get(i);
            spinnerData.add(temp.getUnitName());
        }

        final Spinner unitS = paymentDialog.findViewById(R.id.spinner_unit);
        unitS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (unitData.get(position).getQty() <= myUnitData.getQty()) {
                    currentUnitData = unitData.get(position);
                } else {
                    unitS.setSelection(indexOfdCurrentUnit);
                    currentUnitData = myUnitData;
                    showToast("Unsupported unit");
                }
                double totalBaseQty = myUnitData.getQty() * cartITemList.getQty();

                double basePrice = cartITemList.getPrice() / myUnitData.getQty();


                double totalCurrentQty = totalBaseQty / currentUnitData.getQty();


                double totalReturnBaseQty = myUnitData.getQty() * cartITemList.getReturnQty();

                double totalReturnCurrentQty = totalReturnBaseQty / currentUnitData.getQty();


                currentMaxLimit = totalCurrentQty - totalReturnCurrentQty;

                maxQtyP.setText("Enter QTY to Return\n(Maximum " + TruncateDecimal(currentMaxLimit + "") + ")");


                double currentPrice = currentUnitData.getQty() * basePrice;
                priceTextP.setText(TruncateDecimal(currentPrice + ""));

                qtyTextP.setText("1");

                totAmountP.setText(TruncateDecimal("" + currentPrice));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.custom_spinner_item, spinnerData);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        unitS.setAdapter(spinnerAdapter);
        unitS.setSelection(indexOfdCurrentUnit);
        //*****************END

        qtyTextP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String priceS = priceTextP.getText().toString().trim();
                String qtyS = qtyTextP.getText().toString().trim();
                String totS = totAmountP.getText().toString().trim();

                if (conditionsForDialogue(priceS, qtyS, totS, currentMaxLimit)) {
                    double priceToCart = Double.parseDouble(priceS);
                    int quantityToCart = Integer.parseInt(qtyS);
                    double totalToCart = priceToCart * quantityToCart;
                    totAmountP.setText(TruncateDecimal(totalToCart + ""));
                }
            }
        });

        priceTextP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String priceS = priceTextP.getText().toString().trim();
                String qtyS = qtyTextP.getText().toString().trim();
                String totS = totAmountP.getText().toString().trim();

                if (conditionsForDialogue(priceS, qtyS, totS, currentMaxLimit)) {
                    double priceToCart = Double.parseDouble(priceS);
                    int quantityToCart = Integer.parseInt(qtyS);
                    double totalToCart = priceToCart * quantityToCart;
                    totAmountP.setText(TruncateDecimal(totalToCart + ""));
                }
            }
        });

//*****************************setting up date************************************
        try {
            DcurrentTime = convertDateToDisplayFormat(getCurrentDateTime());
            DdateTextP.setText(DcurrentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        changeDateP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });
//********************************************************************************

        cancelB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentDialog.dismiss();
            }
        });

        returnB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = validateAndReturnContentValues(DcurrentTime, cartITemList.getItemId(), currentUnitData.getId(), qtyTextP.getText().toString(), priceTextP.getText().toString(),
                        totAmountP.getText().toString(), currentMaxLimit);
                if (values != null) {
                    double basePrice = cartITemList.getPrice() / myUnitData.getQty();
                    double currentPrice = currentUnitData.getQty() * basePrice;
                    if (Double.parseDouble(priceTextP.getText().toString()) > currentPrice) {
                        showToast("Please enter the valid price for item");
                    } else {
                        saveThisReturnedItem(values, cartITemList.getId());
                    }
                }
            }
        });

        if (maxLimitOfReturn <= 0) {
            showToast("This is completely Returned");
        } else {
            paymentDialog.show();
        }
    }

    private ContentValues validateAndReturnContentValues(String dateR, String itemIdR, String unitIdR, String qtyR, String priceR, String totalR, double maxLimitOfReturn) {
        if (conditionsForDialogue(priceR, qtyR, totalR, maxLimitOfReturn)) {
            String idForInsert = fabizProvider.getIdForInsert(FabizContract.SalesReturn.TABLE_NAME, "");

            if (idForInsert.matches("-1")) {
                showToast("Max limit of offline operation reached.please contact customer care");
                return null;
            }

            ContentValues values = new ContentValues();
            values.put(FabizContract.SalesReturn._ID, idForInsert);
            values.put(FabizContract.SalesReturn.COLUMN_DATE, dateR);
            values.put(FabizContract.SalesReturn.COLUMN_BILL_ID, billId);
            values.put(FabizContract.SalesReturn.COLUMN_ITEM_ID, itemIdR);
            values.put(FabizContract.SalesReturn.COLUMN_UNIT_ID, unitIdR);
            values.put(FabizContract.SalesReturn.COLUMN_QTY, qtyR);
            values.put(FabizContract.SalesReturn.COLUMN_PRICE, priceR);
            values.put(FabizContract.SalesReturn.COLUMN_TOTAL, totalR);

            return values;
        }
        return null;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(GET_DATE_FORMAT_REAL());
        Log.i("Time:", sdf.format(new Date()));
        return sdf.format(new Date());
    }

    private void showDateTimePicker() {
        DfromDateTime = "";
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year,
                                          final int monthOfYear, final int dayOfMonth) {
                        final Calendar c = Calendar.getInstance();
                        int mHour = c.get(Calendar.HOUR_OF_DAY);
                        int mMinute = c.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(SalesReviewDetail.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        //*************************

                                        DfromDateTime = year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth) + "T";
                                        DfromDateTime += String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":30Z";
                                        try {
                                            DcurrentTime = convertDateToDisplayFormat(DfromDateTime);
                                            DdateTextP.setText(DcurrentTime);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        //*************************
                                    }
                                }, mHour, mMinute, false);
                        timePickerDialog.show();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
    }

    private boolean conditionsForDialogue(String s1, String s2, String s3, double maxLimitOfReturn) {
        if (s1.matches("") || s2.matches("") ||
                s3.matches("")) {
            showToast("Some fields are empty");
            return false;
        } else {
            try {
                double priceToCart = Double.parseDouble(s1);
                double quantityToCart = Double.parseDouble(s2);
                double totalToCart = Double.parseDouble(s3);


                if (quantityToCart > maxLimitOfReturn) {
                    showToast("Only " + maxLimitOfReturn + " Items Left");
                    return false;
                }

                if (priceToCart > 0 && quantityToCart > 0 && totalToCart > 0) {
                    return true;
                } else {
                    showToast("Invalid Number");
                    return false;
                }
            } catch (Error e) {
                showToast("Invalid Number");
                return false;
            }
        }
    }

    private void saveThisReturnedItem(ContentValues values, String cartId) {
        NEGATIVE_DUE = false;
        List<SyncLogDetail> syncLogList = new ArrayList<>();
        FabizProvider saveProvider = new FabizProvider(this, true);
        try {
            //********TRANSACTION STARTED
            saveProvider.createTransaction();
            long idOfSalesReturn = saveProvider.insert(FabizContract.SalesReturn.TABLE_NAME, values);

            if (idOfSalesReturn > 0) {

                syncLogList.add(new SyncLogDetail(values.get(FabizContract.SalesReturn._ID) + "", FabizContract.SalesReturn.TABLE_NAME, OP_INSERT));

                Cursor amountUpdateCursor = saveProvider.query(FabizContract.BillDetail.TABLE_NAME,
                        new String[]{FabizContract.BillDetail.COLUMN_CURRENT_TOTAL,
                                FabizContract.BillDetail.COLUMN_DISCOUNT,
                                FabizContract.BillDetail.COLUMN_RETURNED_TOTAL
                                , FabizContract.BillDetail.COLUMN_DUE}
                        , FabizContract.BillDetail._ID + "=?", new String[]{billId + ""}, null);

                if (amountUpdateCursor.moveToNext()) {


                    double totCurrentUpdate = amountUpdateCursor.getDouble(amountUpdateCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL));
                    double totReturnedUpdate = amountUpdateCursor.getDouble(amountUpdateCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL));
                    double dueUpdate = amountUpdateCursor.getDouble(amountUpdateCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DUE));
                    double discountUpdate = amountUpdateCursor.getDouble(amountUpdateCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DISCOUNT));

                    totReturnedUpdate += values.getAsDouble(FabizContract.SalesReturn.COLUMN_TOTAL);
                    totCurrentUpdate -= values.getAsDouble(FabizContract.SalesReturn.COLUMN_TOTAL);


                    if (discountUpdate > 0) {
                        if (discountUpdate >= values.getAsDouble(FabizContract.SalesReturn.COLUMN_TOTAL)) {
                            discountUpdate -= values.getAsDouble(FabizContract.SalesReturn.COLUMN_TOTAL);
                        } else {
                            dueUpdate = dueUpdate - (values.getAsDouble(FabizContract.SalesReturn.COLUMN_TOTAL) - discountUpdate);
                            discountUpdate = 0;
                        }
                    } else {
                        dueUpdate -= values.getAsDouble(FabizContract.SalesReturn.COLUMN_TOTAL);
                    }


                    if (dueUpdate < 0) {
                        NEGATIVE_DUE = true;
                    }

                    ContentValues accUpValues = new ContentValues();
                    accUpValues.put(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, totCurrentUpdate);
                    accUpValues.put(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL, totReturnedUpdate);
                    accUpValues.put(FabizContract.BillDetail.COLUMN_DUE, dueUpdate);
                    accUpValues.put(FabizContract.BillDetail.COLUMN_DISCOUNT, discountUpdate);

                    int upAffectedRows = saveProvider.update(FabizContract.BillDetail.TABLE_NAME, accUpValues,
                            FabizContract.BillDetail._ID + "=?", new String[]{billId + ""});
                    if (upAffectedRows == 1) {

                        syncLogList.add(new SyncLogDetail(billId, FabizContract.BillDetail.TABLE_NAME, OP_UPDATE));

                        Cursor returnUpdateToBillCursor = saveProvider.query(FabizContract.Cart.TABLE_NAME,
                                new String[]{FabizContract.Cart._ID, FabizContract.Cart.COLUMN_RETURN_QTY}
                                , FabizContract.Cart._ID + "=?",
                                new String[]{cartId}, null);

                        if (returnUpdateToBillCursor.moveToNext()) {

                            double retQtyUpdate = returnUpdateToBillCursor.getDouble(
                                    returnUpdateToBillCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_RETURN_QTY));

                            String idOfRowReturn = returnUpdateToBillCursor.getString(
                                    returnUpdateToBillCursor.getColumnIndexOrThrow(FabizContract.Cart._ID));

                            double baseReturnQty = values.getAsDouble(FabizContract.SalesReturn.COLUMN_QTY) * currentUnitData.getQty();
                            double currentBillUnitFormatQty = baseReturnQty / myUnitData.getQty();

                            retQtyUpdate += currentBillUnitFormatQty;

                            ContentValues billReturnUpValues = new ContentValues();
                            billReturnUpValues.put(FabizContract.Cart.COLUMN_RETURN_QTY, retQtyUpdate);

                            int upReturnAffectedRaw = saveProvider.update(FabizContract.Cart.TABLE_NAME, billReturnUpValues,
                                    FabizContract.Cart._ID + "=?",
                                    new String[]{idOfRowReturn + ""});

                            if (upReturnAffectedRaw > 0) {
                                syncLogList.add(new SyncLogDetail(idOfRowReturn + "", FabizContract.Cart.TABLE_NAME, OP_UPDATE));
                                new SetupSync(this, syncLogList, saveProvider, "Successfully Returned", OP_CODE_SALE_RETURN);

                                //END HERE *****************************************************
                                showFinalInfoDialogue(NEGATIVE_DUE, values.getAsDouble(FabizContract.SalesReturn.COLUMN_TOTAL),
                                        dueUpdate);
                            } else {
                                saveProvider.finishTransaction();
                                showToast("Something went wrong");
                                Log.e("SalesReview:", "Failed To Update Return");
                            }

                        } else {
                            saveProvider.finishTransaction();
                            showToast("Something went wrong");
                            Log.e("SalesReview:", "Cart Item Not Found");
                        }


                    } else {
                        saveProvider.finishTransaction();
                        showToast("Something went wrong");
                        Log.e("SalesReview:", "Due Failed To Update");
                    }

                } else {
                    saveProvider.finishTransaction();
                    showToast("Something went wrong");
                    Log.e("SalesReview:", "Amount Update Cursor Zero");
                }

            } else {
                saveProvider.finishTransaction();
                showToast("Failed to save");
            }
        } catch (Error error) {
            saveProvider.finishTransaction();
            showToast("Failed to save");
        }

    }

    private void showFinalInfoDialogue(boolean NEGATIVE_DUE, double returnedAmt, double dueAmnt) {
        //TODO NEGATIVE DUE WARNING

        paymentDialog.dismiss();
        final Dialog lastDialog = new Dialog(this);

        lastDialog.setContentView(R.layout.pop_up_for_sale_and_payment_success);


        //SETTING SCREEN WIDTH
        lastDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = lastDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************

        lastDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });

        Button okayButton = lastDialog.findViewById(R.id.pop_up_for_payment_okay);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastDialog.dismiss();
            }
        });


        final TextView dateV = lastDialog.findViewById(R.id.pop_up_for_payment_date);

        final TextView returnedAmntLabel = lastDialog.findViewById(R.id.pop_up_for_payment_label_ent_amt);
        returnedAmntLabel.setText("Returned Amount");

        final TextView returnedAmntV = lastDialog.findViewById(R.id.pop_up_for_payment_ent_amt);

        final TextView dueAmtV = lastDialog.findViewById(R.id.pop_up_for_payment_due);

        TextView dueLabelText = lastDialog.findViewById(R.id.pop_up_for_payment_due_label);
        dueLabelText.setText("Bill Due Amount");

        dateV.setText(": " + DcurrentTime);
        returnedAmntV.setText(": " + TruncateDecimal(returnedAmt + ""));

        dueAmtV.setText(": " + TruncateDecimal(dueAmnt + ""));

        lastDialog.show();
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
        LinearLayout columnNameFrame = findViewById(R.id.column_name_cont);

        billIdView.setVisibility(View.INVISIBLE);
        dateView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        columnNameFrame.setVisibility(View.INVISIBLE);

        totalView.setVisibility(View.INVISIBLE);
        totQtyView.setVisibility(View.INVISIBLE);
        returnView.setVisibility(View.INVISIBLE);
        discountView.setVisibility(View.INVISIBLE);
        currentView.setVisibility(View.INVISIBLE);
        paidView.setVisibility(View.INVISIBLE);
        dueView.setVisibility(View.INVISIBLE);

    }

    private void setUpAnimation() {
        hideViews();


        final LinearLayout columnNameFrame = findViewById(R.id.column_name_cont);

        YoYo.with(Techniques.FadeInLeft).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                billIdView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).duration(400).repeat(0).playOn(billIdView);
                dateView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        columnNameFrame.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setUpBillItems();

                                totQtyView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInLeft).duration(300).repeat(0).playOn(totQtyView);

                                totalView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(totalView);

                                returnView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(returnView);

                                discountView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(discountView);

                                currentView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(currentView);

                                paidView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(paidView);

                                dueView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(dueView);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).duration(300).repeat(0).playOn(columnNameFrame);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).duration(300).repeat(0).playOn(dateView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(400).repeat(0).playOn(recyclerView);

    }
}
