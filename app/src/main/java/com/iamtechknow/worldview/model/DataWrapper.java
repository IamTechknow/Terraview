package com.iamtechknow.worldview.model;

import java.util.ArrayList;
import java.util.Hashtable;

public class DataWrapper {
    public ArrayList<Layer> layers;
    public Hashtable<String, ArrayList<String>> cats, measures;

    public DataWrapper(ArrayList<Layer> _layers, Hashtable<String, ArrayList<String>> _cats, Hashtable<String, ArrayList<String>> _measures) {
        layers = _layers;
        cats = _cats;
        measures = _measures;
    }
}
