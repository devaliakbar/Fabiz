package com.officialakbarali.fabiz.network.syncInfo;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.officialakbarali.fabiz.network.syncInfo.services.SyncService;
import com.officialakbarali.fabiz.network.syncInfo.data.SyncLogDetail;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;

import java.util.Date;
import java.util.List;


public class SetupSync {
    //PUBLIC STRING FOR OPERATION
    public static String OP_INSERT = "INSERT";
    public static String OP_UPDATE = "UPDATE";
    public static String OP_DELETE = "DELETE";
    //***************************

    //PUBLIC STRING FOR OPERATION
    public static String OP_CODE_ADD_CUSTOMER = "ADD_CUST";
    public static String OP_CODE_SALE = "SALE";
    public static String OP_CODE_PAY = "PAY";
    public static String OP_CODE_SALE_RETURN = "SALE_RETURN";
    //***************************


    private List<SyncLogDetail> syncLogList;
    private Context context;
    private FabizProvider provider;

    public SetupSync(Context context, List<SyncLogDetail> syncLogList, FabizProvider provider, String successMsg, String opCode) {
        this.context = context;
        this.syncLogList = syncLogList;
        this.provider = provider;

        addCurrentDataToSyncTable(successMsg, opCode);

        if (isNetworkConnected()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isServiceRunning = sharedPreferences.getBoolean("service_running", false);
            if (!isServiceRunning) {
                Intent serviceIntent = new Intent(context, SyncService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            } else {
                //TODO IF ON THEN SETUP SOME FLAG FOR RE-CHECK THE SYNC_TABLE
            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void addCurrentDataToSyncTable(String successMsg, String opCode) {
        Date date = new Date();
        String timeStampToInsert = date.getTime() + "";
        try {
            int i = 0;
            while (i < syncLogList.size()) {
                ContentValues values = new ContentValues();
                values.put(FabizContract.SyncLog.COLUMN_ROW_ID, syncLogList.get(i).getRawId());
                values.put(FabizContract.SyncLog.COLUMN_TABLE_NAME, syncLogList.get(i).getTableName());
                values.put(FabizContract.SyncLog.COLUMN_OPERATION, syncLogList.get(i).getOperation());
                values.put(FabizContract.SyncLog.COLUMN_OP_CODE, opCode);
                values.put(FabizContract.SyncLog.COLUMN_TIMESTAMP, timeStampToInsert);
                long id = provider.insert(FabizContract.SyncLog.TABLE_NAME, values);

                if (id > 0) {
                    Log.i("SetupSync", "Sync Row Created Id:" + id);
                } else {
                    Log.i("SetupSync", "FAILED IN SAVING");
                    Toast.makeText(context, "Something went wrong,please report this to customer care", Toast.LENGTH_SHORT).show();
                    break;
                }
                i++;
            }

            if (i == syncLogList.size()) {
                //********TRANSACTION SUCCESSFUL
                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show();
                Log.i("SetupSync", "SUCCESSFULLY COMPLETED");
                provider.successfulTransaction();
            }

        } catch (Error e) {
            Log.i("SetupSync", "FAILED IN TRY CATCH");
            Toast.makeText(context, "Something went wrong,please report this to customer care", Toast.LENGTH_SHORT).show();
        } finally {
            //******TRANSACTION FINISH
            provider.finishTransaction();
        }
    }
}
