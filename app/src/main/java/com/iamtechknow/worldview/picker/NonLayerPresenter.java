package com.iamtechknow.worldview.picker;

import android.content.Context;
import android.support.v4.app.LoaderManager;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Presenter for the tabs that represent categories and measurements
 * Distinguishes between either view by having views send data type when requesting data
 * Manages the data that was in the fragment and RecyclerView adapter,
 * but when necessary allows views to request data to update the UI
 */
public interface NonLayerPresenter {
    TreeMap<String, ArrayList<String>> getMap(boolean isCategoryTab);

    ArrayList<String> getMeasurementList(String category);

    void getData();

    void setCategory(String cat);

    String getCategory();
}
