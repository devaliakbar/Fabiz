package com.officialakbarali.fabiz.customer.sale.data;

public class SalesReturnReviewItem {
    private String id;
    private String billId;
    private String date;
    private String itemId;
    private String name;
    private String brand;
    private String catagory;
    private double price;
    private int qty;
    private double total;
    private String unitName;

    public SalesReturnReviewItem(String id, String billId, String date, String itemId, String name, String brand, String catagory, double price, int qty, double total, String unitName) {
        this.id = id;
        this.billId = billId;
        this.date = date;
        this.itemId = itemId;
        this.name = name;
        this.brand = brand;
        this.catagory = catagory;
        this.price = price;
        this.qty = qty;
        this.total = total;
        this.unitName = unitName;
    }

    public String getId() {
        return id;
    }

    public String getBillId() {
        return billId;
    }

    public String getDate() {
        return date;
    }

    public String getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCatagory() {
        return catagory;
    }

    public double getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public double getTotal() {
        return total;
    }

    public String getUnitName() {
        return unitName;
    }
}

