package com.sidpec.sidpecmedicalguide.models;

import android.location.Location;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mena on 7/20/2017.
 */

@IgnoreExtraProperties
public class MedicalEntity {
    public String entityID;
    public String name;
    public String address;
    public String phone;
    public double lat;
    public double lon;
    public int cat_id;
    public String metadata;
    public String details;
    public int favCount = 0;
    public Map<String, Boolean> favs = new HashMap<>();

    public MedicalEntity() {

    }

    public MedicalEntity(String entityID, String name, String address, String phone, double lat, double lon, int cat_id, String metadata, String details) {
        this.entityID = entityID;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.lat = lat;
        this.lon = lon;
        this.cat_id = cat_id;
        this.metadata = metadata;
        this.details = details;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("entityID", entityID);
        result.put("name", name);
        result.put("address", address);
        result.put("phone", phone);
        result.put("lat", lat);
        result.put("lon", lon);
        result.put("cat_id", cat_id);
        result.put("metadata", metadata);
        result.put("details", details);
        result.put("favCount", favCount);
        result.put("favs", favs);

        return result;
    }

    @Exclude
    public int getDistance(double myLat, double myLon) {
        Location targetLocation = new Location("db");
        targetLocation.setLatitude(this.lat);
        targetLocation.setLongitude(this.lon);
        Location myLocation = new Location("gps");
        myLocation.setLatitude(myLat);
        myLocation.setLongitude(myLon);
        return (int) targetLocation.distanceTo(myLocation);
    }
}
