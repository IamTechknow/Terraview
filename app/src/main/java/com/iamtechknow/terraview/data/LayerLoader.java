package com.iamtechknow.terraview.data;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.iamtechknow.terraview.model.DataWrapper;
import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;
import java.util.TreeMap;

public class LayerLoader extends AsyncTaskLoader<DataWrapper> {
    private ArrayList<Layer> layers;
    private TreeMap<String, ArrayList<String>> cats, measures;
    private LayerDatabase mHelper;

    public LayerLoader(Context c) {
        super(c);
        mHelper = LayerDatabase.getInstance(c);
    }

    //If the data is cached, use it!
    @Override
    protected void onStartLoading() {
        if(measures != null)
            deliverResult(new DataWrapper(layers, cats, measures));
        else
            forceLoad();
    }

    //Load stuff in a background thread
    @Override
    public DataWrapper loadInBackground() {
        return new DataWrapper(mHelper.queryLayers(), mHelper.queryCategories(), mHelper.queryMeasurements());
    }

    @Override
    public void deliverResult(DataWrapper data) {
        layers = data.layers; //cache data from loadInBackground()
        cats = data.cats;
        measures = data.measures;
        super.deliverResult(data);
    }
}
