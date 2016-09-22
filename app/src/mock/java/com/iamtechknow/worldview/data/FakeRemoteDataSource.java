package com.iamtechknow.worldview.data;

import android.support.annotation.NonNull;

import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.TreeMap;

public class FakeRemoteDataSource implements DataSource {

    //Data
    private ArrayList<Layer> layers;
    private TreeMap<String, ArrayList<String>> categories, measurements;

    /**
     * Create some arbitrary data to be loaded.
     * @param callback The load callback from the presenter
     */
    @Override
    public void loadData(@NonNull LoadCallback callback) {
        layers = new ArrayList<>();
        categories = new TreeMap<>();
        measurements = new TreeMap<>();

        layers.add(new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color)", "Suomi NPP / VIIRS", null, "2015-11-24", null, null, true));
        layers.add(new Layer("AMSRE_Brightness_Temp_89H_Night", "GoogleMapsCompatible_Level6", "png", "Brightness Temperature (89H Ghz B Scan, Night, AMSR-E, Aqua)", "Aqua / AMSR-E", "2011-10-04", "2002-06-01", null, null, false));
        layers.add(new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "This is a Coastline Layer", "OpenStreetMaps", null, null, null, null, false));
        layers.add(new Layer("FakeLayer", "GoogleMapsCompatible_Level6", "png", "A fake layer that will not load", "Fake source", null, null, null, null, false));

        ArrayList<String> temp = new ArrayList<>();
        temp.add("Real Measurement");
        temp.add("Fake Measurement");
        temp.add("Coastlines");
        categories.put("This is a category", temp);

        temp = new ArrayList<>();
        temp.add("VIIRS_SNPP_CorrectedReflectance_TrueColor");
        temp.add("AMSRE_Brightness_Temp_89H_Night");
        measurements.put("Real Measurement", temp);

        temp = new ArrayList<>();
        temp.add("FakeLayer");
        measurements.put("Fake Measurement", temp);

        temp = new ArrayList<>();
        temp.add("Coastlines");
        measurements.put("Coastlines", temp);

        callback.onDataLoaded();
    }

    @Override
    public ArrayList<Layer> getLayers() {
        return layers;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getMeasurements() {
        return measurements;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getCategories() {
        return categories;
    }
}
