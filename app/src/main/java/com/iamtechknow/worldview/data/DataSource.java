package com.iamtechknow.worldview.data;

import android.support.annotation.NonNull;

import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Contract that defines required APIs for a layer data source,
 * including the local database and XML/JSON parsers (remote source)
 */
public interface DataSource {

    //Callback for loading from local or remote source
    interface LoadCallback {
        void onDataLoaded();

        void onDataNotAvailable();
    }

    void loadData(@NonNull LoadCallback callback);

    ArrayList<Layer> getLayers();

    TreeMap<String, ArrayList<String>> getMeasurements();

    TreeMap<String, ArrayList<String>> getCategories();
}
