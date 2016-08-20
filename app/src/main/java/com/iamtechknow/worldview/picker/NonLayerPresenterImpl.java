package com.iamtechknow.worldview.picker;

import android.content.Context;
import android.support.v4.app.LoaderManager;

import com.iamtechknow.worldview.data.DataSource;
import com.iamtechknow.worldview.data.LocalDataSource;

import java.util.ArrayList;
import java.util.TreeMap;

public class NonLayerPresenterImpl implements NonLayerPresenter, DataSource.LoadCallback {
    private NonLayerView view;
    private DataSource dataSource;

    public NonLayerPresenterImpl(NonLayerView _view) {
        view = _view;
    }

    @Override
    public void onStart() {

    }

    public TreeMap<String, ArrayList<String>> getMap(int type) {
        return dataSource == null ? null : (type == 0 ? dataSource.getCategories() : dataSource.getMeasurements());
    }

    public void getData(LoaderManager manager, Context c) {
        dataSource = new LocalDataSource(manager, c);
        dataSource.loadData(this);
    }

    @Override
    public void onDataLoaded() {

    }

    @Override
    public void onDataNotAvailable() {

    }
}
