package com.officialakbarali.fabiz.customer.payment;

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
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.officialakbarali.fabiz.bottomSheets.SalesReviewFilterBottomSheet;
import com.officialakbarali.fabiz.customer.sale.adapter.SalesReviewAdapter;
import com.officialakbarali.fabiz.customer.sale.data.SalesReviewDetail;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.network.syncInfo.SetupSync;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.officialakbarali.fabiz.bottomSheets.SalesReviewFilterBottomSheet.SALES_REVIEW_FILTER_TAG;
import static com.officialakbarali.fabiz.data.CommonInformation.GET_DATE_FORMAT_REAL;
import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.convertDateToDisplayFormat;
import static com.officialakbarali.fabiz.data.CommonInformation.convertDateToSearchFormat;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_CODE_PAY;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_INSERT;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_UPDATE;

public class AddPayment extends AppCompatActivity implements SalesReviewAdapter.SalesReviewAdapterOnClickListener, SalesReviewFilterBottomSheet.SalesReviewFilterListener {
    private Toast toast;

    SalesReviewAdapter salesReviewAdapter;
    private String custId;

    private TextView dateV;

    String fromDateTime, currentTime;

    private double dueA;

    SalesReviewDetail mSalesReviewDetail;
    Dialog paymentDialog;

    boolean dueDiscount = false;

    EditText searchEditText;
    String filterSelection = FabizContract.BillDetail.FULL_COLUMN_ID + " LIKE ?";
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        custId = getIntent().getStringExtra("id");

