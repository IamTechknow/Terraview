package com.iamtechknow.worldview.picker;

import com.iamtechknow.worldview.data.DataSource;

import java.util.ArrayList;
import java.util.TreeMap;

public class NonLayerPresenterImpl implements NonLayerPresenter {
    private NonLayerView view;
    private DataSource data;

    public NonLayerPresenterImpl(NonLayerView _view) {
        view = _view;
    }

    @Override
    public void onStart() {

    }

    @Override
    public TreeMap<String, ArrayList<String>> getData(int type) {
        return null;
    }
}
