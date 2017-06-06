package com.iamtechknow.terraview.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Representation of an curated event from the EONET API.
 */
public class Event implements Parcelable {
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

    protected Event(Parcel in) {
        id = in.readString();
        title = in.readString();
        source = in.readString();
        categoryId = in.readInt();
        point = in.readParcelable(LatLng.class.getClassLoader());
        polygon = in.readParcelable(PolygonOptions.class.getClassLoader());
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(source);
        dest.writeInt(categoryId);
        dest.writeParcelable(point, flags);
        dest.writeParcelable(polygon, flags); //TODO: If this fails, use a boolean to decide which to write/read
    }
}
