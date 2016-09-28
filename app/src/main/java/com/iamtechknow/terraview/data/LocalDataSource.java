package com.iamtechknow.terraview.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.iamtechknow.terraview.model.DataWrapper;
import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

/**
 * Implementation of a local data source by using loaders to access a SQLite database in the background.
 */
public class LocalDataSource implements DataSource, LoaderManager.LoaderCallbacks<DataWrapper> {
    //Loading objects
    private LoaderManager manager;
    private LoadCallback loadCallback;
    private LayerLoader loader;

    //Data
    private DataWrapper allData;

    public LocalDataSource(LoaderManager loadermanager, Context c) {
        loader = new LayerLoader(c);
        manager = loadermanager;
    }

    /**
     * Presenter call to start the load with loader callbacks
     * @param callback Used upon load completion to inform the presenter
     */
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
    public Hashtable<String, Layer> getLayerTable() {
        return allData != null ? allData.layerTable : null;
    }

    @Override
    public Loader<DataWrapper> onCreateLoader(int id, Bundle args) {
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<DataWrapper> loader, DataWrapper data) {
        allData = data;

        loadCallback.onDataLoaded();
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}
}
