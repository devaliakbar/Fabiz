package com.officialakbarali.fabiz;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.customer.Customer;
import com.officialakbarali.fabiz.item.Item;
import com.officialakbarali.fabiz.network.syncInfo.SyncInformation;
import com.officialakbarali.fabiz.requestStock.RequestStock;

import java.util.ArrayList;

import static com.officialakbarali.fabiz.data.CommonInformation.convertToCamelCase;
import static com.officialakbarali.fabiz.data.CommonInformation.setCurrency;

public class MainHome extends AppCompatActivity {
    LinearLayout customerL, stockL, requestL, syncL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        TextView nameText = findViewById(R.id.name_txt);

        String nameS = "Hai ";

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String fullName = sharedPreferences.getString("nameOfStaff", "User");

        fullName = convertToCamelCase(fullName);

        fullName = fullName.split(" ")[0];

        nameS += fullName + "!";
        nameText.setText(nameS);

        setUpCurrency();
        setUpHomeButton();
    }


    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    private void setUpCurrency() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setCurrency(sharedPreferences.getString("currency", "BD"));
    }

    private void setUpHomeButton() {

        customerL = findViewById(R.id.home_cust);
        stockL = findViewById(R.id.home_stock);
        requestL = findViewById(R.id.home_request);
        syncL = findViewById(R.id.home_sync);
        ImageButton settingsL = findViewById(R.id.home_settings);


        settingsL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSettings = new Intent(MainHome.this, Settings.class);
                startActivity(intentSettings);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        customerL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView textView = findViewById(R.id.home_cust_txt);
                ImageView btn = findViewById(R.id.home_cust_img);
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        customerL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon_pressed));
                        btn.setImageResource(R.drawable.main_home_customer_p);
                        textView.setTextColor(getResources().getColor(R.color.pure_white));
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        btn.setImageResource(R.drawable.main_home_customer);
                        customerL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon));
                        textView.setTextColor(getResources().getColor(R.color.text_color));
                        Intent custIntent = new Intent(MainHome.this, Customer.class);
                        startActivity(custIntent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        stockL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                ImageView btn = findViewById(R.id.home_stock_img);
                TextView textView = findViewById(R.id.home_stock_txt);

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        btn.setImageResource(R.drawable.main_home_item_detail_p);
                        stockL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon_pressed));
                        textView.setTextColor(getResources().getColor(R.color.pure_white));
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        btn.setImageResource(R.drawable.main_home_item_detail);
                        stockL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon));
                        textView.setTextColor(getResources().getColor(R.color.text_color));
                        Intent itemShow = new Intent(MainHome.this, Item.class);
                        startActivity(itemShow);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
        requestL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView btn = findViewById(R.id.req_img);
                TextView textView = findViewById(R.id.home_request_txt);

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        btn.setImageResource(R.drawable.main_home_item_p);
                        requestL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon_pressed));
                        textView.setTextColor(getResources().getColor(R.color.pure_white));
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        btn.setImageResource(R.drawable.main_home_item);
                        requestL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon));
                        textView.setTextColor(getResources().getColor(R.color.text_color));
                        Intent requestStockShow = new Intent(MainHome.this, RequestStock.class);
                        startActivity(requestStockShow);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
        syncL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView btn = findViewById(R.id.syn_img);
                TextView textView = findViewById(R.id.home_sync_txt);

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        btn.setImageResource(R.drawable.main_home_sync_p);
                        syncL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon_pressed));
                        textView.setTextColor(getResources().getColor(R.color.pure_white));
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        btn.setImageResource(R.drawable.main_home_sync);
                        syncL.setBackground(getResources().getDrawable(R.drawable.button_color_main_home_icon));
                        textView.setTextColor(getResources().getColor(R.color.text_color));
                        Intent viewSyncIntent = new Intent(MainHome.this, SyncInformation.class);
                        startActivity(viewSyncIntent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideViews();
    }

    private void hideViews() {
        LinearLayout head;
        TextView belowHead;
        head = findViewById(R.id.home_cust_head);

        belowHead = findViewById(R.id.home_cust_below_head);


        head.setVisibility(View.INVISIBLE);


        belowHead.setVisibility(View.INVISIBLE);


        customerL.setVisibility(View.INVISIBLE);


        stockL.setVisibility(View.INVISIBLE);


        requestL.setVisibility(View.INVISIBLE);


        syncL.setVisibility(View.INVISIBLE);

    }

    private void setUpAnimation() {
        hideViews();
        final TextView belowHead;
        final LinearLayout head;
        head = findViewById(R.id.home_cust_head);
        belowHead = findViewById(R.id.home_cust_below_head);

        YoYo.with(Techniques.SlideInUp).withListener(new Animator.AnimatorListener() {
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
                        belowHead.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                                customerL.setVisibility(View.VISIBLE);
                                stockL.setVisibility(View.VISIBLE);
                                requestL.setVisibility(View.VISIBLE);
                                syncL.setVisibility(View.VISIBLE);

                                YoYo.with(Techniques.SlideInLeft).duration(400).repeat(0).playOn(customerL);
                                YoYo.with(Techniques.SlideInRight).duration(400).repeat(0).playOn(stockL);

                                YoYo.with(Techniques.SlideInLeft).duration(400).repeat(0).playOn(requestL);
                                YoYo.with(Techniques.SlideInRight).duration(400).repeat(0).playOn(syncL);

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).duration(400).repeat(0).playOn(belowHead);
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
        }).duration(100).repeat(0).playOn(syncL);


    }
}
