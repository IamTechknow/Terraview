package com.iamtechknow.terraview.data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.DataWrapper;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.Measurement;

import java.util.List;
import java.util.HashMap;

/**
 * Implementation of a local data source by using loaders to access a SQLite database in the background.
 */
public class LocalDataSource implements DataSource, LoaderManager.LoaderCallbacks<DataWrapper> {
    //Loading objects
    private LoaderManager manager;
    private LoadCallback loadCallback;
    private LayerLoader loader;
    private WVDatabase db;

    //Data
    private DataWrapper allData;

    public LocalDataSource(LoaderManager loadermanager, Context c) {
        db = WVDatabase.getInstance(c);
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
    public List<Layer> getLayers() {
        return allData != null ? allData.layers : null;
    }

    //TODO:When this is working, replace it with querying just title column
    @Override
    public List<Layer> getLayersForMeasurement(String m) {
        return db.getMeasureLayerJoinDao().getLayersForMeasurement(m);
    }

    @Override
    public List<Measurement> getMeasurementsForCategory(String c) {
        return db.getCatMeasureJoinDao().getMeasurementsforCategory(c);
    }

    @Override
    public List<Category> getCategories() {
        return allData != null ? allData.cats : null;
    }

    @Override
    public HashMap<String, Layer> getLayerTable() {
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
