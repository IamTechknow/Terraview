package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

public interface NonLayerContract {
    /**
     * View for the tabs that represent categories and measurements, which have same functionality
     */
    interface View {
        void insertList(List<String> list);

        void insertMeasurements(String category, List<Measurement> list);

        boolean isCategoryTab();
    }

    /**
     * Presenter for the tabs that represent categories and measurements
     * Distinguishes between either view by having views send data type when requesting data
     * Manages the data that was in the fragment and RecyclerView adapter,
     * but when necessary allows views to request data to update the UI
     */
    interface Presenter {
        void detachView();

        void emitEvent(String data);

        void handleEvent(Object event);

        void getData();
    }
}
