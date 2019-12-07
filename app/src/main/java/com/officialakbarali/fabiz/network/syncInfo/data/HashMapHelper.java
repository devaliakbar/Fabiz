package com.officialakbarali.fabiz.network.syncInfo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.officialakbarali.fabiz.data.CommonInformation.getDayNameFromNumber;
import static com.officialakbarali.fabiz.data.MyAppVersion.GET_MY_APP_VERSION;

public class HashMapHelper {
    private Context context;
    List<SyncLogData> list;
    private FabizProvider provider;

    public HashMapHelper(Context context, String timeStampOfTransaction) {
        this.context = context;
        provider = new FabizProvider(context, false);
        Cursor syncCursor = provider.query(FabizContract.SyncLog.TABLE_NAME, new String[]{
                FabizContract.SyncLog._ID, FabizContract.SyncLog.COLUMN_OP_CODE, FabizContract.SyncLog.COLUMN_TABLE_NAME,
                FabizContract.SyncLog.COLUMN_ROW_ID, FabizContract.SyncLog.COLUMN_OPERATION, FabizContract.SyncLog.COLUMN_TIMESTAMP,
        }, FabizContract.SyncLog.COLUMN_TIMESTAMP + "=?", new String[]{timeStampOfTransaction}, FabizContract.SyncLog._ID + " ASC");

        list = new ArrayList<>();
        while (syncCursor.moveToNext()) {
            list.add(new SyncLogData(syncCursor.getInt(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog._ID)),
                    syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_ROW_ID)),
                    syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_TABLE_NAME)),
                    syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_OPERATION)),
                    syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_OP_CODE)),
                    syncCursor.getString(syncCursor.getColumnIndexOrThrow(FabizContract.SyncLog.COLUMN_TIMESTAMP))
            ));
        }
    }

    public HashMap<String, String> getHashmapForSync() {
        HashMap<String, String> returnHashMap = new HashMap<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String userName = sharedPreferences.getString("my_username", null);
        String mySignature = sharedPreferences.getString("mysign", null);
        String staffId = sharedPreferences.getInt("idOfStaff", 0) + "";

        returnHashMap.put("app_version", "" + GET_MY_APP_VERSION());
        returnHashMap.put("my_username", "" + userName);
        returnHashMap.put("mysign", "" + mySignature);
        returnHashMap.put("staff_id", "" + staffId);
        returnHashMap.put("OP_CODE", "" + list.get(0).getOpCode());
        returnHashMap.put("time_stamp", "" + list.get(0).getTimestamp());
        switch (list.get(0).getOpCode()) {
            case "ADD_CUST":
                returnHashMap = getAddCustMap(returnHashMap);
                break;

            case "SALE":
                returnHashMap = getSaleMap(returnHashMap);
                break;

            case "PAY":
                returnHashMap = getPayMap(returnHashMap);
                break;

            case "SALE_RETURN":
                returnHashMap = getSalesReturnMap(returnHashMap);
                break;
        }

        return returnHashMap;
    }

    private HashMap<String, String> getAddCustMap(HashMap<String, String> hashMap) {
        SyncLogData syncLogData = list.get(0);
        Cursor custCursor = provider.query(FabizContract.Customer.TABLE_NAME,
                new String[]{
                        FabizContract.Customer._ID, FabizContract.Customer.COLUMN_CR_NO, FabizContract.Customer.COLUMN_SHOP_NAME, FabizContract.Customer.COLUMN_BARCODE,
                        FabizContract.Customer.COLUMN_DAY, FabizContract.Customer.COLUMN_NAME, FabizContract.Customer.COLUMN_PHONE, FabizContract.Customer.COLUMN_EMAIL,
                        FabizContract.Customer.COLUMN_ADDRESS, FabizContract.Customer.COLUMN_VAT_NO, FabizContract.Customer.COLUMN_TELEPHONE
                },
                FabizContract.Customer._ID + "=?",
                new String[]{
                        syncLogData.getRawId() + ""
                }, null);

        if (custCursor.moveToNext()) {
            hashMap.put(FabizContract.Customer.TABLE_NAME + FabizContract.Customer._ID, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer._ID)));
            hashMap.put(FabizContract.Customer.COLUMN_CR_NO, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_CR_NO)));
            hashMap.put(FabizContract.Customer.COLUMN_SHOP_NAME, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_SHOP_NAME)));
            hashMap.put(FabizContract.Customer.COLUMN_BARCODE, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_BARCODE)));
            String dayToUp = custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_DAY));
            if (dayToUp.matches("NA")) {
                dayToUp = getDayNameFromNumber(Calendar.MONDAY + "");
            } else {
                dayToUp = getDayNameFromNumber(dayToUp);
            }
            hashMap.put(FabizContract.Customer.COLUMN_DAY, dayToUp);
            hashMap.put(FabizContract.Customer.COLUMN_NAME, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_NAME)));
            hashMap.put(FabizContract.Customer.COLUMN_PHONE, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_PHONE)));
            hashMap.put(FabizContract.Customer.COLUMN_EMAIL, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_EMAIL)));
            hashMap.put(FabizContract.Customer.COLUMN_ADDRESS, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_ADDRESS)));
            hashMap.put(FabizContract.Customer.COLUMN_VAT_NO, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_VAT_NO)));
            hashMap.put(FabizContract.Customer.COLUMN_TELEPHONE, custCursor.getString(custCursor.getColumnIndexOrThrow(FabizContract.Customer.COLUMN_TELEPHONE)));
        }
        return hashMap;
    }

    private HashMap<String, String> getSaleMap(HashMap<String, String> hashMap) {
        int cartRowIndex = 0, paymentLength = 0;
        for (int i = 0; i < list.size(); i++) {
            SyncLogData syncLogData = list.get(i);
            if (syncLogData.getTableName().matches(FabizContract.BillDetail.TABLE_NAME)) {
                Cursor billCursor = provider.query(FabizContract.BillDetail.TABLE_NAME,
                        new String[]{FabizContract.BillDetail._ID, FabizContract.BillDetail.COLUMN_CUST_ID, FabizContract.BillDetail.COLUMN_DATE
                                , FabizContract.BillDetail.COLUMN_QTY, FabizContract.BillDetail.COLUMN_PRICE, FabizContract.BillDetail.COLUMN_RETURNED_TOTAL
                                , FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, FabizContract.BillDetail.COLUMN_PAID, FabizContract.BillDetail.COLUMN_DUE
                                , FabizContract.BillDetail.COLUMN_DISCOUNT},
                        FabizContract.BillDetail._ID + "=?",
                        new String[]{
                                syncLogData.getRawId() + ""
                        }, null);

                if (billCursor.moveToNext()) {
                    hashMap.put(FabizContract.BillDetail.TABLE_NAME + FabizContract.BillDetail._ID, billCursor.getString(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail._ID)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_CUST_ID, billCursor.getString(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_CUST_ID)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_DATE, billCursor.getString(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DATE)));
                    hashMap.put(FabizContract.BillDetail.COLUMN_QTY, billCursor.getInt(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_QTY)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_PRICE, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PRICE)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_PAID, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PAID)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_DUE, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DUE)) + "");

                    hashMap.put(FabizContract.BillDetail.COLUMN_DISCOUNT, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DISCOUNT)) + "");
                }


            } else if (syncLogData.getTableName().matches(FabizContract.Cart.TABLE_NAME)) {

                Cursor cartCursor = provider.query(FabizContract.Cart.TABLE_NAME, new String[]{
                        FabizContract.Cart._ID, FabizContract.Cart.COLUMN_BILL_ID, FabizContract.Cart.COLUMN_ITEM_ID, FabizContract.Cart.COLUMN_UNIT_ID, FabizContract.Cart.COLUMN_NAME,
                        FabizContract.Cart.COLUMN_BRAND, FabizContract.Cart.COLUMN_CATEGORY, FabizContract.Cart.COLUMN_PRICE,
                        FabizContract.Cart.COLUMN_QTY, FabizContract.Cart.COLUMN_TOTAL, FabizContract.Cart.COLUMN_RETURN_QTY,
                }, FabizContract.Cart._ID + "=?", new String[]{syncLogData.getRawId() + ""}, null);

                if (cartCursor.moveToNext()) {
                    hashMap.put(FabizContract.Cart.TABLE_NAME + FabizContract.Cart._ID + cartRowIndex, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart._ID)) + "");
                    hashMap.put(FabizContract.Cart.COLUMN_BILL_ID + cartRowIndex, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_BILL_ID)) + "");
                    hashMap.put(FabizContract.Cart.COLUMN_ITEM_ID + cartRowIndex, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_ITEM_ID)) + "");
                    hashMap.put(FabizContract.Cart.COLUMN_UNIT_ID + cartRowIndex, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_UNIT_ID)));
                    hashMap.put(FabizContract.Cart.COLUMN_NAME + cartRowIndex, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_NAME)));
                    hashMap.put(FabizContract.Cart.COLUMN_BRAND + cartRowIndex, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_BRAND)));
                    hashMap.put(FabizContract.Cart.COLUMN_CATEGORY + cartRowIndex, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_CATEGORY)));
                    hashMap.put(FabizContract.Cart.COLUMN_PRICE + cartRowIndex, cartCursor.getDouble(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_PRICE)) + "");
                    hashMap.put(FabizContract.Cart.COLUMN_QTY + cartRowIndex, cartCursor.getInt(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_QTY)) + "");
                    hashMap.put(FabizContract.Cart.COLUMN_TOTAL + cartRowIndex, cartCursor.getDouble(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_TOTAL)) + "");
                    hashMap.put(FabizContract.Cart.COLUMN_RETURN_QTY + cartRowIndex, cartCursor.getDouble(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_RETURN_QTY)) + "");
                }
                cartRowIndex++;
            } else if (syncLogData.getTableName().matches(FabizContract.Payment.TABLE_NAME)) {

                Cursor payCursor = provider.query(FabizContract.Payment.TABLE_NAME, new String[]{FabizContract.Payment._ID
                                , FabizContract.Payment.COLUMN_BILL_ID, FabizContract.Payment.COLUMN_DATE, FabizContract.Payment.COLUMN_AMOUNT,FabizContract.Payment.COLUMN_TYPE
                        },
                        FabizContract.Payment._ID + "=?", new String[]{syncLogData.getRawId() + ""},
                        null);


                if (payCursor.moveToNext()) {
                    hashMap.put(FabizContract.Payment.TABLE_NAME + FabizContract.Payment._ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment._ID)) + "");
                    hashMap.put(FabizContract.Payment.COLUMN_BILL_ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_BILL_ID)) + "");
                    hashMap.put(FabizContract.Payment.COLUMN_DATE, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_DATE)));
                    hashMap.put(FabizContract.Payment.COLUMN_AMOUNT, payCursor.getDouble(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_AMOUNT)) + "");
                    hashMap.put(FabizContract.Payment.COLUMN_TYPE, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_TYPE)));
                }

                paymentLength++;
            }
            hashMap.put("cart_length", cartRowIndex + "");
            hashMap.put("payment_length", paymentLength + "");
        }
        return hashMap;
    }


    private HashMap<String, String> getPayMap(HashMap<String, String> hashMap) {
        for (int i = 0; i < list.size(); i++) {
            SyncLogData syncLogData = list.get(i);
            if (syncLogData.getTableName().matches(FabizContract.BillDetail.TABLE_NAME)) {
                Cursor billCursor = provider.query(FabizContract.BillDetail.TABLE_NAME,
                        new String[]{FabizContract.BillDetail._ID, FabizContract.BillDetail.COLUMN_PAID, FabizContract.BillDetail.COLUMN_DUE, FabizContract.BillDetail.COLUMN_DISCOUNT},
                        FabizContract.BillDetail._ID + "=?",
                        new String[]{
                                syncLogData.getRawId() + ""
                        }, null);
                if (billCursor.moveToNext()) {
                    hashMap.put(FabizContract.BillDetail.TABLE_NAME + FabizContract.BillDetail._ID, billCursor.getString(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail._ID)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_PAID, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_PAID)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_DUE, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DUE)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_DISCOUNT, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DISCOUNT)) + "");
                }

            } else if (syncLogData.getTableName().matches(FabizContract.Payment.TABLE_NAME)) {
                Cursor payCursor = provider.query(FabizContract.Payment.TABLE_NAME, new String[]{FabizContract.Payment._ID
                                , FabizContract.Payment.COLUMN_BILL_ID, FabizContract.Payment.COLUMN_DATE, FabizContract.Payment.COLUMN_AMOUNT, FabizContract.Payment.COLUMN_TYPE
                        },
                        FabizContract.Payment._ID + "=?", new String[]{syncLogData.getRawId() + ""},
                        null);
                if (payCursor.moveToNext()) {
                    hashMap.put(FabizContract.Payment.TABLE_NAME + FabizContract.Payment._ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment._ID)) + "");
                    hashMap.put(FabizContract.Payment.COLUMN_BILL_ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_BILL_ID)) + "");
                    hashMap.put(FabizContract.Payment.COLUMN_DATE, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_DATE)));
                    hashMap.put(FabizContract.Payment.COLUMN_AMOUNT, payCursor.getDouble(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_AMOUNT)) + "");
                    hashMap.put(FabizContract.Payment.COLUMN_TYPE, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.Payment.COLUMN_TYPE)) );
                }
            }
        }
        return hashMap;
    }

    private HashMap<String, String> getSalesReturnMap(HashMap<String, String> hashMap) {
        for (int i = 0; i < list.size(); i++) {
            SyncLogData syncLogData = list.get(i);
            if (syncLogData.getTableName().matches(FabizContract.BillDetail.TABLE_NAME)) {
                Cursor billCursor = provider.query(FabizContract.BillDetail.TABLE_NAME,
                        new String[]{FabizContract.BillDetail._ID, FabizContract.BillDetail.COLUMN_RETURNED_TOTAL
                                , FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, FabizContract.BillDetail.COLUMN_DUE},
                        FabizContract.BillDetail._ID + "=?",
                        new String[]{
                                syncLogData.getRawId() + ""
                        }, null);

                if (billCursor.moveToNext()) {
                    hashMap.put(FabizContract.BillDetail.TABLE_NAME + FabizContract.BillDetail._ID, billCursor.getString(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail._ID)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_RETURNED_TOTAL)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_CURRENT_TOTAL)) + "");
                    hashMap.put(FabizContract.BillDetail.COLUMN_DUE, billCursor.getDouble(billCursor.getColumnIndexOrThrow(FabizContract.BillDetail.COLUMN_DUE)) + "");
                }


            } else if (syncLogData.getTableName().matches(FabizContract.Cart.TABLE_NAME)) {

                Cursor cartCursor = provider.query(FabizContract.Cart.TABLE_NAME, new String[]{
                        FabizContract.Cart._ID, FabizContract.Cart.COLUMN_RETURN_QTY,
                }, FabizContract.Cart._ID + "=?", new String[]{syncLogData.getRawId() + ""}, null);

                if (cartCursor.moveToNext()) {
                    hashMap.put(FabizContract.Cart.TABLE_NAME + FabizContract.Cart._ID, cartCursor.getString(cartCursor.getColumnIndexOrThrow(FabizContract.Cart._ID)) + "");
                    hashMap.put(FabizContract.Cart.COLUMN_RETURN_QTY, cartCursor.getDouble(cartCursor.getColumnIndexOrThrow(FabizContract.Cart.COLUMN_RETURN_QTY)) + "");
                }
            } else if (syncLogData.getTableName().matches(FabizContract.SalesReturn.TABLE_NAME)) {

                Cursor payCursor = provider.query(FabizContract.SalesReturn.TABLE_NAME, new String[]{FabizContract.SalesReturn._ID
                                , FabizContract.SalesReturn.COLUMN_BILL_ID, FabizContract.SalesReturn.COLUMN_DATE, FabizContract.SalesReturn.COLUMN_ITEM_ID,
                                FabizContract.SalesReturn.COLUMN_UNIT_ID
                                , FabizContract.SalesReturn.COLUMN_PRICE, FabizContract.SalesReturn.COLUMN_QTY, FabizContract.SalesReturn.COLUMN_TOTAL
                        },
                        FabizContract.SalesReturn._ID + "=?", new String[]{syncLogData.getRawId() + ""},
                        null);


                if (payCursor.moveToNext()) {
                    hashMap.put(FabizContract.SalesReturn.TABLE_NAME + FabizContract.SalesReturn._ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn._ID)) + "");
                    hashMap.put(FabizContract.SalesReturn.COLUMN_BILL_ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_BILL_ID)) + "");
                    hashMap.put(FabizContract.SalesReturn.COLUMN_DATE, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_DATE)));
                    hashMap.put(FabizContract.SalesReturn.COLUMN_ITEM_ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_ITEM_ID)) + "");

                    hashMap.put(FabizContract.SalesReturn.COLUMN_UNIT_ID, payCursor.getString(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_UNIT_ID)));

                    hashMap.put(FabizContract.SalesReturn.COLUMN_PRICE, payCursor.getDouble(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_PRICE)) + "");
                    hashMap.put(FabizContract.SalesReturn.COLUMN_QTY, payCursor.getInt(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_QTY)) + "");
                    hashMap.put(FabizContract.SalesReturn.COLUMN_TOTAL, payCursor.getDouble(payCursor.getColumnIndexOrThrow(FabizContract.SalesReturn.COLUMN_TOTAL)) + "");
                }

            }
        }
        return hashMap;
    }
}
