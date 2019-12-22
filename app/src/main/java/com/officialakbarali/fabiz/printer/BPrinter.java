package com.officialakbarali.fabiz.printer;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.officialakbarali.fabiz.customer.sale.data.Cart;
import com.officialakbarali.fabiz.requestStock.data.RequestItem;

import static com.officialakbarali.fabiz.data.CommonInformation.TruncateDecimal;
import static com.officialakbarali.fabiz.data.CommonInformation.getCurrency;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class BPrinter {
    private BluetoothSocket btsocket;
    private OutputStream outputStream;
    private Context context;
    private String StaffId;
    private String StaffName;

    public BPrinter(BluetoothSocket btsocket, Context context) {
        this.btsocket = btsocket;
        this.context = context;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        StaffId = sharedPreferences.getInt("idOfStaff", 0) + "";
        StaffName = sharedPreferences.getString("nameOfStaff", "User");
    }

    public void printInvoice(String billId,String date, List<Cart> cartItems, String totalAmount, String paidAmount,String totalDue, String customerName, String customerAddress, String vat) {
        try {
            outputStream = btsocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outputStream = btsocket.getOutputStream();
            byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
            outputStream.write(printformat);

            printCustom("My Good Shop", 3, 1);
            printCustom("VAT NO : 12345678", 1, 1);
            printCustom("8 MILE NY USA", 0, 1);
            printNewLine();
            printCustom("Sales", 2, 1);
            printNewLine();
            printCustom("BillId :" + billId, 0, 0);
            printCustom("Date   :" + date, 0, 0);
            printCustom("Staff  :" + StaffName, 0, 0);
            printCustom("................................", 0, 0);
            printCustom("................................", 0, 0);

            for (int i = 0; i < cartItems.size(); i++) {
                String itemInfo = cartItems.get(i).getItemId() + " / " + cartItems.get(i).getName()
                        + " / " + cartItems.get(i).getBrand() + " / " + cartItems.get(i).getCategory();
                printCustom(itemInfo, 0, 1);
                String itemQtyRateInfo = cartItems.get(i).getQty() + " * " + TruncateDecimal(cartItems.get(i).getPrice() + "") + " " + getCurrency();
                printCustom(itemQtyRateInfo, 0, 1);
                printCustom(TruncateDecimal(cartItems.get(i).getTotal() + "") + " " + getCurrency(), 0, 1);
                printCustom("................................", 0, 0);
            }
            printCustom("................................", 0, 0);
            printCustom("Total Amount :" + TruncateDecimal(totalAmount) + " " + getCurrency(), 1, 2);
            printCustom("Received Amount :" + TruncateDecimal(paidAmount) + " " + getCurrency(), 1, 2);
            printCustom("Total Due Amount :" + TruncateDecimal(totalDue) + " " + getCurrency(), 1, 2);
            printCustom("................................", 0, 0);
            printNewLine();
            printCustom(customerName, 2, 1);
            printCustom("Vat :" + vat, 1, 1);
            printCustom(customerAddress + vat, 0, 1);
            printNewLine();
            printCustom(">>>  Thank you  <<<", 1, 1);

            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printRequestItems(List<RequestItem> itemsForRequest) {
        try {
            outputStream = btsocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outputStream = btsocket.getOutputStream();
            byte[] printformat = new byte[]{0x1B, 0x21, 0x03};
            outputStream.write(printformat);
            printCustom("Item Request", 2, 1);
            printNewLine();
            printCustom("Staff    :" + StaffName, 0, 0);
            printNewLine();
            printCustom("Staff Id :" + StaffId, 0, 0);
            printCustom("................................", 0, 0);
            for (int i = 0; i < itemsForRequest.size(); i++) {
                printCustom(itemsForRequest.get(i).getName(), 0, 0);
                printCustom("Qty :" + itemsForRequest.get(i).getQty(), 0, 0);
                printCustom("................................", 0, 0);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printNewLine() {
        try {
            outputStream.write(new byte[]{10});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printCustom(String msg, int size, int align) {
        byte[] cc = new byte[]{0x1B, 0x21, 0x03};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align) {
                case 0:
                    //left align
                    outputStream.write(new byte[]{0x1b, 'a', 0x00});
                    break;
                case 1:
                    //center align
                    outputStream.write(new byte[]{0x1b, 'a', 0x01});
                    break;
                case 2:
                    //right align
                    outputStream.write(new byte[]{0x1b, 'a', 0x02});
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(0x0A);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
