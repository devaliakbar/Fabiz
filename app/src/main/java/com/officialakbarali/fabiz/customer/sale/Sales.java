package com.officialakbarali.fabiz.customer.sale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.officialakbarali.fabiz.LogIn;
import com.officialakbarali.fabiz.data.barcode.FabizBarcode;
import com.officialakbarali.fabiz.item.data.ItemDetail;
import com.officialakbarali.fabiz.item.data.UnitData;
import com.officialakbarali.fabiz.network.syncInfo.SetupSync;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.sale.adapter.SalesAdapter;
import com.officialakbarali.fabiz.customer.sale.data.Cart;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.item.Item;
import com.officialakbarali.fabiz.printer.BPrinter;
import com.officialakbarali.fabiz.printer.DeviceList;

import java.io.IOException;
import java.math.BigInteger;
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
import static com.officialakbarali.fabiz.data.barcode.FabizBarcode.FOR_ITEM;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_CODE_PAY;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_CODE_SALE;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_CODE_SALE_PAID_STACK;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_INSERT;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_UPDATE;


public class Sales extends AppCompatActivity implements SalesAdapter.SalesAdapterOnClickListener {
    public static List<Cart> cartItems;
    private Toast toast;
    private String custId;

    private TextView dateView, totQtyView, totalView;
    String fromDateTime, currentTime;

    private SalesAdapter salesAdapter;

    int totQtyForSave;
    double totAmountToSave;

    double dueAmtPassed, totalDueAmnt;

    TextView currentDueAmntV, totalDueAmntV;


    EditText amtEditText;

    RecyclerView recyclerView;

