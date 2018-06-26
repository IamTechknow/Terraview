package com.iamtechknow.terraview.colormaps;

import android.arch.lifecycle.ViewModel;

import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.util.Utils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;

/**
 * ViewModel that loads ColorMap data and can preserve the data through config changes.
 * An observable is used for the view to know what adapter position has a loaded ColorMap
 */
public class ColorMapViewModel extends ViewModel {

    //Retrofit client
    private Disposable dataSub;
    private ColorMapAPI api;

    private Subject<Integer> liveData;

    //ColorMap data
    private Map<String, ColorMap> data;

    public ColorMapViewModel(ColorMapAPI api, Subject<Integer> subject) {
        this.api = api;
        data = new HashMap<>();
        liveData = subject;
    }

    public void cancelSub() {
        if(dataSub != null)
            dataSub = null;
    }

    public void loadColorMap(int position, String id) {
        dataSub = api.fetchData(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(colorMap -> {
                Utils.cleanColorMap(colorMap);
                data.put(id, colorMap);
                liveData.onNext(position);
            });
    }

    public Observable<Integer> getLiveData() {
        return liveData;
    }

    public ColorMap getColorMap(String id) {
        return data.get(id);
    }
}
