package com.sidpec.sidpecmedicalguide.models;

/**
 * Created by mena on 7/30/2017.
 */

public class Category {

    public int catId;
    public String display;
    public int order;

    public Category() {

    }

    public Category(int catId, String display, int order) {
        this.catId = catId;
        this.display = display;
        this.order = order;
    }
}
