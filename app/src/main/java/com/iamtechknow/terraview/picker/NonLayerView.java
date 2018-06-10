package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

/**
 * View for the tabs that represent categories and measurements, which have same functionality
 */
public interface NonLayerView {
    void insertList(List<String> list);

    void insertMeasurements(String category, List<Measurement> list);

    boolean isCategoryTab();
}
