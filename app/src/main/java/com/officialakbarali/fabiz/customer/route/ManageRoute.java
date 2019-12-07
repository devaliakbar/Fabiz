package com.officialakbarali.fabiz.customer.route;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;

import java.util.Calendar;

public class ManageRoute extends AppCompatActivity {
    Button mon, tue, wed, thu, fri, sat, sun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_route);

        mon = findViewById(R.id.mon);
        tue = findViewById(R.id.tue);
        wed = findViewById(R.id.wed);
        thu = findViewById(R.id.thu);
        fri = findViewById(R.id.fri);
        sat = findViewById(R.id.sat);
        sun = findViewById(R.id.sun);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }


    public void monday(View view) {
        Intent setDayRouteIntent = new Intent(ManageRoute.this, RouteModify.class);
        setDayRouteIntent.putExtra("today", Calendar.MONDAY + "");
        startActivity(setDayRouteIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void tuesday(View view) {
        Intent setDayRouteIntent = new Intent(ManageRoute.this, RouteModify.class);
        setDayRouteIntent.putExtra("today", Calendar.TUESDAY + "");
        startActivity(setDayRouteIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void wednesday(View view) {
        Intent setDayRouteIntent = new Intent(ManageRoute.this, RouteModify.class);
        setDayRouteIntent.putExtra("today", Calendar.WEDNESDAY + "");
        startActivity(setDayRouteIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void thursday(View view) {
        Intent setDayRouteIntent = new Intent(ManageRoute.this, RouteModify.class);
        setDayRouteIntent.putExtra("today", Calendar.THURSDAY + "");
        startActivity(setDayRouteIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void friday(View view) {
        Intent setDayRouteIntent = new Intent(ManageRoute.this, RouteModify.class);
        setDayRouteIntent.putExtra("today", Calendar.FRIDAY + "");
        startActivity(setDayRouteIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void saturday(View view) {
        Intent setDayRouteIntent = new Intent(ManageRoute.this, RouteModify.class);
        setDayRouteIntent.putExtra("today", Calendar.SATURDAY + "");
        startActivity(setDayRouteIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void sunday(View view) {
        Intent setDayRouteIntent = new Intent(ManageRoute.this, RouteModify.class);
        setDayRouteIntent.putExtra("today", Calendar.SUNDAY + "");
        startActivity(setDayRouteIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        final TextView head, belowHead;

        head = findViewById(R.id.route_head);
        belowHead = findViewById(R.id.route_below_head);

        head.setVisibility(View.INVISIBLE);
        belowHead.setVisibility(View.INVISIBLE);
        mon.setVisibility(View.INVISIBLE);
        tue.setVisibility(View.INVISIBLE);
        wed.setVisibility(View.INVISIBLE);
        thu.setVisibility(View.INVISIBLE);
        fri.setVisibility(View.INVISIBLE);
        sat.setVisibility(View.INVISIBLE);
        sun.setVisibility(View.INVISIBLE);
    }

    private void setUpAnimation() {
        hideViews();
        final TextView head, belowHead;

        head = findViewById(R.id.route_head);
        belowHead = findViewById(R.id.route_below_head);

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
                        belowHead.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.FadeInDown).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mon.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInLeft).duration(400).repeat(0).playOn(mon);
                                thu.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInLeft).duration(400).repeat(0).playOn(thu);

                                wed.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInRight).duration(400).repeat(0).playOn(wed);
                                sat.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInRight).withListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        tue.setVisibility(View.VISIBLE);
                                        YoYo.with(Techniques.FadeInUp).duration(350).repeat(0).playOn(tue);
                                        fri.setVisibility(View.VISIBLE);
                                        YoYo.with(Techniques.FadeInUp).duration(400).repeat(0).playOn(fri);
                                        sun.setVisibility(View.VISIBLE);
                                        YoYo.with(Techniques.FadeInUp).duration(450).repeat(0).playOn(sun);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).duration(400).repeat(0).playOn(sat);
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
        }).duration(400).repeat(0).playOn(belowHead);
    }
}
