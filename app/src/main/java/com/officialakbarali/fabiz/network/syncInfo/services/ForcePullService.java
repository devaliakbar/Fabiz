package com.officialakbarali.fabiz.network.syncInfo.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.officialakbarali.fabiz.data.CommonInformation.getNumberFromDayName;
import static com.officialakbarali.fabiz.data.MyAppVersion.GET_MY_APP_VERSION;
import static com.officialakbarali.fabiz.network.syncInfo.NotificationFrame.CHANNEL_ID;

public class ForcePullService extends Service {
    private static String pullTimeStamp;
    String userName;
    String userId;
    String mySignature;
    public static String FORCE_SYNC_BROADCAST_URL = "force_services.uiUpdateBroadcast";
    RequestQueue requestQueue;
    FabizProvider provider;

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
                .setContentText("Fetching file from server")
                .setSmallIcon(R.mipmap.app_logo)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(2, notification);


        Log.i("ForcePullService :", "Started");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        userName = sharedPreferences.getString("my_username", null);
        userId= sharedPreferences.getInt("idOfStaff", 0) + "";
        mySignature = sharedPreferences.getString("mysign", null);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("force_service_running", true);
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("ForcePullService :", "Destroyed");
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


    private void stopSetUp(String msgToPassed) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("force_service_running", false);
        editor.apply();

