package com.kewlakshat95.maraudersmap;

/**
 * Created by Akshat Maheshwari on 28-02-2016.
 */
public class User {
    private String email;
    private String phone;
    private float lat;
    private float lng;

    public User() {

    }

    public User(String email, String phone, float lat, float lng) {
        this.email = email;
        this.phone = phone;
        this.lat = lat;
        this.lng = lng;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public float getLat() {
        return lat;
    }

    public float getLng() {
        return lng;
    }
}
