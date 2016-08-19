package com.iamtechknow.worldview.picker;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Presenter for the tabs that represent categories and measurements
 * Distinguishes between either view by having views send data type when requesting data
 * Manages the data that was in the fragment and RecyclerView adapter,
 * but when necessary allows views to request data to update the UI
 */
public interface NonLayerPresenter {
    void onStart();

    TreeMap<String, ArrayList<String>> getData(int type);
}
