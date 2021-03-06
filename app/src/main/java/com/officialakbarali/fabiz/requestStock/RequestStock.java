package com.officialakbarali.fabiz.requestStock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.Dialog;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.LogIn;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.blockPages.AppVersion;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.network.VolleyRequest;
import com.officialakbarali.fabiz.printer.BPrinter;
import com.officialakbarali.fabiz.printer.DeviceList;
import com.officialakbarali.fabiz.requestStock.adapter.RequestStockAdapter;
import com.officialakbarali.fabiz.requestStock.data.RequestItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.officialakbarali.fabiz.data.MyAppVersion.GET_MY_APP_VERSION;

public class RequestStock extends AppCompatActivity implements RequestStockAdapter.RequestStockOnClickListener {
    public static List<RequestItem> itemsForRequest;
    private Toast toast;

    private RequestStockAdapter adapter;
    RecyclerView recyclerView;

    private BluetoothSocket btsocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_stock);


        ImageButton sendToServer = findViewById(R.id.request_stock_send);
        sendToServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToServer();
            }
        });

        recyclerView = findViewById(R.id.request_stock_recycler);
        adapter = new RequestStockAdapter(this, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        ImageButton printItemBtn = findViewById(R.id.request_stock_print);
        printItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemsForRequest.size() > 0) {
                    if (btsocket == null) {
                        Intent BTIntent = new Intent(getApplicationContext(), DeviceList.class);
                        RequestStock.this.startActivityForResult(BTIntent, DeviceList.REQUEST_CONNECT_BT);
                    } else {
                        BPrinter printer = new BPrinter(btsocket, RequestStock.this);
                        printer.printRequestItems(itemsForRequest);
                    }
                } else {
                    showToast("List is empty");
                }
            }
        });

        ImageButton clearBtn = findViewById(R.id.clear_all);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FabizProvider provider = new FabizProvider(RequestStock.this, true);
                provider.delete(FabizContract.RequestItem.TABLE_NAME, null, null);
                itemsForRequest = new ArrayList<>();
                adapter.swapAdapter(itemsForRequest);
                if (itemsForRequest.size() > 0) {
                    displayEmptyView(false);
                } else {
                    displayEmptyView(true);
                }
            }
        });

        ImageButton enterItemButton = findViewById(R.id.request_stock_enter_item);
        enterItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterItemDialogue();
            }
        });

        ImageButton pickItemButton = findViewById(R.id.request_stock_pick_list);
        pickItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickItemIntent = new Intent(RequestStock.this, com.officialakbarali.fabiz.requestStock.RequestItem.class);
                startActivity(pickItemIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        addDataFromDb();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CommonResumeCheck(this);
        setUpAnimation();
    }

    @Override
    public void onClick(int indexToRemove) {
        itemsForRequest.remove(indexToRemove);
        adapter.swapAdapter(itemsForRequest);
        if (itemsForRequest.size() > 0) {
            displayEmptyView(false);
        } else {
            displayEmptyView(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FabizProvider provider = new FabizProvider(this, true);
        provider.delete(FabizContract.RequestItem.TABLE_NAME, null, null);

        for (int i = 0; i < itemsForRequest.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(FabizContract.RequestItem.COLUMN_NAME, itemsForRequest.get(i).getName());
            values.put(FabizContract.RequestItem.COLUMN_QTY, itemsForRequest.get(i).getQty());

            provider.insert(FabizContract.RequestItem.TABLE_NAME, values);
        }

        try {
            if (btsocket != null) {
                btsocket.close();
                btsocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideAll();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            btsocket = DeviceList.getSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    private void enterItemDialogue() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_enter_item_request);

        //SETTING SCREEN WIDTH
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //*************

        final EditText quantityText = dialog.findViewById(R.id.pop_up_item_request_qty);
        quantityText.setText("1");
        final EditText nameText = dialog.findViewById(R.id.pop_up_item_request_name);
        nameText.setText("");

        Button addButton = dialog.findViewById(R.id.pop_up_item_request_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameS = nameText.getText().toString().trim().toUpperCase();
                String qtyS = quantityText.getText().toString().trim();

                if (nameS.matches("") || qtyS.matches("")) {
                    showToast("Some fields are empty");
                } else {
                    itemsForRequest.add(new RequestItem(nameS, qtyS));
                    adapter.swapAdapter(itemsForRequest);
                    if (itemsForRequest.size() > 0) {
                        displayEmptyView(false);
                    } else {
                        displayEmptyView(true);
                    }
                    dialog.dismiss();
                }
            }
        });


        Button cancelDialogue = dialog.findViewById(R.id.pop_up_item_request_cancel);
        cancelDialogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void hideAll() {
        LinearLayout addCont, pickCont, printCont, clearCont, sendCont;

        recyclerView.setVisibility(View.INVISIBLE);

        clearCont = findViewById(R.id.clear_cont);
        addCont = findViewById(R.id.enter_cont);
        pickCont = findViewById(R.id.pick_cont);
        printCont = findViewById(R.id.print_cont);
        sendCont = findViewById(R.id.send_cont);


        sendCont.setVisibility(View.INVISIBLE);
        clearCont.setVisibility(View.INVISIBLE);
        addCont.setVisibility(View.INVISIBLE);
        printCont.setVisibility(View.INVISIBLE);
        pickCont.setVisibility(View.INVISIBLE);
        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideAll();
        final LinearLayout addCont, pickCont, printCont, clearCont, sendCont;
        addCont = findViewById(R.id.enter_cont);
        pickCont = findViewById(R.id.pick_cont);
        printCont = findViewById(R.id.print_cont);
        clearCont = findViewById(R.id.clear_cont);

        sendCont = findViewById(R.id.send_cont);

        YoYo.with(Techniques.SlideInLeft).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                addCont.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInLeft).duration(400).repeat(0).playOn(addCont);
                pickCont.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).duration(400).repeat(0).playOn(pickCont);
                clearCont.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInDown).duration(400).repeat(0).playOn(clearCont);
                sendCont.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInRight).duration(400).repeat(0).playOn(sendCont);

                printCont.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeInRight).withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.swapAdapter(itemsForRequest);
                        if (itemsForRequest.size() > 0) {
                            displayEmptyView(false);
                        } else {
                            displayEmptyView(true);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).duration(400).repeat(0).playOn(printCont);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).duration(400).repeat(0).playOn(recyclerView);
    }

    private void displayEmptyView(boolean setOn) {
        LinearLayout emptyView = findViewById(R.id.empty_view);
        if (setOn) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

    }

    private void addDataFromDb() {
        itemsForRequest = new ArrayList<>();
        FabizProvider provider = new FabizProvider(this, false);
        Cursor requestCursor = provider.query(FabizContract.RequestItem.TABLE_NAME, new String[]{FabizContract.RequestItem.COLUMN_NAME, FabizContract.RequestItem.COLUMN_QTY},
                null, null, null);
        while (requestCursor.moveToNext()) {
            itemsForRequest.add(new RequestItem(requestCursor.getString(requestCursor.getColumnIndexOrThrow(FabizContract.RequestItem.COLUMN_NAME)),
                    requestCursor.getString(requestCursor.getColumnIndexOrThrow(FabizContract.RequestItem.COLUMN_QTY))));
        }
    }

    private void sendToServer() {
        if (itemsForRequest.size() < 1) {
            showToast("List is empty");
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = sharedPreferences.getString("my_username", null);
        String mySignature = sharedPreferences.getString("mysign", null);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("app_version", "" + GET_MY_APP_VERSION());
        hashMap.put("my_username", "" + userName);
        hashMap.put("mysign", "" + mySignature);

        hashMap.put("row_size", "" + itemsForRequest.size());
        int i = 0;
        while (i < itemsForRequest.size()) {
            hashMap.put(FabizContract.RequestItem.COLUMN_NAME + i, itemsForRequest.get(i).getName());
            hashMap.put(FabizContract.RequestItem.COLUMN_QTY + i, itemsForRequest.get(i).getQty());
            i++;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        final VolleyRequest volleyRequest = new VolleyRequest("sendRequestItem.php", hashMap, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("Response :", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("success")) {
                        FabizProvider provider = new FabizProvider(RequestStock.this, true);
                        provider.delete(FabizContract.RequestItem.TABLE_NAME, null, null);
                        itemsForRequest = new ArrayList<>();
                        adapter.swapAdapter(itemsForRequest);
                        if (itemsForRequest.size() > 0) {
                            displayEmptyView(false);
                        } else {
                            displayEmptyView(true);
                        }

                        showToast("Successfully send to server");
                    } else {
                        showToast("Failed to send");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToast("Bad Response From Server");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
}

