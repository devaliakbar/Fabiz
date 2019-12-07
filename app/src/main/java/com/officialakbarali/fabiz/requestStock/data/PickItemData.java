package com.officialakbarali.fabiz.requestStock.data;

public class PickItemData {
    private String id;
    private String name;
    private String brand;
    private String category;
    private double price;
    private int qty;


    public PickItemData(String id, String name, String brand, String catagory, double price) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.category = catagory;
        this.price = price;
        this.qty = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}
