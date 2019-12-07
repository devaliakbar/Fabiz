package com.officialakbarali.fabiz.network.syncInfo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class NetworkMonitor extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Connection Status","Okay");
        if (isNetworkConnected(context)) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isServiceRunning = sharedPreferences.getBoolean("service_running", false);
            if (!isServiceRunning) {
                Intent serviceIntent = new Intent(context, SyncService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }

    private boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
