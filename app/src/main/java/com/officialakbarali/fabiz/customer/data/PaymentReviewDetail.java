package com.officialakbarali.fabiz.customer.data;

public class PaymentReviewDetail {
    private String id;
    private String date;
    private double amount;
    private String billId;


    public PaymentReviewDetail(String id, String date, double amount, String billId) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.billId = billId;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getBillId() {
        return billId;
    }
}
