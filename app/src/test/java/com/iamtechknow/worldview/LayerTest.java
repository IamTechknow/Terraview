package com.iamtechknow.worldview;

import com.iamtechknow.worldview.model.Layer;

import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

public class LayerTest {
    private Date past, now, future;

    //ISO 8601 date format
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Before
    public void setupDates() {
        now = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        now = c.getTime();

        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
        future = c.getTime();

        c.set(Calendar.DAY_OF_MONTH, 15);
        c.set(Calendar.MONTH, Calendar.JUNE);
        c.set(Calendar.YEAR, 2010);
        past = c.getTime();
    }

    @Test
    public void testURLofCurrentLayer() {
        Layer viirs = new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color)", "Suomi NPP / VIIRS", null, "2015-11-24", true);
        String expected = String.format("http://gibs.earthdata.nasa.gov/wmts/epsg3857/best/VIIRS_SNPP_CorrectedReflectance_TrueColor/default/%s/GoogleMapsCompatible_Level9/", dateFormat.format(now)) + "%d/%d/%d.jpg";

        assertTrue(viirs.generateURL(now).equals(expected));
        assertTrue(viirs.generateURL(future).equals(expected)); //future dates selected default to today
    }

    @Test
    public void testURLofPastLayer() {
        Layer amsr = new Layer("AMSRE_Brightness_Temp_89H_Night", "GoogleMapsCompatible_Level6", "png", "Brightness Temperature (89H Ghz B Scan, Night, AMSR-E, Aqua)", "Aqua / AMSR-E", "2011-10-04", "2002-06-01", false);
        String result = amsr.generateURL(past);

        assertTrue(result.equals("http://gibs.earthdata.nasa.gov/wmts/epsg3857/best/AMSRE_Brightness_Temp_89H_Night/default/2010-06-15/GoogleMapsCompatible_Level6/%d/%d/%d.png"));
    }
}