package com.officialakbarali.fabiz.customer.data;

public class CustomerDetail {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String day;

    public CustomerDetail(String id, String name, String phone, String email, String address, String day) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.day = day;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getDay() {
        return day;
    }
}
