package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;

public interface LayerView {
    void populateList(ArrayList<String> list);

    void updateLayerList(ArrayList<String> list);

    void showInfo(String html);
}
