package com.iamtechknow.worldview.model;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * POGO (Plain old Java object) that represents the XML structure for a layer in WMTSCapabilities.xml
 */
public class Layer {
    //First string is title/identifier, second is time, third is tile matrix set
    public static final String URLtemplate = "http://map1.vis.earthdata.nasa.gov/wmts-webmerc/%s/default/%s/%s/";

    //Fields for XML tags TODO add Dimension
    private String title;
    private String tileMatrixSet;
    private String format;

    //ISO 8601 date format
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public Layer() {}

    public Layer(String _title, String matrixSet, URL url, String _format) {
        title = _title;
        tileMatrixSet = matrixSet;
        format = _format;
    }

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

    /**
     * Formats a string with layer data and specified date to be used for a tile provider
     * @param d A date string to be formatted in ISO 8601
     * @return A URL String that may be used for UrlTileProvider.getTileURL()
     */
    public String generateURL(Date d) {
        String str = String.format(Locale.US, URLtemplate, title, dateFormat.format(d), tileMatrixSet);
        return str + "/%d/%d/%d";
    }
}
