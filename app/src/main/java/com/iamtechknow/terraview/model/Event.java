package com.iamtechknow.terraview.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Representation of an curated event from the EONET API.
 */
public class Event {
    private String id, title, source;
    private int categoryId;

    //An event has either a point or polygon - only the first point is used
    private LatLng point;
    private PolygonOptions polygon;

    public Event(String id, String title, String source, int category, LatLng point) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.categoryId = category;
        this.point = point;
    }

    public Event(String id, String title, String source, int category, PolygonOptions polygon) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.categoryId = category;
        this.polygon = polygon;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public int getCategory() {
        return categoryId;
    }

    public boolean hasPoint() {
        return point != null;
    }

    public LatLng getPoint() {
        return point;
    }

    public PolygonOptions getPolygon() {
        return polygon;
    }
}
