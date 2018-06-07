package com.iamtechknow.terraview.data;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.DataWrapper;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.Measurement;

import java.util.List;

public class LayerLoader extends AsyncTaskLoader<DataWrapper> {
    private List<Layer> layers;
    private List<Category> cats;
    private List<Measurement> measures;
    private WVDatabase db;

    public LayerLoader(Context c) {
        super(c);
        db = WVDatabase.getInstance(c);
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
        return new DataWrapper(db.getLayerDao().getLayers(), db.getCategoryDao().getCategories(), db.getMeasurementDao().getMeasurements());
    }

    @Override
    public void deliverResult(DataWrapper data) {
        layers = data.layers; //cache data from loadInBackground()
        cats = data.cats;
        measures = data.measures;
        super.deliverResult(data);
    }
}
