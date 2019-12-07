package com.officialakbarali.fabiz.network.syncInfo.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.NetworkError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.officialakbarali.fabiz.MainActivity;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.network.VolleyRequest;
import com.officialakbarali.fabiz.network.syncInfo.data.HashMapHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.officialakbarali.fabiz.data.MyAppVersion.GET_MY_APP_VERSION;
import static com.officialakbarali.fabiz.network.syncInfo.NotificationFrame.CHANNEL_ID;

public class SyncService extends Service {
    FabizProvider provider;

    RequestQueue requestQueue;
    private List<String> logDetailsList;
    private int indexOfLog, lengthOfLogList;

    public static String SYNC_BROADCAST_URL = "services.uiUpdateBroadcast";


    String userName;
    String mySignature;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fabiz Sync")
                .setContentText("File Uploading")
                .setSmallIcon(R.mipmap.app_logo)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        Log.i("SyncService :", "Started");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userName = sharedPreferences.getString("my_username", null);
        mySignature = sharedPreferences.getString("mysign", null);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("service_running", true);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SyncService :", "Destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startExecuteThisService();
            }
        }).start();
        return START_STICKY;
    }

    private void startExecuteThisService() {

        if (userName == null || mySignature == null) {
            stopSetUp("USER");
        } else {
            requestQueue = Volley.newRequestQueue(this);
            provider = new FabizProvider(getBaseContext(), true);
            Cursor syncCursor = provider.queryExplicit(
                    true,
                    FabizContract.SyncLog.TABLE_NAME,
                    new String[]{FabizContract.SyncLog.COLUMN_TIMESTAMP},
                    null, null,
                    null,
                    null,
                    FabizContract.SyncLog._ID + " ASC",
                    null);
            lengthOfLogList = syncCursor.getCount();
            if (lengthOfLogList > 0) {
                indexOfLog = 0;
                logDetailsList = new ArrayList<>();
                while (syncCursor.moveToNext()) {
                    logDetailsList.add(syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_TIMESTAMP)));
                }
                sendToServer();
            } else {
                checkForUpdate();
            }
        }
    }

    private void sendToServer() {
        Log.i("SyncLog", "Main Request");
        String timeStampOfTransaction = logDetailsList.get(indexOfLog);
        HashMapHelper hashMapHelper = new HashMapHelper(this, timeStampOfTransaction);
        HashMap<String, String> hashMap = hashMapHelper.getHashmapForSync();
        if (hashMap != null) {
            final VolleyRequest volleyRequest = new VolleyRequest("sync.php", hashMap, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("Response :", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            if (deleteRowFromSyncLog(indexOfLog)) {
                                indexOfLog++;
                                Log.i("SyncLog", indexOfLog + " Synced");
                                if (indexOfLog == lengthOfLogList) {
                                    stopSetUp("Sync Successfully");
                                } else {
                                    sendToServer();
                                }
                            } else {
                                stopSetUp("Failed to update local db");
                            }
                        } else {
                            switch (jsonObject.getString("status")) {
                                case "VERSION": {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("version", true);
                                    editor.apply();
                                    stopSetUp("VERSION");
                                    break;
                                }
                                case "PUSH": {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("force_pull", true);
                                    editor.apply();
                                    stopSetUp("PUSH");
                                    break;
                                }
                                case "UPDATE": {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("update_data", true);
                                    editor.apply();
                                    stopSetUp("UPDATE");
                                    break;
                                }
                                case "USER": {
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("my_username", null);
                                    editor.putString("my_password", null);
                                    editor.putBoolean("update_data", false);
                                    editor.putBoolean("force_pull", false);
                                    editor.apply();
                                    stopSetUp("USER");
                                    break;
                                }
                                default:
                                    stopSetUp("Something went wrong");
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        stopSetUp("Bad Response From Server");
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof ServerError) {
                        stopSetUp("Server Error");
                    } else if (error instanceof TimeoutError) {
                        stopSetUp("Connection Timed Out");
                    } else if (error instanceof NetworkError) {
                        stopSetUp("Bad Network Connection");
                    }
                }
            });
            requestQueue.add(volleyRequest);
        } else {
            if (deleteRowFromSyncLog(indexOfLog)) {
                indexOfLog++;
                if (indexOfLog == lengthOfLogList) {
                    stopSetUp("Sync Successfully");
                } else {
                    sendToServer();
                }
            } else {
                stopSetUp("Failed to update local db");
            }
        }
    }

    private boolean deleteRowFromSyncLog(int indexToDelete) {
        String deleteRowsTimestampId = logDetailsList.get(indexToDelete);
        int deleteCount = provider.delete(FabizContract.SyncLog.TABLE_NAME, FabizContract.SyncLog.COLUMN_TIMESTAMP + "=?", new String[]{deleteRowsTimestampId});
        return deleteCount > 0;
    }

    private void checkForUpdate() {
        Log.i("SyncLog", "Simple Request");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("app_version", "" + GET_MY_APP_VERSION());
        hashMap.put("my_username", "" + userName);
        hashMap.put("mysign", "" + mySignature);
        hashMap.put("confirm_pull", "false");

        final VolleyRequest volleyRequest = new VolleyRequest("simple.php", hashMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Response :", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                        stopSetUp("Sync Successfully");
                    } else {
                        switch (jsonObject.getString("status")) {
                            case "VERSION": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("version", true);
                                editor.apply();
                                stopSetUp("VERSION");
                                break;
                            }
                            case "PUSH": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("force_pull", true);
                                editor.apply();
                                stopSetUp("PUSH");
                                break;
                            }
                            case "UPDATE": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("update_data", true);
                                editor.apply();
                                stopSetUp("UPDATE");
                                break;
                            }
                            case "USER": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SyncService.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("my_username", null);
                                editor.putString("my_password", null);
                                editor.putBoolean("update_data", false);
                                editor.putBoolean("force_pull", false);
                                editor.apply();
                                stopSetUp("USER");
                                break;
                            }
                            default:
                                stopSetUp("Something went wrong");
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    stopSetUp("Bad Response From Server");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof ServerError) {
                    stopSetUp("Server Error");
                } else if (error instanceof TimeoutError) {
                    stopSetUp("Connection Timed Out");
                } else if (error instanceof NetworkError) {
                    stopSetUp("Bad Network Connection");
                }
            }
        });
        requestQueue.add(volleyRequest);
    }

    private void stopSetUp(String msgToPassed) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("service_running", false);
        editor.apply();

        Intent updateUiIntent = new Intent(SYNC_BROADCAST_URL);
        updateUiIntent.putExtra("msgPassed", msgToPassed);
        sendBroadcast(updateUiIntent);

        stopSelf();

        Log.i("SyncService :", "Executed");
    }
}
