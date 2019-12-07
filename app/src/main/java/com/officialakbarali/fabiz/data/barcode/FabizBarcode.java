package com.officialakbarali.fabiz.data.barcode;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.customer.Home;
import com.officialakbarali.fabiz.customer.sale.data.Cart;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.item.data.ItemDetail;
import com.officialakbarali.fabiz.item.data.UnitData;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;
import static com.officialakbarali.fabiz.customer.sale.Sales.cartItems;
import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;

public class FabizBarcode extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    public static final int FOR_ITEM = 1;
    public static final int FOR_CUSTOMER = 2;

    private int FOR_WHO;

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private Toast toast;
    Intent showCustIntent = null;

    List<UnitData> unitData;

    UnitData myUnitData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FOR_WHO = Integer.parseInt(getIntent().getStringExtra("FOR_WHO"));

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        new CommonResumeCheck(this);

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
        if (showCustIntent != null) {
            startActivity(showCustIntent);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(FabizBarcode.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText();
        FabizProvider provider = new FabizProvider(this, false);
        Cursor cursor;
        if (FOR_WHO == FOR_CUSTOMER) {
            cursor = provider.query(FabizContract.Customer.TABLE_NAME, new String[]{FabizContract.Customer._ID},
                    FabizContract.Customer.COLUMN_BARCODE + "=?", new String[]{myResult}, null);
        } else {
            cursor = provider.query(FabizContract.Item.TABLE_NAME, new String[]{FabizContract.Item._ID, FabizContract.Item.COLUMN_UNIT_ID, FabizContract.Item.COLUMN_NAME, FabizContract.Item.COLUMN_BRAND,
                            FabizContract.Item.COLUMN_CATEGORY, FabizContract.Item.COLUMN_PRICE},
                    FabizContract.Item.COLUMN_BARCODE + "=?", new String[]{myResult}, null);
        }

        if (cursor.moveToNext()) {
            if (FOR_WHO == FOR_CUSTOMER) {
                showCustIntent = new Intent(this, Home.class);
                showCustIntent.putExtra("id", cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.Customer._ID)));
                finish();
            } else {
                enterQtyDialogue(new ItemDetail(cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.Item._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_UNIT_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_BRAND)),
                        cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_CATEGORY)),
                        Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.Item.COLUMN_PRICE)))));
            }
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Barcode doesn't match, do you like to scan again ?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            scannerView.resumeCameraPreview(FabizBarcode.this);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }

    private void enterQtyDialogue(final ItemDetail itemDetail) {
        fillUnitData();

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_customer_sale_item_qty);


        //SETTING SCREEN WIDTH
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************

        final TextView labelText = dialog.findViewById(R.id.cust_sale_add_item_label_pop);
        labelText.setText(String.format("%s / %s / %s", itemDetail.getName(), itemDetail.getBrand(), itemDetail.getCategory()));

        final EditText priceText = dialog.findViewById(R.id.cust_sale_add_item_price);
        priceText.setText(TruncateDecimal(itemDetail.getPrice() + ""));
        final EditText quantityText = dialog.findViewById(R.id.cust_sale_add_item_qty);
        quantityText.setText("1");
        final TextView totalText = dialog.findViewById(R.id.cust_sale_add_item_total);
        totalText.setText(TruncateDecimal(itemDetail.getPrice() + ""));

        priceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String priceS = priceText.getText().toString().trim();
                String qtyS = quantityText.getText().toString().trim();
                String totS = totalText.getText().toString().trim();

                if (conditionsForDialogue(priceS, qtyS, totS)) {
                    double priceToCart = Double.parseDouble(priceS);
                    int quantityToCart = Integer.parseInt(qtyS);
                    double totalToCart = priceToCart * quantityToCart;
                    totalText.setText(TruncateDecimal(totalToCart + ""));
                }
            }
        });

        quantityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String priceS = priceText.getText().toString().trim();
                String qtyS = quantityText.getText().toString().trim();
                String totS = totalText.getText().toString().trim();

                if (conditionsForDialogue(priceS, qtyS, totS)) {
                    double priceToCart = Double.parseDouble(priceS);
                    int quantityToCart = Integer.parseInt(qtyS);
                    double totalToCart = priceToCart * quantityToCart;
                    totalText.setText(TruncateDecimal(totalToCart + ""));
                }
            }
        });


        Button addItemButton = dialog.findViewById(R.id.cust_sale_add_item_add);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String priceS = priceText.getText().toString().trim();
                String qtyS = quantityText.getText().toString().trim();
                String totS = totalText.getText().toString().trim();
                if (conditionsForDialogue(priceS, qtyS, totS)) {
                    if (checkAlreadyExist(itemDetail.getId(), myUnitData.getId())) {
                        showToast(itemDetail.getName() + " Replaced Successfully");
                    }
                    cartItems.add(new Cart("0", "0", itemDetail.getId(), myUnitData.getId(), myUnitData.getUnitName(), itemDetail.getName(), itemDetail.getBrand(), itemDetail.getCategory(),
                            Double.parseDouble(priceS), Integer.parseInt(qtyS), Double.parseDouble(totS), 0));
                    finish();
                } else {
                    showToast("Please enter valid number");
                }
                dialog.dismiss();
            }
        });

        Button cancelDialogue = dialog.findViewById(R.id.cust_sale_add_item_cancel);
        cancelDialogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        //**************************************SETTING UP SPINNER
        List<String> spinnerData = new ArrayList<>();
        for (int i = 0; i < unitData.size(); i++) {
            UnitData temp = unitData.get(i);
            spinnerData.add(temp.getUnitName());
        }

        final Spinner unitS = dialog.findViewById(R.id.spinner_unit);
        unitS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                myUnitData = unitData.get(position);
                double iPrice = itemDetail.getPrice() * myUnitData.getQty();
                priceText.setText(TruncateDecimal(iPrice + ""));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.custom_spinner_item, spinnerData);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_item);
        unitS.setAdapter(spinnerAdapter);

        dialog.show();
    }

    private void fillUnitData() {
        FabizProvider provider = new FabizProvider(this, false);
        Cursor cursor = provider.query(FabizContract.ItemUnit.TABLE_NAME, new String[]{FabizContract.ItemUnit.FULL_COLUMN_ID, FabizContract.ItemUnit.COLUMN_UNIT_NAME,
                FabizContract.ItemUnit.COLUMN_QTY}, null, null, null);
        unitData = new ArrayList<>();
        while (cursor.moveToNext()) {
            unitData.add(new UnitData(cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit._ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_UNIT_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(FabizContract.ItemUnit.COLUMN_QTY))));
        }
        myUnitData = unitData.get(0);
    }

    private boolean conditionsForDialogue(String s1, String s2, String s3) {
        if (s1.matches("") || s2.matches("") ||
                s3.matches("")) {
            return false;
        } else {
            try {
                double priceToCart = Double.parseDouble(s1);
                int quantityToCart = Integer.parseInt(s2);
                double totalToCart = Double.parseDouble(s3);

                if (priceToCart > 0 && quantityToCart > 0 && totalToCart > 0) {
                    return true;
                } else {
                    return false;
                }
            } catch (Error e) {
                return false;
            }
        }
    }

    private void showToast(String msgForToast) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msgForToast, Toast.LENGTH_LONG);
        toast.show();
    }

    private boolean checkAlreadyExist(String itemId, String unitId) {
        boolean alredyExits = false;
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getItemId().matches(itemId) && cartItems.get(i).getUnitId().matches(unitId)) {
                alredyExits = true;
                cartItems.remove(i);
                break;
            }
        }
        return alredyExits;
    }
}