    ImageButton addItemButton, showBarCoder, saveButton;
    String idToInsertBill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }

        custId = getIntent().getStringExtra("id");
        dueAmtPassed = Double.parseDouble(getIntent().getStringExtra("custDueAmt"));

        currentDueAmntV = findViewById(R.id.cust_sale_curr_due);
        currentDueAmntV.setText("Previous Due Amount :" + TruncateDecimal(dueAmtPassed + "") + " " + getCurrency());
        totalDueAmntV = findViewById(R.id.cust_sale_tot_due);

        totQtyView = findViewById(R.id.cust_sale_tot_qty);
        totalView = findViewById(R.id.cust_sale_total);

        amtEditText = findViewById(R.id.cust_sale_amnt);
        amtEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                totalDueAmnt = totAmountToSave + dueAmtPassed;
                double amntDisplayed = totalDueAmnt - getEnteredAmnt();
                totalDueAmntV.setText("Total Due Amount :" + TruncateDecimal(amntDisplayed + ""));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dateView = findViewById(R.id.cust_sale_time);
        try {
            currentTime = convertDateToDisplayFormat(getCurrentDateTime());
            dateView.setText(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addItemButton = findViewById(R.id.cust_sale_add_item);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickItem = new Intent(Sales.this, Item.class);
                pickItem.putExtra("fromSales", true);
                startActivity(pickItem);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        showBarCoder = findViewById(R.id.cust_sale_barcode);
        showBarCoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanFromBarcodeIntent = new Intent(Sales.this, FabizBarcode.class);
                scanFromBarcodeIntent.putExtra("FOR_WHO", FOR_ITEM + "");
                startActivity(scanFromBarcodeIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        saveButton = findViewById(R.id.cust_sale_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItems.isEmpty()) {
                    showToast("Please Add Item");
                    return;
                }
                if (getEnteredAmnt() < 0) {
                    showToast("Please enter a valid amount");
                    return;
                }
                if (getEnteredAmnt() > totalDueAmnt) {
                    showToast("Entered Amount is greater than total due amount");
                    return;
                }

                saveThisBill();
            }
        });

        recyclerView = findViewById(R.id.cust_sale_recycler);
        salesAdapter = new SalesAdapter(this, this, false, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(salesAdapter);

        EditText prefixE = findViewById(R.id.cust_sale_prefix);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String salesPrefix = sharedPreferences.getString("sales_prefix", "A");
        prefixE.setText(salesPrefix);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setTotalAndTotalQuantity();
        setUpAnimation();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (btsocket != null) {
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onClick(boolean modificationFlag, int indexToBeRemoved, Cart cartItemsF) {
        if (modificationFlag) {
            setUpCartModification(indexToBeRemoved, cartItems.get(indexToBeRemoved));
        } else {
            cartItems.remove(indexToBeRemoved);
            setUpAdapterData();
        }
    }

    private void setUpAdapterData() {
        salesAdapter.swapAdapter(cartItems);
        if (cartItems.size() > 0) {
            displayEmptyView(false);
        } else {
            displayEmptyView(true);
        }
        setTotalAndTotalQuantity();
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(GET_DATE_FORMAT_REAL());
        return sdf.format(new Date());
    }

    private void showDateTimePicker() {
        fromDateTime = "";
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
                        TimePickerDialog timePickerDialog = new TimePickerDialog(Sales.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        //*************************

                                        fromDateTime = year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth) + "T";
                                        fromDateTime += String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":30Z";
                                        try {
                                            currentTime = convertDateToDisplayFormat(fromDateTime);
                                            dateView.setText(currentTime);
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

    private void setTotalAndTotalQuantity() {
        totAmountToSave = 0;
        totQtyForSave = 0;
        for (int i = 0; i < cartItems.size(); i++) {
            Cart cart = cartItems.get(i);
            totAmountToSave += cart.getTotal();
            totQtyForSave += cart.getQty();
        }
        totQtyView.setText("Total Item :" + TruncateDecimal(totQtyForSave + ""));
        totalView.setText("Total :" + TruncateDecimal(totAmountToSave + "") + " " + getCurrency());

        totalDueAmnt = totAmountToSave + dueAmtPassed;
        double amntDisplayed = totalDueAmnt - getEnteredAmnt();
        totalDueAmntV.setText("*Total Due Amount :" + TruncateDecimal(amntDisplayed + "") + " " + getCurrency());
    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
    }

    private void saveThisBill() {
        boolean paymentStackFlag = false;
        double enteredAmntForUpdate = getEnteredAmnt();
        double currentBillMaxAmt = enteredAmntForUpdate;
        FabizProvider provider = new FabizProvider(this, true);
        try {
            //********TRANSACTION STARTED
            provider.createTransaction();
            List<SyncLogDetail> syncLogList = new ArrayList<>();
            if (enteredAmntForUpdate > 0) {
                //SET BALANCE AMOUNT TO ANOTHER BILL
                if (enteredAmntForUpdate > totAmountToSave) {
                    currentBillMaxAmt = totAmountToSave;
                    syncLogList = setUpBalanceAmntToAnotherBill(provider, syncLogList, enteredAmntForUpdate - totAmountToSave);
                    if (syncLogList == null) {
                        provider.finishTransaction();
                        return;
                    }
                    paymentStackFlag= true;
                }
            }
            double dueAmount = totAmountToSave - currentBillMaxAmt;

            EditText prefixE = findViewById(R.id.cust_sale_prefix);
            String prefix = prefixE.getText().toString();

            if (prefix.matches("")) {
                prefix = "A";
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("sales_prefix", prefix);
            editor.apply();

            idToInsertBill = provider.getIdForInsert(FabizContract.BillDetail.TABLE_NAME, prefix);
            if (idToInsertBill.matches("-1")) {
                showToast("Maximum limit of offline mode reached,please contact customer support");
                return;
            }

           // idToInsertBill = prefix + idToInsertBill;

            ContentValues billValues = new ContentValues();
            billValues.put(FabizContract.BillDetail._ID, idToInsertBill);
            billValues.put(FabizContract.BillDetail.COLUMN_CUST_ID, custId);
            billValues.put(FabizContract.BillDetail.COLUMN_DATE, currentTime);
            billValues.put(FabizContract.BillDetail.COLUMN_QTY, totQtyForSave);
            billValues.put(FabizContract.BillDetail.COLUMN_PRICE, totAmountToSave);
            billValues.put(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL, "0");
            billValues.put(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, totAmountToSave);
            billValues.put(FabizContract.BillDetail.COLUMN_PAID, currentBillMaxAmt);
            billValues.put(FabizContract.BillDetail.COLUMN_DUE, dueAmount);
            billValues.put(FabizContract.BillDetail.COLUMN_DISCOUNT, "0");


            String billId = provider.insert(FabizContract.BillDetail.TABLE_NAME, billValues) + "";

            if (Long.parseLong(billId) > 0) {
                billId = idToInsertBill;
                syncLogList.add(new SyncLogDetail(billId, FabizContract.BillDetail.TABLE_NAME, OP_INSERT));


                int i = 0;
                String idOfEachCartS = provider.getIdForInsert(FabizContract.Cart.TABLE_NAME, "");
                if (idOfEachCartS.matches("-1")) {
                    provider.finishTransaction();
                    showToast("Maximum limit of offline mode reached,please contact customer support");
                    return;
                }

                BigInteger idOfEachCart = new BigInteger(idOfEachCartS);
                idOfEachCart = idOfEachCart.subtract(new BigInteger("1"));
                while (i < cartItems.size()) {
                    Cart cartI = cartItems.get(i);

                    idOfEachCart = idOfEachCart.add(new BigInteger("1"));

                    ContentValues cartItemsValues = new ContentValues();
                    cartItemsValues.put(FabizContract.Cart._ID, idOfEachCart.toString());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_BILL_ID, billId);
                    cartItemsValues.put(FabizContract.Cart.COLUMN_ITEM_ID, cartI.getItemId());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_UNIT_ID, cartI.getUnitId());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_NAME, cartI.getName());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_BRAND, cartI.getBrand());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_CATEGORY, cartI.getCategory());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_PRICE, cartI.getPrice());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_QTY, cartI.getQty());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_TOTAL, cartI.getTotal());
                    cartItemsValues.put(FabizContract.Cart.COLUMN_RETURN_QTY, cartI.getReturnQty());

                    long cartInsertId = provider.insert(FabizContract.Cart.TABLE_NAME, cartItemsValues);

                    if (cartInsertId > 0) {
                        syncLogList.add(new SyncLogDetail(idOfEachCart + "", FabizContract.Cart.TABLE_NAME, OP_INSERT));
                    } else {
                        break;
                    }
                    i++;
                }

                if (i == cartItems.size()) {

                    long insertIdPayment;
                    if (enteredAmntForUpdate != 0) {
                        String idToInsertPayment = provider.getIdForInsert(FabizContract.Payment.TABLE_NAME, "");
                        if (idToInsertPayment.matches("-1")) {
                            provider.finishTransaction();
                            showToast("Maximum limit of offline mode reached,please contact customer support");
                            return;
                        }
                        ContentValues logTranscValues = new ContentValues();
                        logTranscValues.put(FabizContract.Payment._ID, idToInsertPayment);
                        logTranscValues.put(FabizContract.Payment.COLUMN_BILL_ID, billId);
                        logTranscValues.put(FabizContract.Payment.COLUMN_DATE, currentTime);
                        logTranscValues.put(FabizContract.Payment.COLUMN_AMOUNT, currentBillMaxAmt);
                        logTranscValues.put(FabizContract.Payment.COLUMN_TYPE, "P");
                        insertIdPayment = provider.insert(FabizContract.Payment.TABLE_NAME, logTranscValues);
                        if (insertIdPayment > 0) {
                            syncLogList.add(new SyncLogDetail(idToInsertPayment + "", FabizContract.Payment.TABLE_NAME, OP_INSERT));
                        }
                    } else {
                        insertIdPayment = 1;
                    }
                    if (insertIdPayment > 0) {

                        //DONE**********************************************
                        if(paymentStackFlag){
                            new SetupSync(this, syncLogList, provider, "Successfully Saved.", OP_CODE_SALE_PAID_STACK);
                        }else{
                            new SetupSync(this, syncLogList, provider, "Successfully Saved.", OP_CODE_SALE);
                        }

                        showDialogueInfo(totAmountToSave, enteredAmntForUpdate, currentBillMaxAmt);

                    } else {
                        provider.finishTransaction();
                        showToast("Something went wrong");
                    }


                } else {
                    provider.finishTransaction();
                    showToast("Failed to save");
                }

            } else {
                provider.finishTransaction();
                showToast("Failed to save");
            }

        } catch (Error error) {
            provider.finishTransaction();
            showToast("Failed to save");
        }
    }

    private void showDialogueInfo(final double billAmt, final double entAmt, double cBillEnteredAmnt) {
        ///todo
        double dueAmt = billAmt - cBillEnteredAmnt;
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_for_sale_and_payment_success);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });


        //SETTING SCREEN WIDTH
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************
        final double currentDue = (dueAmtPassed + billAmt) - entAmt;

        Button printButton = dialog.findViewById(R.id.pop_up_for_payment_okay);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btsocket == null) {
                    Intent BTIntent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(BTIntent, DeviceList.REQUEST_CONNECT_BT);
                } else {
                    BPrinter printer = new BPrinter(btsocket, Sales.this);
                    FabizProvider provider = new FabizProvider(Sales.this, false);
                    Cursor cursor = provider.query(FabizContract.Customer.TABLE_NAME, new String[]{FabizContract.Customer.COLUMN_SHOP_NAME,
                                    FabizContract.Customer.COLUMN_VAT_NO,
                                    FabizContract.Customer.COLUMN_ADDRESS_AREA,
                                    FabizContract.Customer.COLUMN_ADDRESS_ROAD,
                                    FabizContract.Customer.COLUMN_ADDRESS_BLOCK,
                                    FabizContract.Customer.COLUMN_ADDRESS_SHOP_NUM
                            },
                            FabizContract.Customer._ID + "=?", new String[]{custId}, null);
                    if (cursor.moveToNext()) {
                        String addressForInvoice = cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_ADDRESS_AREA));
                        if (!cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_ADDRESS_ROAD)).matches("NA")) {
                            addressForInvoice += ", " + cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_ADDRESS_ROAD));
                        }
                        if (!cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_ADDRESS_BLOCK)).matches("NA")) {
                            addressForInvoice += ", " + cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_ADDRESS_BLOCK));
                        }
                        if (!cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_ADDRESS_SHOP_NUM)).matches("NA")) {
                            addressForInvoice += ", " + cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_ADDRESS_SHOP_NUM));
                        }

                        printer.printInvoice(idToInsertBill, currentTime, cartItems, billAmt + "", entAmt + "", currentDue + "",
                                cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_SHOP_NAME)), addressForInvoice,
                                cursor.getString(cursor.getColumnIndex(FabizContract.Customer.COLUMN_VAT_NO)));
                    } else {
                        showToast("Something went wrong, can't print right now");
                    }
                }
            }
        });
        printButton.setText("Print");

        final LinearLayout billAmntContainer = dialog.findViewById(R.id.pop_up_for_payment_bill_amt_cont);
        billAmntContainer.setVisibility(View.VISIBLE);

        final TextView billAmntV = dialog.findViewById(R.id.pop_up_for_payment_bill_amt);
        //      final TextView dateV = dialog.findViewById(R.id.pop_up_for_payment_date);
        final TextView enteredAmntV = dialog.findViewById(R.id.pop_up_for_payment_ent_amt);

        final TextView dueAmtV = dialog.findViewById(R.id.pop_up_for_payment_due);

        TextView dueLabelText = dialog.findViewById(R.id.pop_up_for_payment_due_label);
        dueLabelText.setText("Bill Due Amount");


        LinearLayout totDueCont = dialog.findViewById(R.id.pop_up_for_payment_due_cont);
        totDueCont.setVisibility(View.VISIBLE);
        TextView totDueAmnt = dialog.findViewById(R.id.pop_up_for_payment_due_tot);
        totDueAmnt.setText(TruncateDecimal(currentDue + "") + " " + getCurrency());

        Button addPaymentBtn = dialog.findViewById(R.id.pop_up_for_payment_add_pay);
        addPaymentBtn.setVisibility(View.VISIBLE);
        addPaymentBtn.setText("Done");
        addPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        billAmntV.setText(TruncateDecimal(billAmt + "") + " " + getCurrency());
        //       dateV.setText( currentTime);
        enteredAmntV.setText(TruncateDecimal(entAmt + "") + " " + getCurrency());
        dueAmtV.setText(TruncateDecimal(dueAmt + "") + " " + getCurrency());

        dialog.show();
    }

    private double getEnteredAmnt() {
        if (amtEditText.getText().toString().trim().matches("") || amtEditText.getText().toString().trim().matches("-")) {
            return 0;
        } else {
            try {
                return Double.parseDouble(amtEditText.getText().toString().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
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
        hideViews();
    }

    private void hideViews() {
        LinearLayout emptyView = findViewById(R.id.empty_view);
        LinearLayout prefixCont, columnCont, addCont, barcodeCont, saveCont;
        prefixCont = findViewById(R.id.prefix_cont);
        columnCont = findViewById(R.id.column_name_cont);

        addCont = findViewById(R.id.add_cont);
        barcodeCont = findViewById(R.id.barcode_cont);
        saveCont = findViewById(R.id.saveCont);

        prefixCont.setVisibility(View.INVISIBLE);
        columnCont.setVisibility(View.INVISIBLE);

        dateView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        totQtyView.setVisibility(View.INVISIBLE);
        totalView.setVisibility(View.INVISIBLE);
        currentDueAmntV.setVisibility(View.INVISIBLE);
        totalDueAmntV.setVisibility(View.INVISIBLE);
        amtEditText.setVisibility(View.INVISIBLE);

        emptyView.setVisibility(View.GONE);
        salesAdapter.swapAdapter(new ArrayList<Cart>());

        addCont.setVisibility(View.INVISIBLE);
        barcodeCont.setVisibility(View.INVISIBLE);
        saveCont.setVisibility(View.INVISIBLE);

        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideViews();

        final LinearLayout prefixCont, columnCont, addCont, barcodeCont, saveCont;
        prefixCont = findViewById(R.id.prefix_cont);
        columnCont = findViewById(R.id.column_name_cont);
        addCont = findViewById(R.id.add_cont);
        barcodeCont = findViewById(R.id.barcode_cont);
        saveCont = findViewById(R.id.saveCont);

        YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                prefixCont.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInLeft).duration(300).repeat(0).playOn(prefixCont);

                dateView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInRight).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        columnCont.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                salesAdapter.swapAdapter(cartItems);
                                if (cartItems.size() > 0) {
                                    displayEmptyView(false);
                                } else {
                                    displayEmptyView(true);
                                }

                                totQtyView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInLeft).duration(300).repeat(0).playOn(totQtyView);
                                totalView.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(totalView);
                                currentDueAmntV.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInRight).withListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        amtEditText.setVisibility(View.VISIBLE);
                                        YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                totalDueAmntV.setVisibility(View.VISIBLE);

                                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(totalDueAmntV);
                                                addCont.setVisibility(View.VISIBLE);
                                                YoYo.with(Techniques.FadeInLeft).duration(300).repeat(0).playOn(addCont);
                                                saveCont.setVisibility(View.VISIBLE);
                                                YoYo.with(Techniques.FadeInRight).duration(300).repeat(0).playOn(saveCont);
                                                barcodeCont.setVisibility(View.VISIBLE);
                                                YoYo.with(Techniques.FadeInUp).duration(300).repeat(0).playOn(barcodeCont);
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animation) {

                                            }
                                        }).duration(300).repeat(0).playOn(amtEditText);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).duration(300).repeat(0).playOn(currentDueAmntV);

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).duration(300).repeat(0).playOn(columnCont);
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


    private void displayEmptyView(boolean setOn) {
        LinearLayout emptyView = findViewById(R.id.empty_view);
        if (setOn) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private BluetoothSocket btsocket;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            btsocket = DeviceList.getSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<SyncLogDetail> setUpBalanceAmntToAnotherBill(FabizProvider provider, List<SyncLogDetail> syncLogList, double balAmountToAddAnotherBill) {

        Cursor balBill = provider.query(FabizContract.BillDetail.TABLE_NAME, new String[]
                        {FabizContract.BillDetail._ID, FabizContract.BillDetail.COLUMN_PAID, FabizContract.BillDetail.COLUMN_DUE},
                FabizContract.BillDetail.COLUMN_DUE + " > ? AND " + FabizContract.BillDetail.FULL_COLUMN_CUST_ID + "=?", new String[]{"0", custId}, FabizContract.BillDetail._ID + " ASC");

        while (balBill.moveToNext()) {
            double cBillPaidAmnt;
            double cBillDueAmt = balBill.getDouble(balBill.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DUE));
            if (cBillDueAmt <= balAmountToAddAnotherBill) {
                cBillPaidAmnt = cBillDueAmt;
                cBillDueAmt = 0;
                balAmountToAddAnotherBill -= cBillPaidAmnt;
            } else {
                cBillPaidAmnt = balAmountToAddAnotherBill;
                cBillDueAmt -= balAmountToAddAnotherBill;
                balAmountToAddAnotherBill = 0;
            }

            String cbillId = balBill.getString(balBill.getColumnIndexOrThrow(FabizContract.BillDetail._ID));

            String idToInsertPayment = provider.getIdForInsert(FabizContract.Payment.TABLE_NAME, "");
            if (idToInsertPayment.matches("-1")) {
                return null;
            }

            ContentValues logTranscValues = new ContentValues();
            logTranscValues.put(FabizContract.Payment._ID, idToInsertPayment);
            logTranscValues.put(FabizContract.Payment.COLUMN_BILL_ID, cbillId);
            logTranscValues.put(FabizContract.Payment.COLUMN_DATE, currentTime);
            logTranscValues.put(FabizContract.Payment.COLUMN_AMOUNT, cBillPaidAmnt);
            logTranscValues.put(FabizContract.Payment.COLUMN_TYPE, "P");

            long insertIdPayment = provider.insert(FabizContract.Payment.TABLE_NAME, logTranscValues);


            if (insertIdPayment > 0) {
                syncLogList.add(new SyncLogDetail(idToInsertPayment + "", FabizContract.Payment.TABLE_NAME, OP_INSERT));
                double upPaidAmount = balBill.getDouble(balBill.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PAID));
                upPaidAmount += cBillPaidAmnt;

                ContentValues accUpValues = new ContentValues();
                accUpValues.put(FabizContract.BillDetail.COLUMN_PAID, upPaidAmount);
                accUpValues.put(FabizContract.BillDetail.COLUMN_DUE, cBillDueAmt);
                int upAffectedRows = provider.update(FabizContract.BillDetail.TABLE_NAME, accUpValues,
                        FabizContract.BillDetail._ID + "=?", new String[]{cbillId + ""});

                if (upAffectedRows == 1) {
                    syncLogList.add(new SyncLogDetail(cbillId, FabizContract.BillDetail.TABLE_NAME, OP_UPDATE));
                } else {
                    showToast("Something went wrong");
                    return null;
                }
            } else {
                showToast("Failed to save");
                return null;
            }
            if (balAmountToAddAnotherBill == 0) {
                break;
            }
        }
        return syncLogList;
    }


    private void setUpCartModification(final int indexToBeDelete, final Cart itemDetail) {

        final double currentItemPrice = fillUnitDataAndGetPrice(itemDetail);

        if (currentItemPrice == 0) {
            showToast("Something went wrong");
            return;
        }

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
        priceText.setText(TruncateDecimal(currentItemPrice + ""));
        final EditText quantityText = dialog.findViewById(R.id.cust_sale_add_item_qty);
        quantityText.setText("1");
        final TextView totalText = dialog.findViewById(R.id.cust_sale_add_item_total);
        totalText.setText(TruncateDecimal(currentItemPrice + ""));

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
                    cartItems.remove(indexToBeDelete);
                    cartItems.add(new Cart("0", "0", itemDetail.getItemId(), myUnitData.getId(), myUnitData.getUnitName(), itemDetail.getName(), itemDetail.getBrand(), itemDetail.getCategory(),
                            Double.parseDouble(priceS), Integer.parseInt(qtyS), Double.parseDouble(totS), 0));
                    setUpAdapterData();
                    dialog.dismiss();
                } else {
                    showToast("Please enter valid number");
                }
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
                double iPrice = currentItemPrice * myUnitData.getQty();
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

    private double fillUnitDataAndGetPrice(Cart itemsDetailForOp) {
        FabizProvider providerForUnitFetch = new FabizProvider(this, false);
        Cursor cursor = providerForUnitFetch.query(FabizContract.ItemUnit.TABLE_NAME, new String[]{FabizContract.ItemUnit.FULL_COLUMN_ID, FabizContract.ItemUnit.COLUMN_UNIT_NAME,
                FabizContract.ItemUnit.COLUMN_QTY}, null, null, null);
        unitData = new ArrayList<>();
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit._ID)).matches(itemsDetailForOp.getUnitId())) {
                unitData.add(0, new UnitData(cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_UNIT_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_QTY))));
            } else {
                unitData.add(new UnitData(cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_UNIT_NAME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_QTY))));
            }
        }
        myUnitData = unitData.get(0);

        cursor = providerForUnitFetch.query(FabizContract.Item.TABLE_NAME, new String[]{FabizContract.Item.COLUMN_PRICE, FabizContract.Item.COLUMN_UNIT_ID}, FabizContract.Item._ID + "=?",
                new String[]{itemsDetailForOp.getItemId()}, null);
        if (cursor.moveToNext()) {
            return cursor.getDouble(cursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_PRICE));
        }
        return 0;
    }

    List<UnitData> unitData;

    UnitData myUnitData;

    private boolean conditionsForDialogue(String s1, String s2, String s3) {
        if (s1.matches("") || s2.matches("") ||
                s3.matches("")) {
            return false;
        } else {
            try {
                double priceToCart = Double.parseDouble(s1);
                int quantityToCart = Integer.parseInt(s2);
                double totalToCart = Double.parseDouble(s3);

                if (priceToCart > 0 && quantityToCart > 0 && totalToCart > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
