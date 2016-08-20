package com.iamtechknow.worldview.picker;

import android.content.Context;
import android.support.v4.app.LoaderManager;

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

    Layer searchLayerById(String id);

    Layer searchLayerByTitle(String title);

    void getData(LoaderManager manager, Context c);

    void changeStack(Layer l, boolean queue);
}
