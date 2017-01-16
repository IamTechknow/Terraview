package com.iamtechknow.terraview.picker;

import java.util.ArrayList;

public interface LayerView {
    void populateList(ArrayList<String> list);

    void updateLayerList(ArrayList<String> list);

    void showInfo(String html);
}
