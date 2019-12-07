package com.officialakbarali.fabiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;

import static com.officialakbarali.fabiz.data.CommonInformation.SET_DECIMAL_LENGTH;
import static com.officialakbarali.fabiz.data.CommonInformation.setCurrency;

public class Settings extends AppCompatActivity {
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final Button logOut = findViewById(R.id.log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        ImageButton saveCurrencyBtn = findViewById(R.id.save_currency);
        saveCurrencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText currecyText = findViewById(R.id.currency);
                String currency = currecyText.getText().toString().trim().toUpperCase();
                if (currency.length() > 0 && currency.length() <= 3) {
                    SharedPreferences
                            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("currency", currency);
                    editor.apply();
                    setCurrency(currency);
                }
                Intent mainHomeIntent = new Intent(Settings.this, MainHome.class);
                mainHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainHomeIntent);
            }
        });

        Button pre2 = findViewById(R.id.pre_2);
        pre2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);
                final SharedPreferences.Editor editor = sharedPreferences.edit();

                final Intent mainHomeIntent = new Intent(Settings.this, MainHome.class);
                mainHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                editor.putInt("decimal_precision", 2);
                SET_DECIMAL_LENGTH(2);
                editor.apply();
                startActivity(mainHomeIntent);
            }
        });
        Button pre3 = findViewById(R.id.pre_3);
        pre3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Settings.this);
                final SharedPreferences.Editor editor = sharedPreferences.edit();

                final Intent mainHomeIntent = new Intent(Settings.this, MainHome.class);
                mainHomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                editor.putInt("decimal_precision", 3);
                SET_DECIMAL_LENGTH(3);
                editor.apply();
                startActivity(mainHomeIntent);
            }
        });
        setUpCurrencyEditText();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new ServiceResumeCheck(this);
    }

    private void setUpCurrencyEditText() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        EditText currecyText = findViewById(R.id.currency);
        currecyText.setText(sharedPreferences.getString("currency", "BD"));
    }

    public void logout() {
        FabizProvider provider = new FabizProvider(this, false);
        Cursor cursor = provider.query(FabizContract.SyncLog.TABLE_NAME, new String[]{FabizContract.SyncLog._ID}, null, null, null);
        if (cursor.getCount() > 0) {
            showToast("Some data need to be sync! Please try after sometime");
        } else {
            SharedPreferences
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("my_username", null);
            editor.putString("my_password", null);
            editor.putBoolean("update_data", false);
            editor.putBoolean("force_pull", false);
            editor.apply();

            Intent logIntent = new Intent(this, LogIn.class);
            logIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logIntent);
        }
    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
    }
}
