package com.officialakbarali.fabiz.customer;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.network.syncInfo.SetupSync;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.officialakbarali.fabiz.data.CommonInformation.GET_PHONE_NUMBER_LENGTH;
import static com.officialakbarali.fabiz.data.CommonInformation.getNumberFromDayName;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_CODE_ADD_CUSTOMER;
import static com.officialakbarali.fabiz.network.syncInfo.SetupSync.OP_INSERT;


public class AddCustomer extends AppCompatActivity {
    TextInputEditText nameE, phoneE, emailE, addresssE, crE, shopNameE, telephoneE, vatNoE;
    private Toast toast;
    FabizProvider fabizProvider;
    Button saveCustomerB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        nameE = findViewById(R.id.cust_add_name);
        phoneE = findViewById(R.id.cust_add_phone);
        emailE = findViewById(R.id.cust_add_email);
        addresssE = findViewById(R.id.cust_add_address);
        crE = findViewById(R.id.cust_add_cr);
        shopNameE = findViewById(R.id.cust_add_shop_name);

        telephoneE = findViewById(R.id.cust_add_telephone);
        vatNoE = findViewById(R.id.cust_add_vat_no);

        fabizProvider = new FabizProvider(this, true);

        saveCustomerB = findViewById(R.id.cust_add_save);
        saveCustomerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameE.getText().toString().toUpperCase().trim();
                String phone = phoneE.getText().toString().trim();
                String email = emailE.getText().toString().trim();
                String address = addresssE.getText().toString().toUpperCase().trim();
                String crNumber = crE.getText().toString().toUpperCase().trim();
                String shopName = shopNameE.getText().toString().toUpperCase().trim();


                String telephone = telephoneE.getText().toString().trim();


                String vatNo = vatNoE.getText().toString().trim();

                Spinner filterSpinner = findViewById(R.id.cust_add_day_list);
                String selectedDay = "" + getNumberFromDayName(String.valueOf(filterSpinner.getSelectedItem()));


                EditText prefixE = findViewById(R.id.cust_add_prefix);
                String prefix = prefixE.getText().toString();

                if (prefix.matches("")) {
                    prefix = "A";
                }
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AddCustomer.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("sales_prefix", prefix);
                editor.apply();


                ContentValues values = new ContentValues();
                String idOfCuustomerToInsert = fabizProvider.getIdForInsert(FabizContract.Customer.TABLE_NAME, prefix);
                idOfCuustomerToInsert = prefix + idOfCuustomerToInsert;

                values.put(FabizContract.Customer._ID, idOfCuustomerToInsert);
                values.put(FabizContract.Customer.COLUMN_BARCODE, idOfCuustomerToInsert);
                values.put(FabizContract.Customer.COLUMN_DAY, selectedDay);//String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
                values.put(FabizContract.Customer.COLUMN_NAME, name);
                values.put(FabizContract.Customer.COLUMN_PHONE, phone);

                if (crNumber.matches("")) {
                    values.put(FabizContract.Customer.COLUMN_CR_NO, "NA");
                } else {
                    values.put(FabizContract.Customer.COLUMN_CR_NO, crNumber);
                }

                if (shopName.matches("")) {
                    values.put(FabizContract.Customer.COLUMN_SHOP_NAME, "NA");
                } else {
                    values.put(FabizContract.Customer.COLUMN_SHOP_NAME, shopName);
                }

                if (email.matches("")) {
                    values.put(FabizContract.Customer.COLUMN_EMAIL, "NA");
                } else {
                    values.put(FabizContract.Customer.COLUMN_EMAIL, email);
                }

                if (address.matches("")) {
                    values.put(FabizContract.Customer.COLUMN_ADDRESS, "NA");
                } else {
                    values.put(FabizContract.Customer.COLUMN_ADDRESS, address);
                }

                if (telephone.matches("")) {
                    values.put(FabizContract.Customer.COLUMN_TELEPHONE, "NA");
                } else {
                    values.put(FabizContract.Customer.COLUMN_TELEPHONE, telephone);
                }

