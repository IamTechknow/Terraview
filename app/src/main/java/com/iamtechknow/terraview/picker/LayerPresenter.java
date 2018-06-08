package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.Layer;

import java.util.List;

/**
 * Presenter for the layer tab of the picker activity.
 * Manages the data that was in the fragment and RecyclerView adapter,
 * but when necessary allows views to request data to update the UI
 */
public interface LayerPresenter {
    void attachView(LayerView v);

    void detachView();

    void handleEvent(Object event);

    void useRetrofit(String description);

    void updateSelectedItems(List<Layer> items);

    void setItemChecked(int position, boolean isSelected);

    boolean isItemChecked(int position);

    Layer searchLayerByTitle(String title);

    void getData();

    void changeStack(Layer l, boolean queue);

    void setMeasurement(String str);

    String getMeasurement();
}
