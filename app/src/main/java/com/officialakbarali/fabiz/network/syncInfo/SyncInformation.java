package com.officialakbarali.fabiz.network.syncInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.officialakbarali.fabiz.LogIn;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.ServiceResumeCheck;
import com.officialakbarali.fabiz.blockPages.AppVersion;
import com.officialakbarali.fabiz.blockPages.ForcePull;
import com.officialakbarali.fabiz.blockPages.UpdateData;
import com.officialakbarali.fabiz.network.syncInfo.services.SyncService;

import pl.droidsonroids.gif.GifImageView;

import static com.officialakbarali.fabiz.network.syncInfo.services.SyncService.SYNC_BROADCAST_URL;

public class SyncInformation extends AppCompatActivity {
    TextView checkingText;
    Button checkUpdationButton;

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_information);

        checkingText = findViewById(R.id.checking);
        checkUpdationButton = findViewById(R.id.check_for_update);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msgPassed = intent.getExtras().getString("msgPassed");

                if (msgPassed.matches("VERSION")) {
                    Intent versionIntent = new Intent(SyncInformation.this, AppVersion.class);
                    versionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(versionIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else if (msgPassed.matches("PUSH")) {
                    Intent forcePullIntent = new Intent(SyncInformation.this, ForcePull.class);
                    forcePullIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(forcePullIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else if (msgPassed.matches("UPDATE")) {
                    Intent updateDataIntent = new Intent(SyncInformation.this, UpdateData.class);
                    updateDataIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(updateDataIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else if (msgPassed.matches("USER")) {
                    Intent logIntent = new Intent(SyncInformation.this, LogIn.class);
                    logIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    checkingText.setVisibility(View.VISIBLE);
                    checkingText.setText(msgPassed);
                    checkUpdationButton.setVisibility(View.GONE);
                }
                showLoading(false);
            }
        };
        checkUpdationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncInformation.this);


                String userName = sharedPreferences.getString("my_username", null);
                String password = sharedPreferences.getString("my_password", null);

                if (userName != null && password != null) {
                    boolean isServiceRunning = sharedPreferences.getBoolean("service_running", false);
                    if (!isServiceRunning) {
                        Intent serviceIntent = new Intent(getBaseContext(), SyncService.class);
                        ContextCompat.startForegroundService(getBaseContext(), serviceIntent);
                    }
                    checkUpdationButton.setVisibility(View.GONE);
                    checkingText.setVisibility(View.VISIBLE);
                    showLoading(true);
                } else {
                    Intent loginIntent = new Intent(SyncInformation.this, LogIn.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(SYNC_BROADCAST_URL));

        new ServiceResumeCheck(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isServiceRunning = sharedPreferences.getBoolean("service_running", false);
        if (isServiceRunning) {
            showLoading(true);
            checkUpdationButton.setVisibility(View.GONE);
            checkingText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showLoading(boolean show) {
        GifImageView loadingV = findViewById(R.id.loading);
        ImageView normalImage = findViewById(R.id.nImage);
        if (show) {
            loadingV.setVisibility(View.VISIBLE);
            normalImage.setVisibility(View.GONE);
        } else {
            loadingV.setVisibility(View.GONE);
            normalImage.setVisibility(View.VISIBLE);
        }
    }
}