                if (vatNo.matches("")) {
                    values.put(FabizContract.Customer.COLUMN_VAT_NO, "NA");
                } else {
                    values.put(FabizContract.Customer.COLUMN_VAT_NO, vatNo);
                }


                if (validateCustomerFields(values)) {
                    saveCustomer(values);
                }
            }
        });

        EditText prefixE = findViewById(R.id.cust_add_prefix);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String salesPrefix = sharedPreferences.getString("cust_add_prefix", "A");
        prefixE.setText(salesPrefix);
    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
    }

    private boolean validateName(String name) {
        Pattern pattern = Pattern.compile("[^A-Za-z0-9 ._-]");
        Matcher matcher = pattern.matcher(name);
        return name.length() > 0 && !matcher.find();
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        Pattern pattern = Pattern.compile("[^0-9+ ]");
        Matcher matcher = pattern.matcher(phoneNumber);
        return phoneNumber.length() >= GET_PHONE_NUMBER_LENGTH() && phoneNumber.length() <= 25 && !matcher.find();
    }

    private boolean validateEmail(String email) {
        if (email.matches("NA")) return false;
        return email.matches("^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$");
    }


    private boolean validateVatNumber(String passedString) {
        if (passedString.matches("NA")) return true;
        Pattern pattern = Pattern.compile("[^A-Za-z0-9 ._-]");
        Matcher matcher = pattern.matcher(passedString);
        return !matcher.find();
    }

    private boolean validateCommonInformation(String passedString) {
        if (passedString.matches("NA")) return false;
        Pattern pattern = Pattern.compile("[^A-Za-z0-9 ._-]");
        Matcher matcher = pattern.matcher(passedString);
        return !matcher.find();
    }

    private boolean validateAddressInformation(String passedString) {
        if (passedString.matches("NA")) return false;
        Pattern pattern = Pattern.compile("[^A-Za-z0-9 ._,-]");
        Matcher matcher = pattern.matcher(passedString);
        return !matcher.find();
    }


    private boolean validateCustomerFields(ContentValues values) {
        if (validateName(values.getAsString(FabizContract.Customer.COLUMN_NAME))) {
            if (validatePhoneNumber(values.getAsString(FabizContract.Customer.COLUMN_PHONE))) {
                if (validateEmail(values.getAsString(FabizContract.Customer.COLUMN_EMAIL))) {
                    if (validateAddressInformation(values.getAsString(FabizContract.Customer.COLUMN_ADDRESS))) {
                        if (validateCommonInformation(values.getAsString(FabizContract.Customer.COLUMN_SHOP_NAME))) {
                            if (validateCommonInformation(values.getAsString(FabizContract.Customer.COLUMN_CR_NO))) {
                                if (validateVatNumber(values.getAsString(FabizContract.Customer.COLUMN_VAT_NO))) {
                                    if (validatePhoneNumber(values.getAsString(FabizContract.Customer.COLUMN_TELEPHONE))) {
                                        return true;
                                    } else {
                                        showToast("Please enter valid Telephone");
                                    }
                                } else {
                                    showToast("Please enter valid Vat Number");
                                }
                            } else {
                                showToast("Please enter valid CR Number");
                            }
                        } else {
                            showToast("Please enter valid Shop Name");
                        }
                    } else {
                        showToast("Please enter valid Address");
                    }
                } else {
                    showToast("Please enter valid Email Address");
                }
            } else {
                showToast("Please enter valid Phone Number");
            }
        } else {
            showToast("Please enter valid Name");
        }
        return false;
    }

    private void saveCustomer(ContentValues values) {
        try {
            //********TRANSACTION STARTED
            fabizProvider.createTransaction();
            long idOfCustomer = fabizProvider.insert(FabizContract.Customer.TABLE_NAME, values);

            if (idOfCustomer > 0) {
                List<SyncLogDetail> syncLogList = new ArrayList<>();
                syncLogList.add(new SyncLogDetail(values.get(FabizContract.Customer._ID) + "", FabizContract.Customer.TABLE_NAME, OP_INSERT));
                new SetupSync(this, syncLogList, fabizProvider, "Successfully Saved. Id:" + values.get(FabizContract.Customer._ID), OP_CODE_ADD_CUSTOMER);
                finish();
            } else {
                fabizProvider.finishTransaction();
                showToast("Failed to Save");
            }
        } catch (Error e) {
            fabizProvider.finishTransaction();
            showToast("Failed to Save");
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        hideViews();
    }

    private void hideViews() {
        TextView head, belowHead;
        LinearLayout prefixContainer, dayContainer;

        head = findViewById(R.id.cust_add_head);
        belowHead = findViewById(R.id.cust_add_below_head);

        prefixContainer = findViewById(R.id.cust_add_prefix_cont);
        dayContainer = findViewById(R.id.cust_add_day_container);

        head.setVisibility(View.INVISIBLE);
        belowHead.setVisibility(View.INVISIBLE);
        prefixContainer.setVisibility(View.INVISIBLE);
        dayContainer.setVisibility(View.INVISIBLE);

        saveCustomerB.setVisibility(View.INVISIBLE);

        TextInputLayout nameC = findViewById(R.id.namec);
        TextInputLayout phoneC = findViewById(R.id.phonec);
        TextInputLayout emailC = findViewById(R.id.emailc);
        TextInputLayout addresssC = findViewById(R.id.addc);
        TextInputLayout vatNoC = findViewById(R.id.vatc);
        TextInputLayout crC = findViewById(R.id.crc);
        TextInputLayout shopNameC = findViewById(R.id.shopc);
        TextInputLayout telephoneC = findViewById(R.id.telec);

        nameC.setVisibility(View.INVISIBLE);
        phoneC.setVisibility(View.INVISIBLE);
        emailC.setVisibility(View.INVISIBLE);
        vatNoC.setVisibility(View.INVISIBLE);
        crC.setVisibility(View.INVISIBLE);
        addresssC.setVisibility(View.INVISIBLE);
        shopNameC.setVisibility(View.INVISIBLE);
        telephoneC.setVisibility(View.INVISIBLE);
    }

    private void setUpAnimation() {
        hideViews();
        final TextView head, belowHead;
        final LinearLayout prefixContainer, dayContainer;

        head = findViewById(R.id.cust_add_head);
        belowHead = findViewById(R.id.cust_add_below_head);

        final TextInputLayout nameC = findViewById(R.id.namec);
        final TextInputLayout phoneC = findViewById(R.id.phonec);
        final TextInputLayout emailC = findViewById(R.id.emailc);
        final TextInputLayout addresssC = findViewById(R.id.addc);
        final TextInputLayout vatNoC = findViewById(R.id.vatc);
        final TextInputLayout crC = findViewById(R.id.crc);
        final TextInputLayout shopNameC = findViewById(R.id.shopc);
        final TextInputLayout telephoneC = findViewById(R.id.telec);

        prefixContainer = findViewById(R.id.cust_add_prefix_cont);
        dayContainer = findViewById(R.id.cust_add_day_container);


        YoYo.with(Techniques.SlideInLeft).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                head.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).duration(400).repeat(0).playOn(head);
                prefixContainer.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInLeft).duration(400).repeat(0).playOn(prefixContainer);
                belowHead.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInUp).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        shopNameC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(300).repeat(0).playOn(shopNameC);

                        emailC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInLeft).duration(300).repeat(0).playOn(emailC);

                        crC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(350).repeat(0).playOn(crC);

                        telephoneC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInLeft).duration(350).repeat(0).playOn(telephoneC);

                        phoneC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(400).repeat(0).playOn(phoneC);

                        vatNoC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInLeft).duration(400).repeat(0).playOn(vatNoC);

                        nameC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInRight).duration(450).repeat(0).playOn(nameC);

                        addresssC.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.SlideInLeft).withListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                dayContainer.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInUp).duration(300).repeat(0).playOn(dayContainer);

                                saveCustomerB.setVisibility(View.VISIBLE);
                                YoYo.with(Techniques.SlideInUp).duration(400).repeat(0).playOn(saveCustomerB);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).duration(450).repeat(0).playOn(addresssC);
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
        }).duration(400).repeat(0).playOn(dayContainer);
    }
}
