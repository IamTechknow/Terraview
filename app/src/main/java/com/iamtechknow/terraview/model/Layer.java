package com.iamtechknow.terraview.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * POGO (Plain old Java object) that represents the XML structure for a layer in WMTSCapabilities.xml
 * Also defines a one-to-many relationship of measurements with layers.
 */
@Entity
public class Layer implements Parcelable, Comparable<Layer> {
    public static final int VISIBLE = 0, TRANSPARENT = 1, INVISIBLE = 2;

    //Fields for XML/JSON tags that are stored to database
    @PrimaryKey
    @NonNull
    private String identifier;

    private String tileMatrixSet;
    private String format;
    private String title;
    private String subtitle;
    private String endDate;
    private String startDate;
    private String description;
    private String palette;
    private boolean isBaseLayer;

    //Unless set all layers are visible by default
    @Ignore
    private int visibility;

    //ISO 8601 date format
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Ignore
    public Layer() {}

    public Layer(@NonNull String identifier, String tileMatrixSet, String format, String title, String subtitle, String endDate, String startDate, String description, String palette, boolean isBaseLayer) {
        this.identifier = identifier;
        this.tileMatrixSet = tileMatrixSet;
        this.format = format;
        this.title = title;
        this.subtitle = subtitle;
        this.endDate = endDate;
        this.startDate = startDate;
        this.description = description;
        this.palette = palette;
        this.isBaseLayer = isBaseLayer;
        visibility = VISIBLE;
    }

    @Ignore
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
        visibility = in.readInt();
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

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(@NonNull String identifier) {
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

    public int getVisibility() {
        return visibility;
    }

    public void setVisible(int visibility) {
        this.visibility = visibility;
    }

    @Override
    public int describeContents() {
        return 0;
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
        dest.writeInt(visibility);
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
