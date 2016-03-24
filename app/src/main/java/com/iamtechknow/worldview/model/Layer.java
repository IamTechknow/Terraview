package com.iamtechknow.worldview.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * POGO (Plain old Java object) that represents the XML structure for a layer in WMTSCapabilities.xml
 */
public class Layer implements Parcelable {
    //First string is title/identifier, second is time, third is tile matrix set
    public static final String URLtemplate = "http://map1.vis.earthdata.nasa.gov/wmts-webmerc/%s/default/%s/%s/";

    //Fields for XML tags that are stored to database TODO add Dimension
    private String title;
    private String tileMatrixSet;
    private String format;

    //ISO 8601 date format
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    //Is layer displayed:
    private boolean isDisplaying;

    public Layer() {}

    public Layer(String _title, String matrixSet, String _format) {
        title = _title;
        tileMatrixSet = matrixSet;
        format = _format;
    }

    protected Layer(Parcel in) {
        title = in.readString();
        tileMatrixSet = in.readString();
        format = in.readString();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    /**
     * Formats a string with layer data and specified date to be used for a tile provider
     * @param d A date string to be formatted in ISO 8601
     * @return A URL String that may be used for UrlTileProvider.getTileURL()
     */
    public String generateURL(Date d) {
        String str = String.format(Locale.US, URLtemplate, title, dateFormat.format(d), tileMatrixSet);
        return str + "%d/%d/%d." + format;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(tileMatrixSet);
        dest.writeString(format);
    }
}
