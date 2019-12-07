package com.officialakbarali.fabiz.requestStock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.officialakbarali.fabiz.CommonResumeCheck;
import com.officialakbarali.fabiz.R;
import com.officialakbarali.fabiz.data.db.FabizContract;
import com.officialakbarali.fabiz.data.db.FabizProvider;
import com.officialakbarali.fabiz.requestStock.adapter.RequestStockAdapter;
import com.officialakbarali.fabiz.requestStock.data.RequestItem;

import java.util.ArrayList;
import java.util.List;

public class RequestStock extends AppCompatActivity implements RequestStockAdapter.RequestStockOnClickListener {
    public static List<RequestItem> itemsForRequest;
    private Toast toast;

    private RequestStockAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_stock);

        recyclerView = findViewById(R.id.request_stock_recycler);
        adapter = new RequestStockAdapter(this, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

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
    }

    private void showToast() {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, "Some fields are empty", Toast.LENGTH_LONG);
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
                String nameS = nameText.getText().toString().trim();
                String qtyS = quantityText.getText().toString().trim();

                if (nameS.matches("") || qtyS.matches("")) {
                    showToast();
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

    private void hideAll() {
        LinearLayout addCont, pickCont, printCont, clearCont;

        recyclerView.setVisibility(View.INVISIBLE);

        clearCont = findViewById(R.id.clear_cont);
        addCont = findViewById(R.id.enter_cont);
        pickCont = findViewById(R.id.pick_cont);
        printCont = findViewById(R.id.print_cont);

        clearCont.setVisibility(View.INVISIBLE);
        addCont.setVisibility(View.INVISIBLE);
        printCont.setVisibility(View.INVISIBLE);
        pickCont.setVisibility(View.INVISIBLE);
        displayEmptyView(false);
    }

    private void setUpAnimation() {
        hideAll();
        final LinearLayout addCont, pickCont, printCont, clearCont;
        addCont = findViewById(R.id.enter_cont);
        pickCont = findViewById(R.id.pick_cont);
        printCont = findViewById(R.id.print_cont);
        clearCont = findViewById(R.id.clear_cont);

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
}

