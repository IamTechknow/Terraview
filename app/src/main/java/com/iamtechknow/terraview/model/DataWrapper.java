package com.iamtechknow.terraview.model;

import com.iamtechknow.terraview.util.Utils;

import java.util.List;
import java.util.HashMap;

public class DataWrapper {
    public final List<Layer> layers;
    public final List<Category> cats;
    public final List<Measurement> measures;
    public final HashMap<String, Layer> layerTable;

    public DataWrapper(List<Layer> _layers, List<Category> _cats, List<Measurement> _measures) {
        layers = _layers;
        cats = _cats;
        measures = _measures;
        layerTable = Utils.getLayerTable(layers);
    }
}
