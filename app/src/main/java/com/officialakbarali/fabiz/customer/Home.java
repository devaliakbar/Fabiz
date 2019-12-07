package com.officialakbarali.fabiz.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.payment.AddPayment;
import com.officialakbarali.fabiz.customer.payment.PaymentReview;
import com.officialakbarali.fabiz.customer.sale.Sales;
import com.officialakbarali.fabiz.customer.sale.SalesReturnReview;
import com.officialakbarali.fabiz.customer.sale.SalesReview;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;

import java.util.ArrayList;

import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.convertToCamelCase;
import static com.officialakbarali.fabiz.data.CommonInformation.getCurrency;

public class Home extends AppCompatActivity {
    private String custId;
    private String custName;
    private String custPhone;
    private String custEmail;
    private String custAddress;
    private String custCrNo;
    private String custShopName;
    private String vatNo;
    private String telephone;

    private FabizProvider provider;
    private Toast toast;

    private double custDueAmt;

    TextView idView, nameView, phoneView, crNoView, shopNameView, emailView, addressView, vatView, telephoneV, custDueView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.text_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }


        custId = getIntent().getStringExtra("id");

        provider = new FabizProvider(this, false);

        setCustomerDetail();

        ImageButton goToSaleButton = findViewById(R.id.cust_home_sale);
        goToSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent saleIntent = new Intent(Home.this, Sales.class);
                saleIntent.putExtra("id", custId + "");
                saleIntent.putExtra("custDueAmt", custDueAmt + "");
                Sales.cartItems = new ArrayList<>();
                startActivity(saleIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ImageButton salesReview = findViewById(R.id.cust_home_sale_review);
        salesReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent salesReviewIntent = new Intent(Home.this, SalesReview.class);
                salesReviewIntent.putExtra("id", custId + "");
                startActivity(salesReviewIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        final ImageButton salesReturnButton = findViewById(R.id.cust_home_sales_return);
        salesReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent salesReturnIntent = new Intent(Home.this, SalesReview.class);
                salesReturnIntent.putExtra("fromSalesReturn", true);
                salesReturnIntent.putExtra("id", custId + "");

                startActivity(salesReturnIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ImageButton saleReturnReviewButton = findViewById(R.id.cust_home_sales_return_review);
        saleReturnReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent saleReturnReviewIntent = new Intent(Home.this, SalesReturnReview.class);
                saleReturnReviewIntent.putExtra("id", custId + "");
                startActivity(saleReturnReviewIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });


        ImageButton payDueButton = findViewById(R.id.cust_home_sales_paydue);
        payDueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent payIntent = new Intent(Home.this, AddPayment.class);
                payIntent.putExtra("id", custId + "");
                startActivity(payIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ImageButton paymentReviewButton = findViewById(R.id.cust_home_sales_payment_review);
        paymentReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent paymentReviewIntent = new Intent(Home.this, PaymentReview.class);
                paymentReviewIntent.putExtra("id", custId + "");
                startActivity(paymentReviewIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setPaymentsDetail();
        setUpAnimation();
    }


    private void setCustomerDetail() {

        Cursor customerDetailCursor = provider.query(FabizContract.Customer.TABLE_NAME, new String[]{},
                FabizContract.Customer._ID + "=?", new String[]{custId + ""}, null);

        if (customerDetailCursor.moveToNext()) {

            custName = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_NAME));
            custPhone = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_PHONE));

            custCrNo = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_CR_NO));
            custShopName = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_SHOP_NAME));

            custEmail = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_EMAIL));
            custAddress = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_ADDRESS));

            telephone = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_TELEPHONE));
            vatNo = customerDetailCursor.getString(customerDetailCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_VAT_NO));

            idView = findViewById(R.id.cust_home_id);
            idView.setText("Customer ID : " + custId);

            nameView = findViewById(R.id.cust_home_name);
            if (custName.length() > 38) {
                nameView.setText(convertToCamelCase(custName.substring(0, 34)) + "...");
            } else {
                nameView.setText(convertToCamelCase(custName));
            }

            phoneView = findViewById(R.id.cust_home_phone);
            if (custPhone.length() > 19) {
                phoneView.setText(custPhone.substring(0, 15) + "...");
            } else {
                phoneView.setText(custPhone);
            }

            crNoView = findViewById(R.id.cust_home_cr);
            if (custCrNo.length() > 38) {
                crNoView.setText("CR_NO : " + custCrNo.substring(0, 34) + "...");
            } else {
                crNoView.setText("CR_NO : " + custCrNo);
            }

            shopNameView = findViewById(R.id.cust_home_shop_name);
            if (custShopName.length() > 19) {
                shopNameView.setText(custShopName.substring(0, 15) + "...");
            } else {
                shopNameView.setText(custShopName);
            }


            emailView = findViewById(R.id.cust_home_email);
            if (custEmail.length() > 38) {
                emailView.setText(custEmail.substring(0, 34) + "...");
            } else {
                emailView.setText(custEmail);
            }

            addressView = findViewById(R.id.cust_home_address);
            if (custAddress.length() > 38) {
                addressView.setText(custAddress.substring(0, 34) + "...");
            } else {
                addressView.setText(custAddress);
            }

            vatView = findViewById(R.id.cust_home_vat_no);
            if (vatNo.length() > 38) {
                vatView.setText("VAT_NO : " + vatNo.substring(0, 34) + "...");
            } else {
                vatView.setText("VAT_NO : " + vatNo);
            }

            telephoneV = findViewById(R.id.cust_home_telephone);
            if (telephone.length() > 38) {
                telephoneV.setText(telephone.substring(0, 34) + "...");
            } else {
                telephoneV.setText(telephone);
            }

            custDueView = findViewById(R.id.cust_home_due);

        } else {
            showToast("Something went wrong");
            finish();
        }
    }

    private void setPaymentsDetail() {

        custDueAmt = provider.getCount(FabizContract.BillDetail.TABLE_NAME, FabizContract.BillDetail.COLUMN_DUE, FabizContract.BillDetail.COLUMN_CUST_ID + "=?",
                new String[]{custId + ""});
        custDueView.setText("Due Amount :" + TruncateDecimal(custDueAmt + "")+ " " + getCurrency());

    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
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
        LinearLayout vatCrCont, salesCont, payCont, returnCont, payRCont, salesRCont, returnRCont;
        vatCrCont = findViewById(R.id.vat_cr_cont);

        salesCont = findViewById(R.id.sale_cont);
        payCont = findViewById(R.id.pay_cont);
        returnCont = findViewById(R.id.sales_return_cont);
        payRCont = findViewById(R.id.pay_review_cont);
        salesRCont = findViewById(R.id.sales_review_cont);
        returnRCont = findViewById(R.id.sales_return_review_cont);


        vatCrCont.setVisibility(View.INVISIBLE);

        salesCont.setVisibility(View.INVISIBLE);
        payCont.setVisibility(View.INVISIBLE);
        returnCont.setVisibility(View.INVISIBLE);
        payRCont.setVisibility(View.INVISIBLE);
        salesRCont.setVisibility(View.INVISIBLE);
        returnRCont.setVisibility(View.INVISIBLE);

        custDueView.setVisibility(View.INVISIBLE);
        addressView.setVisibility(View.INVISIBLE);
        emailView.setVisibility(View.INVISIBLE);
        shopNameView.setVisibility(View.INVISIBLE);
        telephoneV.setVisibility(View.INVISIBLE);
        phoneView.setVisibility(View.INVISIBLE);
        nameView.setVisibility(View.INVISIBLE);
        idView.setVisibility(View.INVISIBLE);

    }

    private void setUpAnimation() {
        hideViews();

        final LinearLayout vatCrCont, salesCont, payCont, returnCont, payRCont, salesRCont, returnRCont;
        vatCrCont = findViewById(R.id.vat_cr_cont);

        salesCont = findViewById(R.id.sale_cont);
        payCont = findViewById(R.id.pay_cont);
        returnCont = findViewById(R.id.sales_return_cont);
        payRCont = findViewById(R.id.pay_review_cont);
        salesRCont = findViewById(R.id.sales_review_cont);
        returnRCont = findViewById(R.id.sales_return_review_cont);

        YoYo.with(Techniques.SlideInRight).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                nameView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).duration(400).repeat(0).playOn(nameView);
                idView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInUp).duration(400).repeat(0).playOn(idView);
                shopNameView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        vatCrCont.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInUp).duration(400).repeat(0).playOn(vatCrCont);
                        phoneView.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInLeft).duration(400).repeat(0).playOn(phoneView);
                        addressView.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInLeft).duration(400).repeat(0).playOn(addressView);
                        emailView.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInLeft).duration(400).repeat(0).playOn(emailView);
                        telephoneV.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInRight).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                salesCont.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInLeft).duration(400).repeat(0).playOn(salesCont);
                                payRCont.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInLeft).duration(400).repeat(0).playOn(payRCont);
                                returnCont.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInRight).duration(400).repeat(0).playOn(returnCont);
                                returnRCont.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInRight).duration(400).repeat(0).playOn(returnRCont);
                                payCont.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInUp).duration(400).repeat(0).playOn(payCont);
                                salesRCont.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInUp).withListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        custDueView.setVisibility(View.VISIBLE);
                                        YoYo.with(Techniques.FadeInUp).duration(400).repeat(0).playOn(custDueView);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).duration(400).repeat(0).playOn(salesRCont);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).duration(400).repeat(0).playOn(telephoneV);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).duration(400).repeat(0).playOn(shopNameView);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(300).repeat(0).playOn(idView);
    }
}
