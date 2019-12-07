package com.officialakbarali.fabiz.item.data;

public class UnitData {
    private String id;
    private String unitName;
    private int qty;

    public UnitData(String id, String unitName, int qty) {
        this.id = id;
        this.unitName = unitName;
        this.qty = qty;
    }

    public String getId() {
        return id;
    }

    public String getUnitName() {
        return unitName;
    }

    public int getQty() {
        return qty;
    }
}
