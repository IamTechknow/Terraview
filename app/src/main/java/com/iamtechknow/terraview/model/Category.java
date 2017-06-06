package com.iamtechknow.terraview.model;

import java.util.Locale;

/**
 * Representation of a category from the EONET API.
 * Contains a link to access events based on its category.
 */
public class Category {
    private static final String CATEGORY_FMT = "https://eonet.sci.gsfc.nasa.gov/api/v2.1/categories/%d";

    private int id;
    private String title;

    public Category(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return String.format(Locale.US, CATEGORY_FMT, id);
    }
}
