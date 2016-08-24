package com.iamtechknow.worldview.colormaps;

import com.iamtechknow.worldview.api.ColorMapAPI;
import com.iamtechknow.worldview.model.ColorMap;
import com.iamtechknow.worldview.model.ColorMapEntry;

import java.io.IOException;
import java.util.HashSet;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ColorMapPresenterImpl implements ColorMapPresenter {
    private static final String BASE_URL = "http://gibs.earthdata.nasa.gov";

    private ColorMapView view;

    public ColorMapPresenterImpl(ColorMapView _view) {
        view = _view;
    }

    @Override
    public void parseColorMap(String id) {
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build();

        ColorMapAPI api = retrofit.create(ColorMapAPI.class);
        Call<ColorMap> map = api.fetchData(id);

        Observable.just(map).map(new Func1<Call<ColorMap>, Response<ColorMap>>() {
            @Override
            public Response<ColorMap> call(Call<ColorMap> map_call) {
                Response<ColorMap> r = null;
                try {
                    r = map_call.execute();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return r;
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Response<ColorMap>>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {}

                @Override
                public void onNext(Response<ColorMap> r) {
                    cleanColorMap(r.body());
                    view.setColorMapData(r.body());
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