        recyclerView = findViewById(R.id.sales_review_recycler);
        salesReviewAdapter = new SalesReviewAdapter(this, this, true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(salesReviewAdapter);

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


        setPaymentsDetail();
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

    private void setPaymentsDetail() {
        FabizProvider providerForFetch = new FabizProvider(this, false);
        TextView custDue = findViewById(R.id.cust_payment_due_total);
        dueA = providerForFetch.getCount(FabizContract.BillDetail.TABLE_NAME, FabizContract.BillDetail.COLUMN_DUE, FabizContract.BillDetail.COLUMN_CUST_ID + "=?",
                new String[]{custId + ""});
        custDue.setText("Total Due Amount :" + TruncateDecimal(dueA + ""));
    }

    private void showBills(String Fselection, String[] FselectionArg) {
        FabizProvider provider = new FabizProvider(this, false);

        String tableName = FabizContract.BillDetail.TABLE_NAME + " INNER JOIN " + FabizContract.Cart.TABLE_NAME
                + " ON " + FabizContract.BillDetail.FULL_COLUMN_ID + " = " + FabizContract.Cart.FULL_COLUMN_BILL_ID;

        String selection = FabizContract.BillDetail.FULL_COLUMN_CUST_ID + "=? AND " +
                FabizContract.BillDetail.FULL_COLUMN_DUE + " NOT LIKE ?";

        String[] selectionArg;

        if (Fselection != null) {
            selection += " AND " + Fselection;
            selectionArg = new String[]{custId + "", "0.0%", FselectionArg[0]};
        } else {
            selectionArg = new String[]{custId + "", "0.0%"};
        }

        Cursor cursorBills = provider.queryExplicit(true,
                tableName,
                new String[]{FabizContract.BillDetail.FULL_COLUMN_ID, FabizContract.BillDetail.FULL_COLUMN_DATE,
                        FabizContract.BillDetail.FULL_COLUMN_QTY, FabizContract.BillDetail.FULL_COLUMN_PRICE,
                        FabizContract.BillDetail.FULL_COLUMN_PAID, FabizContract.BillDetail.FULL_COLUMN_DUE,
                        FabizContract.BillDetail.FULL_COLUMN_RETURNED_TOTAL, FabizContract.BillDetail.FULL_COLUMN_CURRENT_TOTAL,
                        FabizContract.BillDetail.FULL_COLUMN_DISCOUNT},
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
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }

    @Override
    public void onClick(SalesReviewDetail salesReviewDetail) {
        mSalesReviewDetail = salesReviewDetail;
        showPaymentDialogue();
    }

    private void showPaymentDialogue() {
        paymentDialog = new Dialog(this);
        paymentDialog.setContentView(R.layout.pop_up_payment);

        //SETTING SCREEN WIDTH
        paymentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = paymentDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************

        TextView billDueText = paymentDialog.findViewById(R.id.cust_payment_due);
        billDueText.setText(TruncateDecimal(mSalesReviewDetail.getDue() + ""));

        dateV = paymentDialog.findViewById(R.id.cust_payment_date);
        try {
            currentTime = convertDateToDisplayFormat(getCurrentDateTime());
            dateV.setText(currentTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Button changeB = paymentDialog.findViewById(R.id.cust_payment_change);
        changeB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        final EditText paidAmountV = paymentDialog.findViewById(R.id.cust_payment_to_pay);

        Button payNowB = paymentDialog.findViewById(R.id.cust_payment_pay_now);
        payNowB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox dueDiscCheckBox = paymentDialog.findViewById(R.id.cust_payment_due_discount);

                dueDiscount = dueDiscCheckBox.isChecked();

                setUpPaymentAccount(paidAmountV.getText().toString());
            }
        });


        paymentDialog.show();
    }

    private void setUpPaymentAccount(String amountToUpdate) {
        if (amountToUpdate.matches("")) {
            showToast("Please enter the amount");
        } else {
            try {
                double enteredAmount = Double.parseDouble(amountToUpdate);

                if (enteredAmount == 0) {
                    showToast("Enter a valid number");
                    return;
                }


                if (dueA <= 0 && enteredAmount <= 0 && enteredAmount < dueA) {
                    showToast("You giving more amount than total credit");
                    return;
                }


                if (enteredAmount > mSalesReviewDetail.getDue()) {
                    if (mSalesReviewDetail.getDue() > 0 || enteredAmount > 0) {
                        showToast("Entered amount is greater than this bill amount");
                        return;
                    }
                }


                if (mSalesReviewDetail.getDue() > enteredAmount && mSalesReviewDetail.getDue() < 0 && enteredAmount < 0) {
                    showToast("You giving more amount than credit");
                    return;
                }


                if (mSalesReviewDetail.getDue() > 0 && enteredAmount < 0) {
                    showToast("You cannot give money back through this bill");
                    return;
                }


                if (enteredAmount > dueA) {
                    if (dueA > 0 || enteredAmount > 0) {
                        showToast("Entered Amount is greater than total due amount");
                        return;
                    }
                }


                if (dueA > enteredAmount && dueA <= 0 && enteredAmount < 0) {
                    showToast("You giving more amount than the total credit");
                    return;
                }

//                if (dueA <= 0 && enteredAmount < dueA) {
//                    showToast("Total due is ");
//                    return;
//                }


                if (dueDiscount) {
                    if (enteredAmount < 0) {
                        showToast("Please enter valid discount amount");
                        return;
                    }
                }


                setPaymentToSql(enteredAmount);

            } catch (Error e) {
                showToast("Enter a valid number");
            }
        }
    }

    private void setPaymentToSql(double enteredAmount) {


        FabizProvider provider = new FabizProvider(this, true);

        double discountAmountToUpdate = mSalesReviewDetail.getDiscount();
        double paidAmountToUpdate = mSalesReviewDetail.getPaid();


        if (dueDiscount) {
            discountAmountToUpdate += enteredAmount;
        } else {
            paidAmountToUpdate += enteredAmount;
        }

        double dueAmountToUpdate = mSalesReviewDetail.getDue() - enteredAmount;


        ContentValues accUpValues = new ContentValues();

        if (dueDiscount) {
            accUpValues.put(FabizContract.BillDetail.COLUMN_DISCOUNT, discountAmountToUpdate);
        } else {
            accUpValues.put(FabizContract.BillDetail.COLUMN_PAID, paidAmountToUpdate);
        }
        accUpValues.put(FabizContract.BillDetail.COLUMN_DUE, dueAmountToUpdate);

        //********TRANSACTION STARTED
        provider.createTransaction();

        int upAffectedRows = provider.update(FabizContract.BillDetail.TABLE_NAME, accUpValues,
                FabizContract.BillDetail._ID + "=?", new String[]{mSalesReviewDetail.getId() + ""});


        if (upAffectedRows == 1) {
            List<SyncLogDetail> syncLogList = new ArrayList<>();
            syncLogList.add(new SyncLogDetail(mSalesReviewDetail.getId(), FabizContract.BillDetail.TABLE_NAME, OP_UPDATE));

            String idToInsertPayment = provider.getIdForInsert(FabizContract.Payment.TABLE_NAME, "");
            if (idToInsertPayment.matches("-1")) {
                provider.finishTransaction();
                showToast("Maximum limit of offline mode reached,please contact customer support");
                return;
            }
            ContentValues logTranscValues = new ContentValues();
            logTranscValues.put(FabizContract.Payment._ID, idToInsertPayment);
            logTranscValues.put(FabizContract.Payment.COLUMN_BILL_ID, mSalesReviewDetail.getId());
            logTranscValues.put(FabizContract.Payment.COLUMN_DATE, currentTime);
            logTranscValues.put(FabizContract.Payment.COLUMN_AMOUNT, enteredAmount);

            if (dueDiscount) {
                logTranscValues.put(FabizContract.Payment.COLUMN_TYPE, "D");
            } else {
                logTranscValues.put(FabizContract.Payment.COLUMN_TYPE, "P");
            }

            long insertIdPayment = provider.insert(FabizContract.Payment.TABLE_NAME, logTranscValues);

            if (insertIdPayment > 0) {
                syncLogList.add(new SyncLogDetail(idToInsertPayment + "", FabizContract.Payment.TABLE_NAME, OP_INSERT));
                new SetupSync(this, syncLogList, provider, "Amount saved successful", OP_CODE_PAY);
                paymentDialog.dismiss();
                showDialogueInfo(enteredAmount, dueAmountToUpdate);
            } else {
                provider.finishTransaction();
                showToast("Failed to save");
            }
        } else {
            provider.finishTransaction();
            showToast("Something went wrong");
        }
    }

    private void showDialogueInfo(double entAmt, double dueAmt) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_for_sale_and_payment_success);

