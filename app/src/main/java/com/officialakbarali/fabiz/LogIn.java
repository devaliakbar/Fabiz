package com.officialakbarali.fabiz;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.blockPages.AppVersion;
import com.officialakbarali.fabiz.blockPages.ForcePull;
import com.officialakbarali.fabiz.network.VolleyRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.officialakbarali.fabiz.data.CommonInformation.SET_DECIMAL_LENGTH;
import static com.officialakbarali.fabiz.data.MyAppVersion.GET_MY_APP_VERSION;

public class LogIn extends AppCompatActivity {
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        Button logInBtn = findViewById(R.id.log_in_btn);
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText usernameE, passwordE;
                usernameE = findViewById(R.id.log_in_usr);
                passwordE = findViewById(R.id.log_in_pass);

                String usernameString = usernameE.getText().toString().trim();
                String passwordString = passwordE.getText().toString().trim();

                if (usernameString.matches("")) {
                    showToast("Please enter username");
                } else {
                    if (passwordString.matches("")) {
                        showToast("Please eneter password");
                    } else {
                        performLogIn(usernameString, passwordString);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean appVersionProblem = sharedPreferences.getBoolean("version", false);
        if (appVersionProblem) {
            Intent versionIntent = new Intent(this, AppVersion.class);
            versionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(versionIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            setUpStartAnimation();
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void performLogIn(final String username, final String password) {
        showLoading(true);
        hideKeybord();
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("app_version", "" + GET_MY_APP_VERSION());
        hashMap.put("my_username", "" + username);
        hashMap.put("my_password", "" + password);

        final VolleyRequest volleyRequest = new VolleyRequest("login.php", hashMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showLoading(false);
                Log.i("Response :", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                        proceed(username, password, jsonObject.getString("mysign"), jsonObject.getInt("precision"), jsonObject.getInt("idOfStaff"), jsonObject.getString("nameOfStaff"));
                    } else {
                        if (jsonObject.getString("status").equals("VERSION")) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LogIn.this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("version", true);
                            editor.apply();
                            Intent versionIntent = new Intent(LogIn.this, AppVersion.class);
                            versionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(versionIntent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else if (jsonObject.getString("status").equals("USER")) {
                            showToast("Invalid username or password");
                        } else {
                            showToast("Something went wrong");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("Bad Response From Server");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showLoading(false);
                if (error instanceof ServerError) {
                    showToast("Server Error");
                } else if (error instanceof TimeoutError) {
                    showToast("Connection Timed Out");
                } else if (error instanceof NetworkError) {
                    showToast("Bad Network Connection");
                }
            }
        });
        requestQueue.add(volleyRequest);
    }

    private void proceed(String username, String password, String mysign, int precision, int idOfStaff, String staffName) {
        final SharedPreferences
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("my_username", username);
        editor.putString("my_password", password);
        editor.putString("mysign", mysign);
        editor.putBoolean("update_data", false);
        editor.putBoolean("force_pull", true);
        editor.putInt("precision", precision);
        editor.putInt("idOfStaff", idOfStaff);
        editor.putString("nameOfStaff", staffName);

        final Intent mainHomeIntent = new Intent(LogIn.this, ForcePull.class);
        mainHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        final LinearLayout precisionContainer = findViewById(R.id.precision_cont);
        final RelativeLayout logInContainer = findViewById(R.id.main_log_in_cont);

        YoYo.with(Techniques.SlideOutUp).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                logInContainer.setVisibility(View.GONE);
                precisionContainer.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInUp).duration(600).repeat(0).playOn(precisionContainer);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(450).repeat(0).playOn(logInContainer);


        Button pre2 = findViewById(R.id.pre_2);
        pre2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("decimal_precision", 2);
                SET_DECIMAL_LENGTH(2);
                editor.apply();
                setUpCurrency(mainHomeIntent);
            }
        });
        Button pre3 = findViewById(R.id.pre_3);
        pre3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("decimal_precision", 3);
                SET_DECIMAL_LENGTH(3);
                editor.apply();
                setUpCurrency(mainHomeIntent);
            }
        });
    }


    private void setUpCurrency(final Intent pIntent) {

        final LinearLayout precisionContainer = findViewById(R.id.precision_cont);


        final RelativeLayout currencyContainer = findViewById(R.id.currency_cont);


        YoYo.with(Techniques.SlideOutUp).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                precisionContainer.setVisibility(View.GONE);
                currencyContainer.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInUp).duration(600).repeat(0).playOn(currencyContainer);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(600).repeat(0).playOn(precisionContainer);


        ImageButton proceedBtn = findViewById(R.id.proceed);
        proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText currecyText = findViewById(R.id.currency);
                String currency = currecyText.getText().toString().trim().toUpperCase();
                SharedPreferences
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LogIn.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (currency.length() > 0 && currency.length() <= 3) {
                    editor.putString("currency", currency);
                } else {
                    editor.putString("currency", "BD");
                }
                editor.apply();
                startActivity(pIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideViews();
    }

    private void hideViews() {
        TextView head, belowHead;
        EditText username, password;

        Button logIn = findViewById(R.id.log_in_btn);
        logIn.setVisibility(View.INVISIBLE);

        RadioButton remember = findViewById(R.id.login_remember);
        TextView forgot = findViewById(R.id.login_forgot);
        remember.setVisibility(View.INVISIBLE);
        forgot.setVisibility(View.INVISIBLE);

        head = findViewById(R.id.log_in_head);
        head.setVisibility(View.INVISIBLE);
        belowHead = findViewById(R.id.log_in_below_head);
        belowHead.setVisibility(View.INVISIBLE);

        username = findViewById(R.id.log_in_usr);
        username.setVisibility(View.INVISIBLE);
        password = findViewById(R.id.log_in_pass);
        password.setVisibility(View.INVISIBLE);
    }

    private void setUpStartAnimation() {
        hideViews();

        final TextView head, belowHead;
        final EditText username, password;

        final Button logIn = findViewById(R.id.log_in_btn);

        final RadioButton remember = findViewById(R.id.login_remember);
        final TextView forgot = findViewById(R.id.login_forgot);


        head = findViewById(R.id.log_in_head);
        belowHead = findViewById(R.id.log_in_below_head);

        username = findViewById(R.id.log_in_usr);
        password = findViewById(R.id.log_in_pass);


        YoYo.with(Techniques.SlideInLeft).withListener(new Animator.AnimatorListener() {
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
                                username.setVisibility(View.VISIBLE);
                                password.setVisibility(View.VISIBLE);
                                logIn.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.FadeInDown).duration(400).repeat(0).playOn(username);
                                YoYo.with(Techniques.SlideInUp).duration(400).repeat(0).playOn(password);
                                YoYo.with(Techniques.SlideInUp).duration(400).repeat(0).playOn(logIn);
                                remember.setVisibility(View.VISIBLE);
                                forgot.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInLeft).duration(450).repeat(0).playOn(remember);
                                YoYo.with(Techniques.SlideInRight).duration(450).repeat(0).playOn(forgot);
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
        }).duration(100).repeat(0).playOn(remember);
    }

    private void hideKeybord() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private void showLoading(boolean show) {
        LinearLayout loadingV = findViewById(R.id.loading);
        if (show) {
            loadingV.setVisibility(View.VISIBLE);
        } else {
            loadingV.setVisibility(View.GONE);
        }

        disableAllView(show);
    }

    public void disableAllView(boolean hide) {
        EditText username, password;
        Button logIn = findViewById(R.id.log_in_btn);
        logIn.setEnabled(!hide);

        TextView forgot = findViewById(R.id.login_forgot);
        forgot.setEnabled(!hide);

        username = findViewById(R.id.log_in_usr);
        username.setEnabled(!hide);
        password = findViewById(R.id.log_in_pass);
        password.setEnabled(!hide);
    }
}
