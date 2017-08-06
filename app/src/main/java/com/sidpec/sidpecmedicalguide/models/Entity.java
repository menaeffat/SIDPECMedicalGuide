package com.sidpec.sidpecmedicalguide.models;

/**
 * Created by mena on 7/31/2017.
 */

public class Entity {
    public String address;
    public int catId;
    public double lat;
    public double lon;
    public String name;
    public String phone;

    public Entity() {

    }

    public Entity(String address,
                  int catId,
                  double lat,
                  double lon,
                  String name,
                  String phone) {
        this.address = address;
        this.catId = catId;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.phone = phone;
    }
}