        //SETTING SCREEN WIDTH
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setPaymentsDetail();
                showBills(null, null);
            }
        });

        Button okayButton = dialog.findViewById(R.id.pop_up_for_payment_okay);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        final TextView dateV = dialog.findViewById(R.id.pop_up_for_payment_date);
        final TextView enteredAmntV = dialog.findViewById(R.id.pop_up_for_payment_ent_amt);
        final TextView dueAmtV = dialog.findViewById(R.id.pop_up_for_payment_due);

        TextView dueLabelText = dialog.findViewById(R.id.pop_up_for_payment_due_label);
        dueLabelText.setText("Bill Due Amount");


        dateV.setText(": " + currentTime);
        enteredAmntV.setText(": " + TruncateDecimal(entAmt + ""));

        dueAmtV.setText(": " + TruncateDecimal(dueAmt + ""));
        dialog.show();
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
                        TimePickerDialog timePickerDialog = new TimePickerDialog(AddPayment.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        //*************************

                                        fromDateTime = year + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + String.format("%02d", dayOfMonth) + "T";
                                        fromDateTime += String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":30Z";
                                        try {
                                            currentTime = convertDateToDisplayFormat(fromDateTime);
                                            dateV.setText(currentTime);
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

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(GET_DATE_FORMAT_REAL());
        Log.i("Time:", sdf.format(new Date()));
        return sdf.format(new Date());
    }


    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
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
        TextView custDue = findViewById(R.id.cust_payment_due_total);
        LinearLayout searchCont = findViewById(R.id.search_cont);

        custDue.setVisibility(View.INVISIBLE);
        searchCont.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideAllViews();

        final TextView custDue = findViewById(R.id.cust_payment_due_total);
        final LinearLayout searchCont = findViewById(R.id.search_cont);

        YoYo.with(Techniques.FadeInDown).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                custDue.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).duration(300).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchCont.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
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
                        }).duration(300).repeat(0).playOn(searchCont);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).repeat(0).playOn(custDue);
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
