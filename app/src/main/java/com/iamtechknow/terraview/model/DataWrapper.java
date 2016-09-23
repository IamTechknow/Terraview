package com.iamtechknow.terraview.model;

import java.util.ArrayList;
import java.util.TreeMap;

public class DataWrapper {
    public ArrayList<Layer> layers;
    public TreeMap<String, ArrayList<String>> cats, measures;

    public DataWrapper(ArrayList<Layer> _layers, TreeMap<String, ArrayList<String>> _cats, TreeMap<String, ArrayList<String>> _measures) {
        layers = _layers;
        cats = _cats;
        measures = _measures;
    }
}