        Intent updateUiIntent = new Intent(FORCE_SYNC_BROADCAST_URL);
        updateUiIntent.putExtra("msgPassed", msgToPassed);
        sendBroadcast(updateUiIntent);
        stopSelf();
        Log.i("ForcePullService :", "Executed");
    }

    private void startExecuteThisService() {
        if (userName == null || mySignature == null) {
            stopSetUp("USER");
        } else {
            requestQueue = Volley.newRequestQueue(this);
            checkForUpdate();
        }
    }

    private void checkForUpdate() {
        Log.i("ForcePullService", "Request Sent");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("app_version", "" + GET_MY_APP_VERSION());
        hashMap.put("my_username", "" + userName);
        hashMap.put("userId", "" + userId);
        Log.i("Job", mySignature);
        hashMap.put("mysign", "" + mySignature);

        final VolleyRequest volleyRequest = new VolleyRequest("forcePull.php", hashMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Response :", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                       // pullTimeStamp = jsonObject.getString("pullSignature");
                        addDataToDb(jsonObject);

                    } else {
                        switch (jsonObject.getString("status")) {
                            case "VERSION": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ForcePullService.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("version", true);
                                editor.apply();
                                stopSetUp("VERSION");
                                break;
                            }
                            case "USER": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ForcePullService.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("my_username", null);
                                editor.putString("my_password", null);
                                editor.putBoolean("update_data", false);
                                editor.putBoolean("force_pull", false);
                                editor.apply();
                                stopSetUp("USER");
                                break;
                            }
                            case "PAUSE": {
                                stopSetUp("PAUSE");
                                break;
                            }
                            case "ITEM": {
                                stopSetUp("Server is empty");
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

    private void addDataToDb(JSONObject jsonObject) {
        provider = new FabizProvider(this, true);
        provider.deleteAllTables();
        provider.createTransaction();
        try {
            if (insertItem(jsonObject.getJSONArray(FabizContract.Item.TABLE_NAME))) {
                if (insertCustomer(jsonObject)) {
                    if (insertBillDetail(jsonObject)) {
                        if (insertCart(jsonObject)) {
                            if (insertSalesReturn(jsonObject)) {
                                if (insertPayment(jsonObject)) {
                                    if (insertUnitId(jsonObject)) {
                                        sendConfirmRequest();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        provider.finishTransaction();
        stopSetUp("FAILED");
    }

    private void sendConfirmRequest() {
        provider.successfulTransaction();
        provider.finishTransaction();

        Log.i("SyncLog", "Simple Request");
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("app_version", "" + GET_MY_APP_VERSION());
        hashMap.put("my_username", "" + userName);
        hashMap.put("mysign", "" + mySignature);
        hashMap.put("confirm_pull", "true");

        final VolleyRequest volleyRequest = new VolleyRequest("simple.php", hashMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Response :", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ForcePullService.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("force_pull", false);
                        editor.putBoolean("update_data", false);
                        editor.apply();
                        stopSetUp("SUCCESS");
                    } else {
                        switch (jsonObject.getString("status")) {
                            case "VERSION": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ForcePullService.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("version", true);
                                editor.apply();
                                stopSetUp("VERSION");
                                break;
                            }
                            case "PUSH": {
                                stopSetUp("PUSH");
                                break;
                            }
                            case "FAIL": {
                                stopSetUp("FAILED");
                                break;
                            }
                            case "USER": {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ForcePullService.this);
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

    private boolean insertItem(JSONArray itemArray) throws JSONException {
        boolean thisSuccess = true;
        for (int i = 0; i < itemArray.length(); i++) {
            JSONObject obj = itemArray.getJSONObject(i);

            ContentValues values = new ContentValues();

            try {
                values.put(FabizContract.Item._ID, obj.getInt(FabizContract.Item.TABLE_NAME + FabizContract.Item._ID));
            } catch (NumberFormatException | JSONException | NullPointerException nfe) {
                values.put(FabizContract.Item._ID, obj.getString(FabizContract.Item.TABLE_NAME + FabizContract.Item._ID));
            }

            values.put(FabizContract.Item.COLUMN_UNIT_ID, obj.getString(FabizContract.Item.COLUMN_UNIT_ID));
            values.put(FabizContract.Item.COLUMN_BARCODE, obj.getString(FabizContract.Item.COLUMN_BARCODE));
            values.put(FabizContract.Item.COLUMN_NAME, obj.getString(FabizContract.Item.COLUMN_NAME));
            values.put(FabizContract.Item.COLUMN_BRAND, obj.getString(FabizContract.Item.COLUMN_BRAND));
            values.put(FabizContract.Item.COLUMN_CATEGORY, obj.getString(FabizContract.Item.COLUMN_CATEGORY));
            values.put(FabizContract.Item.COLUMN_PRICE, obj.getString(FabizContract.Item.COLUMN_PRICE));
            provider.insert(FabizContract.Item.TABLE_NAME, values);

        }
        return thisSuccess;
    }

    private boolean insertCustomer(JSONObject jsonObject) throws JSONException {
        boolean thisSuccess = true;
        if (jsonObject.getBoolean(FabizContract.Customer.TABLE_NAME + "status")) {
            JSONArray jsonArray = jsonObject.getJSONArray(FabizContract.Customer.TABLE_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                ContentValues values = new ContentValues();

                try {
                    values.put(FabizContract.Customer._ID, obj.getInt(FabizContract.Customer.TABLE_NAME + FabizContract.Customer._ID));
                } catch (NumberFormatException | NullPointerException | JSONException e) {
                    values.put(FabizContract.Customer._ID, obj.getString(FabizContract.Customer.TABLE_NAME + FabizContract.Customer._ID));
                }

                values.put(FabizContract.Customer.COLUMN_BARCODE, obj.getString(FabizContract.Customer.COLUMN_BARCODE));
                values.put(FabizContract.Customer.COLUMN_CR_NO, obj.getString(FabizContract.Customer.COLUMN_CR_NO));
                values.put(FabizContract.Customer.COLUMN_SHOP_NAME, obj.getString(FabizContract.Customer.COLUMN_SHOP_NAME));
                values.put(FabizContract.Customer.COLUMN_NAME, obj.getString(FabizContract.Customer.COLUMN_NAME));

                int numberOfDay = getNumberFromDayName(obj.getString(FabizContract.Customer.COLUMN_DAY));

                values.put(FabizContract.Customer.COLUMN_DAY, numberOfDay + "");
                values.put(FabizContract.Customer.COLUMN_PHONE, obj.getString(FabizContract.Customer.COLUMN_PHONE));
                values.put(FabizContract.Customer.COLUMN_EMAIL, obj.getString(FabizContract.Customer.COLUMN_EMAIL));
                values.put(FabizContract.Customer.COLUMN_ADDRESS, obj.getString(FabizContract.Customer.COLUMN_ADDRESS));
                values.put(FabizContract.Customer.COLUMN_TELEPHONE, obj.getString(FabizContract.Customer.COLUMN_TELEPHONE));
                values.put(FabizContract.Customer.COLUMN_VAT_NO, obj.getString(FabizContract.Customer.COLUMN_VAT_NO));
                provider.insert(FabizContract.Customer.TABLE_NAME, values);

            }
        }
        return thisSuccess;
    }

    private boolean insertBillDetail(JSONObject jsonObject) throws JSONException {
        boolean thisSuccess = true;
        if (jsonObject.getBoolean(FabizContract.BillDetail.TABLE_NAME + "status")) {
            JSONArray jsonArray = jsonObject.getJSONArray(FabizContract.BillDetail.TABLE_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                ContentValues values = new ContentValues();

                try {
                    values.put(FabizContract.BillDetail._ID, obj.getInt(FabizContract.BillDetail.TABLE_NAME + FabizContract.BillDetail._ID));
                } catch (NumberFormatException | JSONException | NullPointerException nfe) {
                    values.put(FabizContract.BillDetail._ID, obj.getString(FabizContract.BillDetail.TABLE_NAME + FabizContract.BillDetail._ID));
                }


                values.put(FabizContract.BillDetail.COLUMN_DATE, obj.getString(FabizContract.BillDetail.COLUMN_DATE));
                values.put(FabizContract.BillDetail.COLUMN_CUST_ID, obj.getString(FabizContract.BillDetail.COLUMN_CUST_ID));
                values.put(FabizContract.BillDetail.COLUMN_QTY, obj.getString(FabizContract.BillDetail.COLUMN_QTY));
                values.put(FabizContract.BillDetail.COLUMN_PRICE, obj.getString(FabizContract.BillDetail.COLUMN_PRICE));
                values.put(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL, obj.getString(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL));
                values.put(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, obj.getString(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL));
                values.put(FabizContract.BillDetail.COLUMN_PAID, obj.getString(FabizContract.BillDetail.COLUMN_PAID));
                values.put(FabizContract.BillDetail.COLUMN_DUE, obj.getString(FabizContract.BillDetail.COLUMN_DUE));
                values.put(FabizContract.BillDetail.COLUMN_DISCOUNT, obj.getString(FabizContract.BillDetail.COLUMN_DISCOUNT));
                provider.insert(FabizContract.BillDetail.TABLE_NAME, values);

            }
        }
        return thisSuccess;
    }

    private boolean insertCart(JSONObject jsonObject) throws JSONException {
        boolean thisSuccess = true;
        if (jsonObject.getBoolean(FabizContract.Cart.TABLE_NAME + "status")) {
            JSONArray jsonArray = jsonObject.getJSONArray(FabizContract.Cart.TABLE_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                ContentValues values = new ContentValues();

                try {
                    values.put(FabizContract.Cart._ID, obj.getInt(FabizContract.Cart.TABLE_NAME + FabizContract.Cart._ID));
                } catch (NumberFormatException | JSONException | NullPointerException nfe) {
                    values.put(FabizContract.Cart._ID, obj.getString(FabizContract.Cart.TABLE_NAME + FabizContract.Cart._ID));
                }


                values.put(FabizContract.Cart.COLUMN_BILL_ID, obj.getString(FabizContract.Cart.COLUMN_BILL_ID));
                values.put(FabizContract.Cart.COLUMN_ITEM_ID, obj.getString(FabizContract.Cart.COLUMN_ITEM_ID));
                values.put(FabizContract.Cart.COLUMN_UNIT_ID, obj.getString(FabizContract.Cart.COLUMN_UNIT_ID));
                values.put(FabizContract.Cart.COLUMN_NAME, obj.getString(FabizContract.Cart.COLUMN_NAME));
                values.put(FabizContract.Cart.COLUMN_BRAND, obj.getString(FabizContract.Cart.COLUMN_BRAND));
                values.put(FabizContract.Cart.COLUMN_CATEGORY, obj.getString(FabizContract.Cart.COLUMN_CATEGORY));
                values.put(FabizContract.Cart.COLUMN_PRICE, obj.getString(FabizContract.Cart.COLUMN_PRICE));
                values.put(FabizContract.Cart.COLUMN_QTY, obj.getString(FabizContract.Cart.COLUMN_QTY));
                values.put(FabizContract.Cart.COLUMN_TOTAL, obj.getString(FabizContract.Cart.COLUMN_TOTAL));
                values.put(FabizContract.Cart.COLUMN_RETURN_QTY, obj.getString(FabizContract.Cart.COLUMN_RETURN_QTY));
                provider.insert(FabizContract.Cart.TABLE_NAME, values);

            }
        }
        return thisSuccess;
    }

    private boolean insertSalesReturn(JSONObject jsonObject) throws JSONException {
        boolean thisSuccess = true;
        if (jsonObject.getBoolean(FabizContract.SalesReturn.TABLE_NAME + "status")) {
            JSONArray jsonArray = jsonObject.getJSONArray(FabizContract.SalesReturn.TABLE_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                ContentValues values = new ContentValues();

                try {
                    values.put(FabizContract.SalesReturn._ID, obj.getInt(FabizContract.SalesReturn.TABLE_NAME + FabizContract.SalesReturn._ID));
                } catch (NumberFormatException | JSONException | NullPointerException nfe) {
                    values.put(FabizContract.SalesReturn._ID, obj.getString(FabizContract.SalesReturn.TABLE_NAME + FabizContract.SalesReturn._ID));
                }


                values.put(FabizContract.SalesReturn.COLUMN_DATE, obj.getString(FabizContract.SalesReturn.COLUMN_DATE));
                values.put(FabizContract.SalesReturn.COLUMN_BILL_ID, obj.getString(FabizContract.SalesReturn.COLUMN_BILL_ID));
                values.put(FabizContract.SalesReturn.COLUMN_ITEM_ID, obj.getString(FabizContract.SalesReturn.COLUMN_ITEM_ID));

                values.put(FabizContract.SalesReturn.COLUMN_UNIT_ID, obj.getString(FabizContract.SalesReturn.COLUMN_UNIT_ID));

                values.put(FabizContract.SalesReturn.COLUMN_PRICE, obj.getString(FabizContract.SalesReturn.COLUMN_PRICE));
                values.put(FabizContract.SalesReturn.COLUMN_QTY, obj.getString(FabizContract.SalesReturn.COLUMN_QTY));
                values.put(FabizContract.SalesReturn.COLUMN_TOTAL, obj.getString(FabizContract.SalesReturn.COLUMN_TOTAL));
                provider.insert(FabizContract.SalesReturn.TABLE_NAME, values);

            }
        }
        return thisSuccess;
    }

    private boolean insertPayment(JSONObject jsonObject) throws JSONException {
        boolean thisSuccess = true;
        if (jsonObject.getBoolean(FabizContract.Payment.TABLE_NAME + "status")) {
            JSONArray jsonArray = jsonObject.getJSONArray(FabizContract.Payment.TABLE_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                ContentValues values = new ContentValues();

                try {
                    values.put(FabizContract.Payment._ID, obj.getInt(FabizContract.Payment.TABLE_NAME + FabizContract.Payment._ID));
                } catch (NumberFormatException | JSONException | NullPointerException nfe) {
                    values.put(FabizContract.Payment._ID, obj.getString(FabizContract.Payment.TABLE_NAME + FabizContract.Payment._ID));
                }
                values.put(FabizContract.Payment.COLUMN_BILL_ID, obj.getString(FabizContract.Payment.COLUMN_BILL_ID));
                values.put(FabizContract.Payment.COLUMN_DATE, obj.getString(FabizContract.Payment.COLUMN_DATE));
                values.put(FabizContract.Payment.COLUMN_AMOUNT, obj.getString(FabizContract.Payment.COLUMN_AMOUNT));
                values.put(FabizContract.Payment.COLUMN_TYPE, obj.getString(FabizContract.Payment.COLUMN_TYPE));
                provider.insert(FabizContract.Payment.TABLE_NAME, values);
            }
        }
        return thisSuccess;
    }

    private boolean insertUnitId(JSONObject jsonObject) throws JSONException {
        boolean thisSuccess = true;
        if (jsonObject.getBoolean(FabizContract.ItemUnit.TABLE_NAME + "status")) {
            JSONArray jsonArray = jsonObject.getJSONArray(FabizContract.ItemUnit.TABLE_NAME);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                ContentValues values = new ContentValues();

                try {
                    values.put(FabizContract.ItemUnit._ID, obj.getInt(FabizContract.ItemUnit.TABLE_NAME + FabizContract.ItemUnit._ID));
                } catch (NumberFormatException | JSONException | NullPointerException nfe) {
                    values.put(FabizContract.ItemUnit._ID, obj.getString(FabizContract.ItemUnit.TABLE_NAME + FabizContract.ItemUnit._ID));
                }
                values.put(FabizContract.ItemUnit.COLUMN_UNIT_NAME, obj.getString(FabizContract.ItemUnit.COLUMN_UNIT_NAME));
                values.put(FabizContract.ItemUnit.COLUMN_QTY, obj.getInt(FabizContract.ItemUnit.COLUMN_QTY));

                provider.insert(FabizContract.ItemUnit.TABLE_NAME, values);
            }
        }


        return thisSuccess;
    }
}
