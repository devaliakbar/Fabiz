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
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.customer.payment.AddPayment;
import com.officialakbarali.fabiz.data.barcode.FabizBarcode;
import com.officialakbarali.fabiz.network.syncInfo.SetupSync;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.sale.adapter.SalesAdapter;
import com.officialakbarali.fabiz.customer.sale.data.Cart;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.item.Item;

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
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_CODE_SALE;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_INSERT;


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

    Intent addPaymentIntent = null;
    RecyclerView recyclerView;

    ImageButton addItemButton, showBarCoder, saveButton;

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

                if (getEnteredAmnt() == 0) {
                    saveThisBill();
                    return;
                }

                if (getEnteredAmnt() > totalDueAmnt) {
                    showToast("Entered Amount is greater than total due amount");
                    return;
                }

                if (getEnteredAmnt() > totAmountToSave) {
                    showToast("Entered amount is greater than bill amount");
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
        if (addPaymentIntent != null) {
            addPaymentIntent.putExtra("id", custId + "");
            startActivity(addPaymentIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void onClick(int indexToBeRemoved, Cart cartItemsF) {
        cartItems.remove(indexToBeRemoved);
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
        Log.i("Time:", sdf.format(new Date()));
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
        double enteredAmntForUpdate = getEnteredAmnt();
        double dueAmount = totAmountToSave - enteredAmntForUpdate;

        FabizProvider provider = new FabizProvider(this, true);


        EditText prefixE = findViewById(R.id.cust_sale_prefix);
        String prefix = prefixE.getText().toString();

        if (prefix.matches("")) {
            prefix = "A";
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sales_prefix", prefix);
        editor.apply();

        String idToInsertBill = provider.getIdForInsert(FabizContract.BillDetail.TABLE_NAME, prefix);
        if (idToInsertBill.matches("-1")) {
            showToast("Maximum limit of offline mode reached,please contact customer support");
            return;
        }

        idToInsertBill = prefix + idToInsertBill;

        ContentValues billValues = new ContentValues();
        billValues.put(FabizContract.BillDetail._ID, idToInsertBill);
        billValues.put(FabizContract.BillDetail.COLUMN_CUST_ID, custId);
        billValues.put(FabizContract.BillDetail.COLUMN_DATE, currentTime);
        billValues.put(FabizContract.BillDetail.COLUMN_QTY, totQtyForSave);
        billValues.put(FabizContract.BillDetail.COLUMN_PRICE, totAmountToSave);
        billValues.put(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL, "0");
        billValues.put(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, totAmountToSave);
        billValues.put(FabizContract.BillDetail.COLUMN_PAID, enteredAmntForUpdate);
        billValues.put(FabizContract.BillDetail.COLUMN_DUE, dueAmount);
        billValues.put(FabizContract.BillDetail.COLUMN_DISCOUNT, "0");

        try {
            //********TRANSACTION STARTED
            provider.createTransaction();
            String billId = provider.insert(FabizContract.BillDetail.TABLE_NAME, billValues) + "";
            Log.i("Bill Id:", "GId :" + idToInsertBill + ", RId :" + billId);


            if (Long.parseLong(billId) > 0) {
                billId = idToInsertBill;
                List<SyncLogDetail> syncLogList = new ArrayList<>();
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

                    long insertIdPayment = 0;
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
                        logTranscValues.put(FabizContract.Payment.COLUMN_AMOUNT, enteredAmntForUpdate);
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
                        new SetupSync(this, syncLogList, provider, "Successfully Saved.", OP_CODE_SALE);
                        showDialogueInfo(totAmountToSave, enteredAmntForUpdate);

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

    private void showDialogueInfo(double billAmt, double entAmt) {
        double dueAmt = billAmt - entAmt;
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


        Button okayButton = dialog.findViewById(R.id.pop_up_for_payment_okay);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        okayButton.setText("Done");

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
        double currentDue = (dueAmtPassed + billAmt) - entAmt;
        totDueAmnt.setText(TruncateDecimal(currentDue + ""));

        Button addPaymentBtn = dialog.findViewById(R.id.pop_up_for_payment_add_pay);
        addPaymentBtn.setVisibility(View.VISIBLE);
        addPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPaymentIntent = new Intent(Sales.this, AddPayment.class);
                dialog.dismiss();
            }
        });


        billAmntV.setText(TruncateDecimal(billAmt + ""));
        //       dateV.setText( currentTime);
        enteredAmntV.setText(TruncateDecimal(entAmt + ""));
        dueAmtV.setText(TruncateDecimal(dueAmt + ""));

        dialog.show();
    }

    private double getEnteredAmnt() {
        if (amtEditText.getText().toString().trim().matches("") || amtEditText.getText().toString().trim().matches("-") || amtEditText.getText().toString().trim().matches(".")) {
            return 0;
        } else {
            try {
                return Double.parseDouble(amtEditText.getText().toString().trim());
            } catch (Error e) {
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
}
