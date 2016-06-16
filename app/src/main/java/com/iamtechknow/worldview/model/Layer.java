package com.iamtechknow.worldview.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.iamtechknow.worldview.WorldActivity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * POGO (Plain old Java object) that represents the XML structure for a layer in WMTSCapabilities.xml
 */
public class Layer implements Parcelable {
    //First string is title/identifier, second is time, third is tile matrix set
    public static final String URLtemplate = "http://gibs.earthdata.nasa.gov/wmts/epsg3857/best/%s/default/%s/%s/";

    //Fields for XML tags that are stored to database
    private String identifier, tileMatrixSet, format, title, subtitle, endDate, startDate;
    private boolean isBaseLayer;

    //ISO 8601 date format
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    //Is layer displayed? Not really needed when stack is used
    private boolean isDisplaying;

    public Layer() {}

    public Layer(String _identifier, String matrixSet, String _format, String _title, String sub, String end, String start, boolean isBase) {
        identifier = _identifier;
        tileMatrixSet = matrixSet;
        format = _format;
        title = _title;
        subtitle = sub;
        endDate = end;
        startDate = start;
        isBaseLayer = isBase;
    }

    protected Layer(Parcel in) {
        identifier = in.readString();
        tileMatrixSet = in.readString();
        format = in.readString();
        title = in.readString();
        subtitle = in.readString();
        endDate = in.readString();
        startDate = in.readString();
        isBaseLayer = in.readByte() != 0;
    }

    public static final Creator<Layer> CREATOR = new Creator<Layer>() {
        @Override
        public Layer createFromParcel(Parcel in) {
            return new Layer(in);
        }

        @Override
        public Layer[] newArray(int size) {
            return new Layer[size];
        }
    };

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTileMatrixSet() {
        return tileMatrixSet;
    }

    public void setTileMatrixSet(String tileMatrixSet) {
        this.tileMatrixSet = tileMatrixSet;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isDisplaying() {
        return isDisplaying;
    }

    public void setDisplaying(boolean displaying) {
        isDisplaying = displaying;
    }

    public boolean isBaseLayer() {
        return isBaseLayer;
    }

    public void setBaseLayer(boolean baseLayer) {
        isBaseLayer = baseLayer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String _title) {
        title = _title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String title) {
        subtitle = title;
    }

    public String getEndDate() {
        return endDate != null ? endDate : dateFormat.format(new Date(System.currentTimeMillis()));
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate != null ? startDate : "1979-01-01";
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Formats a string with layer data and specified date to be used for a tile provider
     * A date is needed even when a layer does not have a time interval, in which it is unused
     * @param d A date string to be formatted in ISO 8601
     * @return A URL String that may be used for UrlTileProvider.getTileURL()
     */
    public String generateURL(Date d) {
        //Check if date is between start and end dates
        String date;
        try {
            date = d.before(dateFormat.parse(getEndDate())) && d.after(dateFormat.parse(getStartDate())) ? dateFormat.format(d) : dateFormat.format(new Date(System.currentTimeMillis()));
        } catch(ParseException e) {
            Log.w(WorldActivity.class.getSimpleName(), "Invalid date string");
            date = dateFormat.format(new Date(System.currentTimeMillis()));
        }

        String str = String.format(Locale.US, URLtemplate, identifier, date, tileMatrixSet);
        return str + "%d/%d/%d." + format;
    }

    /**
     * Determines whether or not the layer has both a start and end date. If it does the date must be restricted
     * @return If the layer does not have an end date
     */
    public boolean isOngoing() {
        return endDate != null && startDate != null;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(tileMatrixSet);
        dest.writeString(format);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(endDate);
        dest.writeString(startDate);
        dest.writeByte((byte) (isBaseLayer ? 1 : 0));
    }
}
