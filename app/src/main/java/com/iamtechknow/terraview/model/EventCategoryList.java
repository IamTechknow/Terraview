package com.iamtechknow.terraview.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class EventCategoryList {
    @SerializedName("categories")
    public ArrayList<EventCategory> list;
}
