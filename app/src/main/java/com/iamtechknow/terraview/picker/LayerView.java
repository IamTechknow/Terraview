package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;

public interface LayerView {
    void populateList(ArrayList<Layer> layers);

    void onNewMeasurement(String measurement);

    void showInfo(String html);
}
