package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.Layer;

import java.util.List;

public interface LayerView {
    void populateList(List<Layer> list);

    void updateLayerList(List<Layer> list);

    void showInfo(String html);
}
