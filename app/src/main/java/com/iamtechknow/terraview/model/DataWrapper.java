package com.iamtechknow.terraview.model;

import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

public class DataWrapper {
    public ArrayList<Layer> layers;
    public TreeMap<String, ArrayList<String>> cats, measures;
    public Hashtable<String, Layer> layerTable;

    public DataWrapper(ArrayList<Layer> _layers, TreeMap<String, ArrayList<String>> _cats, TreeMap<String, ArrayList<String>> _measures) {
        layers = _layers;
        cats = _cats;
        measures = _measures;
        layerTable = Utils.getLayerTable(layers);
    }
}
