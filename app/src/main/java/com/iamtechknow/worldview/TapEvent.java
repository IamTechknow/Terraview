package com.iamtechknow.worldview;

import com.iamtechknow.worldview.model.Layer;

public class TapEvent { //Container with event parameter
    private int tab;
    private Layer layer;
    private String measurement, category;

    public TapEvent(int num, Layer l, String s, String c) {
        tab = num;
        layer = l;
        measurement = s;
        category = c;
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

