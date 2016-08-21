package com.iamtechknow.worldview.model;

public class TapEvent { //Container with event parameter
    private int tab;
    private Layer layer;
    private String measurement, category;

    public TapEvent(int num, Layer l, String _measurement, String _category) {
        tab = num;
        layer = l;
        measurement = _measurement;
        category = _category;
    }

    public int getTab() {
        return tab;
    }

    public Layer getLayer() {
        return layer;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getCategory() {
        return category;
    }
}

