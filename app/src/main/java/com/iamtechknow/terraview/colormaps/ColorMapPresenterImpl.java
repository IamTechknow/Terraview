package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.util.Utils;

import java.lang.ref.WeakReference;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ColorMapPresenterImpl implements ColorMapPresenter {
    private WeakReference<ColorMapView> viewRef;

    private Disposable dataSub;

    private ColorMapAPI api;

    public ColorMapPresenterImpl(ColorMapAPI service) {
        api = service;
    }

    @Override
    public void attachView(ColorMapView v) {
        viewRef = new WeakReference<>(v);
    }

    @Override
    public void detachView() {
        if(dataSub != null) {
            dataSub.dispose();
            dataSub = null;
        }

        if(viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
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
                if(viewRef != null && viewRef.get() != null) {
                    Utils.cleanColorMap(colorMap);
                    viewRef.get().setColorMapData(colorMap);
                }
            });
    }
}
