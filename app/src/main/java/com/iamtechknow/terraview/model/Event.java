package com.iamtechknow.terraview.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    //Date for the geometry objects
    private List<String> dates;

    //An event has either points or polygon - only the first geometry object is used
    private List<LatLng> points;
    private PolygonOptions polygon;

    public Event(String id, String title, String source, List<String> rawDates, int category, List<LatLng> points) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.dates = parseDates(rawDates);
        this.categoryId = category;
        this.points = points;
    }

    public Event(String id, String title, String source, String rawDate, int category, PolygonOptions polygon) {
        this.id = id;
        this.title = title;
        this.source = source;
        this.dates = Collections.singletonList(parseDate(rawDate));
        this.categoryId = category;
        this.polygon = polygon;

        //Change default polygon options
        this.polygon.strokeWidth(5.0f).strokeColor(0xff0000ff);
    }

    protected Event(Parcel in) {
        dates = new ArrayList<>();
        id = in.readString();
        title = in.readString();
        source = in.readString();
        in.readList(dates, String.class.getClassLoader());
        categoryId = in.readInt();
        polygon = in.readParcelable(PolygonOptions.class.getClassLoader());

        //Check if points were saved
        if(in.readInt() == 1) {
            points = new ArrayList<>();
            in.readTypedList(points, LatLng.CREATOR);
        }
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
        return points != null;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public PolygonOptions getPolygon() {
        return polygon;
    }

    public boolean isOngoing() {
        return closed == null;
    }

    public List<String> getDates() {
        return dates;
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
        dest.writeList(dates);
        dest.writeInt(categoryId);
        dest.writeParcelable(polygon, flags);

        //Write a value to indicate if there are points (and not a polygon), then write the points
        dest.writeInt(points != null ? 1 : 0);
        if(points != null)
            dest.writeTypedList(points);
    }

    /**
     * Strip the extra time on the date string to make it parsable
     * @param isoDate date from JSON response
     */
    private String parseDate(String isoDate) {
        return isoDate.substring(0, isoDate.indexOf('T'));
    }

    private List<String> parseDates(List<String> rawDates) {
        for (int i = 0; i < rawDates.size(); i++)
            rawDates.set(i, parseDate(rawDates.get(i)));
        return rawDates;
    }
}
