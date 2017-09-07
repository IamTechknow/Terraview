package com.iamtechknow.terraview.data;

import com.google.gson.*;
import com.iamtechknow.terraview.model.Layer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

public class WVJsonParser {
    private BufferedReader b;

    //Primary data structures, Measurements contain layer names, Categories contain measurements
    //The hash table is to map layers with their metadata URL descriptions
    private TreeMap<String, ArrayList<String>> measurement_map, category_map;
    private Hashtable<String, String> desc_map;

    public WVJsonParser(InputStream is) {
        b = new BufferedReader(new InputStreamReader(is));
        category_map = new TreeMap<>();
        measurement_map = new TreeMap<>();
        desc_map = new Hashtable<>();
    }

    /**
     * Fill in the rest of the metadata, first by compiling hashtables of measurements and categories.
     * Then get the layer order and get all the keys because each layer name is an object name in JSON.
     * We can use the key to access the metadata for that layer
     * @param list The list that currently has the layer endpoint data
     */
    public void parse(ArrayList<Layer> list, Hashtable<String, Layer> table) {
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

        fillMeasurements(measurement_map, table, measurement); //Populate measurements

        fillCategories(category_map, hazard_cat); //Populate category hashtable
        fillCategories(category_map, sci_cat);

        //Go thru the layer list and populate the rest of the metadata
        fillLayers(list, layers_json);
    }

    private void fillMeasurements(TreeMap<String, ArrayList<String>> measurements, Hashtable<String, Layer> table, JsonObject m_json) {
        //Parse measurement categories, first get the keys by getting the entry set
        ArrayList<String> measurement_keys = getKeys(m_json);

        //Create a Map that will contain a string key and List object value of all layer strings for that measurement
        for(String measure : measurement_keys) {
            JsonObject measureObj = m_json.getAsJsonObject(measure),
                    measureSources = measureObj.getAsJsonObject("sources");

            ArrayList<String> sources = getKeys(measureSources), measurement_layers = new ArrayList<>();

            for(String source_name : sources) { //iterate thru each source (GPM/GPI, MODIS, etc)
                JsonObject source = measureSources.getAsJsonObject(source_name);
                for(JsonElement e : source.getAsJsonArray("settings")) { //Now we can access the layer names to put in the list and map
                    if(table.containsKey(e.getAsString())) { //Avoid adding non-Mercator layers like orbit or SEDAC
                        measurement_layers.add(e.getAsString());

                        JsonElement desc = source.get("description");
                        if(desc != null && !desc.getAsString().isEmpty()) //may be blank or null
                            desc_map.put(e.getAsString(), desc.getAsString());
                    }
                }
            }
            Collections.sort(measurement_layers); //Display measurements in order
            if(!measurement_layers.isEmpty())
                measurements.put(measure, measurement_layers);
        }
    }

    private void fillCategories(TreeMap<String, ArrayList<String>> categories, JsonObject cat_json) {
        //Get keys, fill category measurements list
        for(String cat : getKeys(cat_json)) {
            ArrayList<String> cat_measurements = new ArrayList<>();
            JsonObject categoryObj = cat_json.getAsJsonObject(cat);
            JsonArray catArray = categoryObj.getAsJsonArray("measurements");

            for(JsonElement e : catArray)
                if(measurement_map.containsKey(e.getAsString())) //Only add parsed measurements
                    cat_measurements.add(e.getAsString());

            Collections.sort(cat_measurements);
            if(!cat_measurements.isEmpty())
                categories.put(cat, cat_measurements);
        }
    }

    private void fillLayers(ArrayList<Layer> list, JsonObject layer_json) {
        //By now we have the identifiers for all the Layers we could use to display
        //Layers not found in both XML and JSON metadata are deleted for now
        ArrayList<Layer> toDelete = new ArrayList<>();

        for(Layer layer : list) {
            JsonElement element = layer_json.get(layer.getIdentifier());

            if(element != null) {
                JsonObject jsonLayer = element.getAsJsonObject();
                JsonPrimitive subPrimitive, startPrimitive, endPrimitive;
                String subtitle = null, endDate = null, startDate = null;

                boolean isBaseLayer = jsonLayer.getAsJsonPrimitive("group").getAsString().equals("baselayers");

                //These may not exist in the metadata
                subPrimitive = jsonLayer.getAsJsonPrimitive("subtitle");
                startPrimitive = jsonLayer.getAsJsonPrimitive("startDate");
                endPrimitive = jsonLayer.getAsJsonPrimitive("endDate");

                if(subPrimitive != null)
                    subtitle = subPrimitive.getAsString();
                if(startPrimitive != null)
                    startDate = startPrimitive.getAsString();
                if(endPrimitive != null)
                    endDate = endPrimitive.getAsString();

                layer.setBaseLayer(isBaseLayer);
                layer.setSubtitle(subtitle);
                layer.setStartDate(startDate);
                layer.setEndDate(endDate);

                if(desc_map.containsKey(layer.getIdentifier()))
                    layer.setDescription(desc_map.get(layer.getIdentifier()));
            } else
                toDelete.add(layer);
        }
        list.removeAll(toDelete);
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
