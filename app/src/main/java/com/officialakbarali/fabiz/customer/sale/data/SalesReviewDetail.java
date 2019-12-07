package com.officialakbarali.fabiz.customer.sale.data;

public class SalesReviewDetail {
    private String id;
    private String date;
    private int qty;
    private double total;
    private double paid;
    private double due;
    private double returnedAmount;
    private double currentTotal;
    private double discount;

    public SalesReviewDetail(String id, String date, int qty, double total, double paid, double due, double returnedAmount, double currentTotal, double discount) {
        this.id = id;
        this.date = date;
        this.qty = qty;
        this.total = total;
        this.paid = paid;
        this.due = due;
        this.returnedAmount = returnedAmount;
        this.currentTotal = currentTotal;
        this.discount = discount;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public int getQty() {
        return qty;
    }

    public double getTotal() {
        return total;
    }

    public double getPaid() {
        return paid;
    }

    public double getDue() {
        return due;
    }

    public double getReturnedAmount() {
        return returnedAmount;
    }

    public double getCurrentTotal() {
        return currentTotal;
    }

    public double getDiscount() {
        return discount;
    }
}

