package com.iamtechknow.terraview.colormaps;

import android.util.Log;

import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.ColorMapEntry;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class ColorMapPresenterImpl implements ColorMapPresenter {
    private static final String BASE_URL = "https://gibs.earthdata.nasa.gov";

    private WeakReference<ColorMapView> viewRef;

    @Override
    public void attachView(ColorMapView v) {
        viewRef = new WeakReference<>(v);
    }

    @Override
    public void detachView() {
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
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build();

        ColorMapAPI api = retrofit.create(ColorMapAPI.class);
        Call<ColorMap> map = api.fetchData(id);

        Observable.just(map).map(map_call -> {
            Response<ColorMap> r = null;
            try {
                r = map_call.execute();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return r;
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Response<ColorMap>>() {
                @Override
                public void onSubscribe(Disposable disposable) {}

                @Override
                public void onComplete() {}

                @Override
                public void onError(Throwable e) {
                    Log.w(getClass().getSimpleName(), e);
                }

                @Override
                public void onNext(Response<ColorMap> r) {
                    if(viewRef.get() != null) {
                        cleanColorMap(r.body());
                        viewRef.get().setColorMapData(r.body());
                    }
                }
            });
    }

    /**
     * A few entries are considered invalid, find and delete them
     * @param map ColorMap that was parsed
     */
    private void cleanColorMap(ColorMap map) {
        HashSet<ColorMapEntry> set = new HashSet<>();

        for(ColorMapEntry e : map.getList())
            if(e.isInvalid())
                set.add(e);
        map.getList().removeAll(set);
    }
}
