package com.iamtechknow.worldview.data;

import android.support.annotation.NonNull;

import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.TreeMap;

public class LocalDataSource implements DataSource {
    @Override
    public void loadData(@NonNull LoadCallback callback) {

    }

    @Override
    public ArrayList<Layer> getLayers() {
        return null;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getMeasurements() {
        return null;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getCategories() {
        return null;
    }
}
