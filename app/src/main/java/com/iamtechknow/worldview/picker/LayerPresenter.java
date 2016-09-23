package com.iamtechknow.worldview.picker;

import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;

/**
 * Presenter for the layer tab of the picker activity.
 * Manages the data that was in the fragment and RecyclerView adapter,
 * but when necessary allows views to request data to update the UI
 */
public interface LayerPresenter {
    ArrayList<Layer> getCurrStack();

    void useRetrofit(String description);

    void updateSelectedItems(ArrayList<String> items);

    void setItemChecked(int position, boolean isSelected);

    boolean isItemChecked(int position);

    Layer searchLayerByTitle(String title);

    void getData();

    void changeStack(Layer l, boolean queue);

    void setMeasurement(String str);

    String getMeasurement();

    ArrayList<String> getLayerTitlesForMeasurement(String measurement);
}
