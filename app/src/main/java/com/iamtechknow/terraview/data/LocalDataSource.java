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

import io.reactivex.Single;

/**
 * Implementation of a local data source by using loaders to access a SQLite database in the background.
 */
public class LocalDataSource implements DataSource, LoaderManager.LoaderCallbacks<DataWrapper> {
    //Loading objects
    private LoaderManager manager;
    private LoadCallback loadCallback;
    private LayerLoader loader;
    private TVDatabase db;

    //Data
    private DataWrapper allData;

    public LocalDataSource(LoaderManager loadermanager, Context c) {
        db = TVDatabase.getInstance(c);
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

    @Override
    public Single<List<Layer>> getLayersForMeasurement(String m) {
        return db.getJoinDAO().getLayersForMeasurement(m);
    }

    @Override
    public Single<List<Measurement>> getMeasurementsForCategory(String c) {
        return db.getJoinDAO().getMeasurementsForCategory(c);
    }

    @Override
    public List<Category> getCategories() {
        return allData != null ? allData.cats : null;
    }

    @Override
    public HashMap<String, Layer> getLayerTable() {
        return allData != null ? allData.layerTable : null;
    }

    @NonNull
    @Override
    public Loader<DataWrapper> onCreateLoader(int id, Bundle args) {
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<DataWrapper> loader, DataWrapper data) {
        allData = data;

        loadCallback.onDataLoaded();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<DataWrapper> loader) {}
}
