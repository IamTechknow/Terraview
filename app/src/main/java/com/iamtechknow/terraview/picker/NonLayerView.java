package com.iamtechknow.terraview.picker;

import java.util.ArrayList;

/**
 * View for the tabs that represent categories and measurements, which have same functionality
 */
public interface NonLayerView {
    void insertList(ArrayList<String> list);

    void insertMeasurements(ArrayList<String> list);

    boolean isCategoryTab();
}
