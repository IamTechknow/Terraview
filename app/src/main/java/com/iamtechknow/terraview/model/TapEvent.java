package com.iamtechknow.terraview.model;

public class TapEvent { //Container with event parameter
    private int tab, intArg;
    private Layer layer;
    private Event eonetEvent;
    private String measurement, category;

    public TapEvent(int num, Layer l, String _measurement, String _category) {
        tab = num;
        layer = l;
        measurement = _measurement;
        category = _category;
    }

    public TapEvent(int num, Event e) {
        tab = num;
        eonetEvent = e;
    }

    public TapEvent(int num, int arg) {
        tab = num;
        intArg = arg;
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

    public Event getEonetEvent() {
        return eonetEvent;
    }

    public int getArg() {
        return intArg;
    }
}

