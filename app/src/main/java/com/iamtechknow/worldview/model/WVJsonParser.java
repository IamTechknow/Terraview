package com.iamtechknow.worldview.model;

import android.util.Log;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class WVJsonParser {
    private BufferedReader b;

    //Primary data structures, Measurements contain layer names, Categories contain measurements
    private TreeMap<String, ArrayList<String>> measurement_map, category_map;

    public WVJsonParser(InputStream is) {
        b = new BufferedReader(new InputStreamReader(is));
        category_map = new TreeMap<>();
        measurement_map = new TreeMap<>();
    }

    /**
     * Fill in the rest of the metadata, first by compiling hashtables of measurements and categories.
     * Then get the layer order and get all the keys because each layer name is an object name in JSON.
     * We can use the key to access the metadata for that layer
     * @param list The list that currently has the layer endpoint data
     */
    public void parse(ArrayList<Layer> list) {
        JsonObject root, layers_json, measurement, cat, hazard_cat, sci_cat;

        root = new JsonParser().parse(b).getAsJsonObject();
        layers_json = root.get("layers").getAsJsonObject();
        measurement = root.get("measurements").getAsJsonObject();
        cat = root.get("categories").getAsJsonObject();
        hazard_cat = cat.get("hazards and disasters").getAsJsonObject();
        sci_cat = cat.get("scientific").getAsJsonObject();
        try {
            b.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        fillMeasurements(measurement_map, measurement); //Populate measurements

        fillCategories(category_map, hazard_cat); //Populate category hashtable
        fillCategories(category_map, sci_cat);

        //Go thru the layer list and populate the rest of the metadata
        fillLayers(list, layers_json);
    }

    public void fillMeasurements(TreeMap<String, ArrayList<String>> measurements, JsonObject m_json) {
        //Parse measurement categories, first get the keys by getting the entry set
        ArrayList<String> measurement_keys = getKeys(m_json);

        //Create a Map that will contain a string key and List object value of all layer strings for that measurement
        for(String measure : measurement_keys) {
            JsonObject measureObj = m_json.getAsJsonObject(measure),
                    measureSources = measureObj.getAsJsonObject("sources");

            //Get each key for the sources
            ArrayList<String> sources = getKeys(measureSources), measurement_layers = new ArrayList<>();

            for(String source : sources) { //iterate thru each source (GPM/GPI, MODIS, etc)
                JsonArray settings = measureSources.getAsJsonObject(source).getAsJsonArray("settings");
                for(JsonElement e : settings) //Now we can access the layer names to put in the list and map
                    measurement_layers.add(e.getAsString());
            }
            Collections.sort(measurement_layers); //To display measurements in order
            measurements.put(measure, measurement_layers); //List is complete here
        }
    }

    public void fillCategories(TreeMap<String, ArrayList<String>> categories, JsonObject cat_json) {
        //Get keys, fill category measurements list
        for(String s : getKeys(cat_json)) {
            ArrayList<String> cat_measurements = new ArrayList<>();
            JsonObject categoryObj = cat_json.getAsJsonObject(s);
            JsonArray catArray = categoryObj.getAsJsonArray("measurements");

            for(JsonElement e : catArray)
                cat_measurements.add(e.getAsString());

            Collections.sort(cat_measurements);
            categories.put(s, cat_measurements);
        }
    }

    public void fillLayers(ArrayList<Layer> list, JsonObject layer_json) {
        //By now we have the identifiers for all the Layers we could use to display on Google Maps
        //Not all layers supported on Worldview have support for GMaps. Now we go through each identifier

        for(Layer layer : list) {
            try {
                JsonObject jsonLayer = layer_json.get(layer.getIdentifier()).getAsJsonObject();
                String subtitle = null, endDate = null, startDate = null;
                boolean isBaseLayer;

                isBaseLayer = jsonLayer.getAsJsonPrimitive("group").getAsString().equals("baselayers");

                try { //These elements don't always exist in the layer object
                    subtitle = jsonLayer.getAsJsonPrimitive("subtitle").getAsString();
                    startDate = jsonLayer.getAsJsonPrimitive("startDate").getAsString();
                    endDate = jsonLayer.getAsJsonPrimitive("endDate").getAsString();
                } catch (Exception e) { //Don't print stack trace

                }

                //Finishing getting data, add it in now
                layer.setBaseLayer(isBaseLayer);
                layer.setSubtitle(subtitle);
                layer.setStartDate(startDate);
                layer.setEndDate(endDate);
            } catch(NullPointerException e) { //Gracefully deal with bad input
                Log.w(getClass().getSimpleName(), "Unable to access layer metadata, skipping: " + layer.getIdentifier());
                layer.setTitle(layer.getIdentifier());
                layer.setBaseLayer(false);
            }
        }
        Collections.sort(list);
    }

    /**
     * Get the keys of the objects stored inside the parent JsonObject by accessing its entry set.
     * @param json The object that encapsulates many other objects of relevant data
     * @return List of all the object names, which allow us to access each JsonElement
     */
    private static ArrayList<String> getKeys(JsonObject json) {
        ArrayList<String> cat_keys = new ArrayList<>();
        for(Map.Entry<String, JsonElement> e : json.entrySet())
            cat_keys.add(e.getKey());

        return cat_keys;
    }

    public TreeMap<String, ArrayList<String>> getMeasurementMap() {
        return measurement_map;
    }

    public TreeMap<String, ArrayList<String>> getCategoryMap() {
        return category_map;
    }
}
