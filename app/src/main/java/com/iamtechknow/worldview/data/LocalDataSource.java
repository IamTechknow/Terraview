package com.iamtechknow.worldview.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.iamtechknow.worldview.model.DataWrapper;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;

import java.util.ArrayList;
import java.util.TreeMap;

public class LocalDataSource implements DataSource, LoaderManager.LoaderCallbacks<DataWrapper> {
    //Loading objects
    private LoaderManager manager;
    private LoadCallback loadCallback;
    private Context context;

    //Data
    private DataWrapper allData;

    public LocalDataSource(LoaderManager loadermanager, Context c) {
        context = c;
        manager = loadermanager;
    }

    @Override
    public void loadData(@NonNull LoadCallback callback) {
        loadCallback = callback;
        manager.initLoader(0, null, this);
    }

    @Override
    public ArrayList<Layer> getLayers() {
        return allData != null ? allData.layers : null;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getMeasurements() {
        return allData != null ? allData.measures : null;
    }

    @Override
    public TreeMap<String, ArrayList<String>> getCategories() {
        return allData != null ? allData.cats : null;
    }

    @Override
    public Loader<DataWrapper> onCreateLoader(int id, Bundle args) {
        return new LayerLoader(context);
    }

    @Override
    public void onLoadFinished(Loader<DataWrapper> loader, DataWrapper data) {
        allData = data;

        //Prevent loitering
        context = null;
        manager = null;

        loadCallback.onDataLoaded();
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}
}
