package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.TapEvent;

import java.util.List;

public interface LayerContract {
    interface View {
        void populateList(List<Layer> list);

        void updateLayerList(String measurement, List<Layer> list);

        void showInfo(String html);
    }

    /**
     * Presenter for the layer tab of the picker activity.
     * Manages the data that was in the fragment and RecyclerView adapter,
     * but when necessary allows views to request data to update the UI
     */
    interface Presenter {
        void detachView();

        void handleEvent(TapEvent event);

        void useRetrofit(String description);

        void updateSelectedItems(List<Layer> items);

        void setItemChecked(int position, boolean isSelected);

        boolean isItemChecked(int position);

        Layer searchLayerByTitle(String title);

        void getData();

        void changeStack(Layer l, boolean queue);
    }
}
