package com.iamtechknow.terraview.picker;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Presenter for the tabs that represent categories and measurements
 * Distinguishes between either view by having views send data type when requesting data
 * Manages the data that was in the fragment and RecyclerView adapter,
 * but when necessary allows views to request data to update the UI
 */
public interface NonLayerPresenter {

    void attachView(NonLayerView v);

    void detachView();

    void emitEvent(String data);

    void handleEvent(Object event);

    TreeMap<String, ArrayList<String>> getMap(boolean isCategoryTab);

    ArrayList<String> getMeasurementList(String category);

    ArrayList<String> getDefaultList();

    void getData();

    void setCategory(String cat);

    String getCategory();
}
