package com.iamtechknow.terraview.data;

import android.support.annotation.NonNull;

import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.Measurement;
import com.iamtechknow.terraview.util.Utils;

import java.util.List;
import java.util.HashMap;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of a local data source by using loaders to access the Room database.
 */
public class LocalDataSource implements DataSource {
    private TVDatabase db;

    //Cached data
    private CompositeDisposable subs;
    private List<Layer> layers;
    private List<Measurement> measurements;
    private List<Category> categories;
    private HashMap<String, Layer> layerMap;

    public LocalDataSource(TVDatabase _db) {
        db = _db;
        subs = new CompositeDisposable();
    }

    /**
     * ViewModel call to start loading data
     * @param callback Used upon load completion to inform the viewModel
     */
    @Override
    public void loadData(@NonNull LoadCallback callback) {
        subs.add(db.getTVDao().getMeasurements()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(measures -> measurements = measures)
        );

        subs.add(db.getTVDao().getCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(cats -> categories = cats)
        );

        subs.add(db.getTVDao().getLayers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(layers1 -> {
                layers = layers1;
                layerMap = Utils.getLayerTable(layers);
                callback.onDataLoaded();
            })
        );
    }

    @Override
    public List<Layer> getLayers() {
        return layers;
    }

    @Override
    public List<Category> getCategories() {
        return categories;
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
    public HashMap<String, Layer> getLayerTable() {
        return layerMap;
    }
}
