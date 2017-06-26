package com.iamtechknow.terraview.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CategoryList {
    @SerializedName("categories")
    public ArrayList<Category> list;
}
