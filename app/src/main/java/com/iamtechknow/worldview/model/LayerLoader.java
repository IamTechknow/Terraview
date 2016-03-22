package com.iamtechknow.worldview.model;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

public class LayerLoader extends AsyncTaskLoader<ArrayList<Layer>> {
    private ArrayList<Layer> layers;
    private LayerDatabase mHelper;

    public LayerLoader(Context c) {
        super(c);
        mHelper = new LayerDatabase(c);
    }

    //If the data is cached, use it!
    @Override
    protected void onStartLoading() {
        if(layers != null)
            deliverResult(layers);
        else
            forceLoad();
    }

    //Load stuff in a background stuff, what we're doing before
    @Override
    public ArrayList<Layer> loadInBackground() {
        return mHelper.queryLayers();
    }

    @Override
    public void deliverResult(ArrayList<Layer> data) {
        layers = data; //cache data
        super.deliverResult(data);
    }
}
