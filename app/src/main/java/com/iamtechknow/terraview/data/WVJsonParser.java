package com.iamtechknow.terraview.data;

import com.google.gson.*;
import com.iamtechknow.terraview.model.CatMeasureJoin;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.MeasureLayerJoin;
import com.iamtechknow.terraview.model.Measurement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class WVJsonParser {
    private BufferedReader b;

    //Primary data structures, Measurements contain layer names, Categories contain measurements
    //The hash table is to map layers with their metadata URL descriptions
    private ArrayList<Category> categories;
    private HashSet<String> measurements;
    private HashMap<String, String> desc_map;

    //Join Lists
    private ArrayList<CatMeasureJoin> catJoins;
    private ArrayList<MeasureLayerJoin> measureJoins;

    public WVJsonParser(InputStream is) {
        b = new BufferedReader(new InputStreamReader(is));
        categories = new ArrayList<>();
        measurements = new HashSet<>();
        catJoins = new ArrayList<>();
        measureJoins = new ArrayList<>();
        desc_map = new HashMap<>();
    }

    /**
     * Populate model objects and joins for Measurements/Layers and Categories/Measurements.
     * @param list Current layer information from parsed XML
     */
    public void parse(List<Layer> list, HashMap<String, Layer> table) {
        JsonObject root, layers_json, cat;

        root = new JsonParser().parse(b).getAsJsonObject();
        layers_json = root.get("layers").getAsJsonObject();
        cat = root.get("categories").getAsJsonObject();
        try {
            b.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        fillMeasurements(measurements, measureJoins, table, root.get("measurements").getAsJsonObject());
        fillCategories(categories, catJoins, cat.get("hazards and disasters").getAsJsonObject());
        fillCategories(categories, catJoins, cat.get("scientific").getAsJsonObject());
        fillLayers(list, layers_json);
    }

    //For each measurement, iterate through all "sources" or layers
    private void fillMeasurements(HashSet<String> measurements, ArrayList<MeasureLayerJoin> joins, HashMap<String, Layer> table, JsonObject m_json) {
        for(String measure : getKeys(m_json)) {
            JsonObject measureObj = m_json.getAsJsonObject(measure), measureSources = measureObj.getAsJsonObject("sources");
            ArrayList<String> sources = getKeys(measureSources), measurement_layers = new ArrayList<>();

            for(String source_name : sources) { //iterate thru each source (GPM/GPI, MODIS, etc)
                JsonObject source = measureSources.getAsJsonObject(source_name);
                for(JsonElement layer : source.getAsJsonArray("settings")) //Now we can access the layer names to put in the list and map
                    if(table.containsKey(layer.getAsString())) { //Avoid adding non-Mercator layers like orbit or SEDAC
                        measurement_layers.add(layer.getAsString());

                        JsonElement desc = source.get("description");
                        if(desc != null && !desc.getAsString().isEmpty()) //may be blank or null
                            desc_map.put(layer.getAsString(), desc.getAsString());
                    }
            }

            //Add measurement to list and all joins
            if(!measurement_layers.isEmpty())
                measurements.add(measure);
            for(String layer : measurement_layers)
                joins.add(new MeasureLayerJoin(measure, layer));
        }
    }

    //Get keys, fill category list and all possible category/measurement pairs
    private void fillCategories(ArrayList<Category> categories, ArrayList<CatMeasureJoin> joins, JsonObject cat_json) {
        for(String cat : getKeys(cat_json)) {
            categories.add(new Category(cat));

            //Get the JSON array and add pairs. Filter out measurements that aren't already found like Fires
            JsonObject categoryObj = cat_json.getAsJsonObject(cat);
            JsonArray measureArray = categoryObj.getAsJsonArray("measurements");
            for(JsonElement e : measureArray)
                if(measurements.contains(e.getAsString()))
                    joins.add(new CatMeasureJoin(cat, e.getAsString()));
        }
    }

    private void fillLayers(List<Layer> list, JsonObject layer_json) {
        //By now we have the identifiers for all the Layers we could use to display
        //Layers not found in both XML and JSON metadata are deleted for now
        ArrayList<Layer> toDelete = new ArrayList<>();

        for(Layer layer : list) {
            JsonElement element = layer_json.get(layer.getIdentifier());

            if(element != null) {
                JsonObject jsonLayer = element.getAsJsonObject();
                JsonPrimitive subPrimitive, startPrimitive, endPrimitive;

                boolean isBaseLayer = jsonLayer.getAsJsonPrimitive("group").getAsString().equals("baselayers");
                layer.setBaseLayer(isBaseLayer);

                //These may not exist in the metadata
                subPrimitive = jsonLayer.getAsJsonPrimitive("subtitle");
                startPrimitive = jsonLayer.getAsJsonPrimitive("startDate");
                endPrimitive = jsonLayer.getAsJsonPrimitive("endDate");

                if(subPrimitive != null)
                    layer.setSubtitle(subPrimitive.getAsString());
                if(startPrimitive != null)
                    layer.setStartDate(startPrimitive.getAsString());
                if(endPrimitive != null)
                    layer.setEndDate(endPrimitive.getAsString());
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
    private ArrayList<String> getKeys(JsonObject json) {
        ArrayList<String> cat_keys = new ArrayList<>();
        for(Map.Entry<String, JsonElement> e : json.entrySet())
            cat_keys.add(e.getKey());
        return cat_keys;
    }

    public ArrayList<Measurement> getMeasurements() {
        ArrayList<Measurement> temp = new ArrayList<>();
        for(String str : measurements)
            temp.add(new Measurement(str));
        return temp;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public ArrayList<CatMeasureJoin> getCatJoins() {
        return catJoins;
    }

    public ArrayList<MeasureLayerJoin> getMeasureJoins() {
        return measureJoins;
    }
}
