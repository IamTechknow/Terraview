package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.util.Utils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ColorMapPresenterImpl implements ColorMapContract.Presenter {
    private ColorMapContract.View view;

    private Disposable dataSub;

    private ColorMapAPI api;

    public ColorMapPresenterImpl(ColorMapContract.View v, ColorMapAPI service) {
        view = v;
        api = service;
    }

    @Override
    public void detachView() {
        if(dataSub != null) {
            dataSub.dispose();
            dataSub = null;
        }
        view = null;
    }

    /**
     * Given the id, use a background thread to retrieve and parse the XML via RxJava and Retrofit.
     * When finished call the view to draw the colorMap.
     * @param id the layer's identifier
     */
    @Override
    public void parseColorMap(String id) {
        dataSub = api.fetchData(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(colorMap -> {
                Utils.cleanColorMap(colorMap);
                view.setColorMapData(colorMap);
            });
    }
}
