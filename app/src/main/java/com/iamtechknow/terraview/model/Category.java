package com.iamtechknow.terraview.model;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

/**
 * Representation of a category from the EONET API.
 * Contains a link to access events based on its category.
 */
public class Category {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
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

    /**
     * Tell the EONET client to get all events.
     * @return A category encompassing all events.
     */
    public static Category getAll() {
        return new Category(0, "All");
    }
}
