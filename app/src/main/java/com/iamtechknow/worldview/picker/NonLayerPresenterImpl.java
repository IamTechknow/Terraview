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

    @Override
    public TreeMap<String, ArrayList<String>> getMap(boolean isCategoryTab) {
        return dataSource == null ? null : (isCategoryTab ? dataSource.getCategories() : dataSource.getMeasurements());
    }

    @Override
    public ArrayList<String> getMeasurementList(String category) {
        TreeMap<String, ArrayList<String>> categories = dataSource.getCategories();
        return categories.get(category);
    }

    @Override
    public void getData(LoaderManager manager, Context c) {
        dataSource = new LocalDataSource(manager, c);
        dataSource.loadData(this);
    }

    @Override
    public void onDataLoaded() {
        view.insertList();
    }

    @Override
    public void onDataNotAvailable() {

    }
}
