package com.iamtechknow.terraview.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * POGO (Plain old Java object) that represents the XML structure for a layer in WMTSCapabilities.xml
 */
public class Layer implements Parcelable, Comparable<Layer> {
    //Fields for XML tags that are stored to database
    private String identifier, tileMatrixSet, format, title, subtitle, endDate, startDate, description, palette;
    private boolean isBaseLayer;

    //ISO 8601 date format
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public Layer() {}

    public Layer(String _identifier, String matrixSet, String _format, String _title, String sub, String end, String start, String _description, String _palette, boolean isBase) {
        identifier = _identifier;
        tileMatrixSet = matrixSet;
        format = _format;
        title = _title;
        subtitle = sub;
        endDate = end;
        startDate = start;
        description = _description;
        palette = _palette;
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
        description = in.readString();
        palette = in.readString();
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

    public String getEndDateRaw() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate != null ? startDate : "1948-01-01";
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartDateRaw() {
        return startDate;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    public String getDescription() {
        return description;
    }

    public void setPalette(String _palette) {
        palette = _palette;
    }

    public String getPalette() {
        return palette;
    }

    /**
     * Determines if the layer is an overlay that contains a colormap
     */
    public boolean hasColorMap() {
        return palette != null;
    }

    /**
     * Does the layer have dates? (No if Reference Labels, Coastlines, Blue Marble, Earth at Night 2012)
     */
    public boolean hasNoDates() {
        return endDate == null && startDate == null;
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
        dest.writeString(description);
        dest.writeString(palette);
        dest.writeByte((byte) (isBaseLayer ? 1 : 0));
    }

    //Layers are compared the same way as strings, the relative order depends on that of their titles
    @Override
    public int compareTo(@NonNull Layer another) {
        return getTitle().compareTo(another.getTitle());
    }

    @Override
    public boolean equals(@NonNull Object other) {
        return other instanceof Layer && identifier.equals(((Layer) other).getIdentifier());
    }

    @Override
    public int hashCode() {
        return identifier.hashCode(); //use identifier string hash
    }
}
