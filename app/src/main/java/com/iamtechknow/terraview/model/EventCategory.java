package com.iamtechknow.terraview.model;

import com.google.gson.annotations.SerializedName;

/**
 * Representation of a category from the EONET API.
 * Contains a link to access events based on its category.
 */
public class EventCategory {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    public EventCategory(int id, String title) {
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
    public static EventCategory getAll() {
        return new EventCategory(0, "All");
    }
}
