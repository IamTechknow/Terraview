package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.TreeMap;

public class NonLayerPresenterImpl implements NonLayerPresenter, DataSource.LoadCallback {
    private WeakReference<NonLayerView> viewRef;
    private DataSource dataSource;

    //Used for state restoration in config change
    private String category;

    public NonLayerPresenterImpl(DataSource source) {
        dataSource = source;
    }

    @Override
    public void attachView(NonLayerView v) {
        viewRef = new WeakReference<>(v);
    }

    @Override
    public void detachView() {
        if(viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    @Override
    public TreeMap<String, ArrayList<String>> getMap(boolean isCategoryTab) {
        return dataSource == null ? null : (isCategoryTab ? dataSource.getCategories() : dataSource.getMeasurements());
    }

    @Override
    public ArrayList<String> getMeasurementList(String category) {
        this.category = category;
        TreeMap<String, ArrayList<String>> categories = dataSource.getCategories();
        return categories.get(category);
    }

    @Override
    public void getData() {
        dataSource.loadData(this);
    }

    @Override
    public void setCategory(String cat) {
        category = cat;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void onDataLoaded() {
        if(viewRef != null)
            viewRef.get().insertList();
    }

    @Override
    public void onDataNotAvailable() {

    }
}
