package com.iamtechknow.worldview.picker;

import com.iamtechknow.worldview.data.DataSource;

import java.util.ArrayList;
import java.util.TreeMap;

public class NonLayerPresenterImpl implements NonLayerPresenter, DataSource.LoadCallback {
    private NonLayerView view;
    private DataSource dataSource;

    //Used for state restoration in config change
    private String category;

    public NonLayerPresenterImpl(NonLayerView _view, DataSource source) {
        view = _view;
        dataSource = source;
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
        view.insertList();
    }

    @Override
    public void onDataNotAvailable() {

    }
}
