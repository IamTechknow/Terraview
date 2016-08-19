package com.iamtechknow.worldview.picker;

import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Presenter for the layer tab of the picker activity.
 * Manages the data that was in the fragment and RecyclerView adapter,
 * but when necessary allows views to request data to update the UI
 */
public interface LayerPresenter {
    ArrayList<Layer> getCurrStack();

    void useRetrofit(String description);

    void updateSelectedItems(HashSet<String> set);

    void setItemChecked(int position, boolean isSelected); //TODO: these two may be called by data adapter

    boolean isItemChecked(int position);

    Layer searchLayer(int pos);
}
