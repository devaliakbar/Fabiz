package com.officialakbarali.fabiz.requestStock.data;

public class RequestItem {
    private String name;
    private String qty;

    public RequestItem(String name, String qty) {
        this.name = name;
        this.qty = qty;
    }

    public String getName() {
        return name;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }
}
