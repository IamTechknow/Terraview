package com.iamtechknow.terraview.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Representation of an curated event from the EONET API.
 */
public class Event implements Parcelable {
    //Unique identifier for the event
    private String id;

    //Event title
    private String title;

    //Source for the event
    private String source;

    //Date in which the event has ended, null if ongoing
    private String closed;

    //Category of the event
    private int categoryId;

    //Date for the first geometry object
    private String date;

    //An event has either a point or polygon - only the first geometry object is used
    private LatLng point;
    private PolygonOptions polygon;

    public Event(String id, String title, String source, String date, int category, LatLng point) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.date = parseDate(date);
        this.categoryId = category;
        this.point = point;
    }

    public Event(String id, String title, String source, String date, int category, PolygonOptions polygon) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.date = parseDate(date);
        this.categoryId = category;
        this.polygon = polygon;

        //Change default polygon options
        this.polygon.strokeWidth(5.0f).strokeColor(0xff0000ff);
    }

    protected Event(Parcel in) {
        id = in.readString();
        title = in.readString();
        source = in.readString();
        date = in.readString();
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

    public boolean isOngoing() {
        return closed == null;
    }

    public String getDate() {
        return date;
    }

    public String getClosedDate() {
        return closed;
    }

    public void setClosedDate(String closed) {
        this.closed = closed;
    }

    @Override
    public int describeContents() {
        return id.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(source);
        dest.writeString(date);
        dest.writeInt(categoryId);
        dest.writeParcelable(point, flags);
        dest.writeParcelable(polygon, flags);
    }

    /**
     * Strip the extra time on the date string to make it parsable
     * @param isoDate date from JSON response
     */
    private String parseDate(String isoDate) {
        return isoDate.substring(0, isoDate.indexOf('T'));
    }
}
