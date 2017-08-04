package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.util.Utils;

import java.lang.ref.WeakReference;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ColorMapPresenterImpl implements ColorMapPresenter {
    private static final String BASE_URL = "https://gibs.earthdata.nasa.gov";

    private WeakReference<ColorMapView> viewRef;

    private Disposable dataSub;

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
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build();

        dataSub = retrofit.create(ColorMapAPI.class).fetchData(id)
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
