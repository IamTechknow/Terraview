package com.iamtechknow.terraview.picker;

import android.arch.lifecycle.ViewModel;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Measurement;
import com.iamtechknow.terraview.model.TapEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_LAYER_TAB;
import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_MEASURE_TAB;

public class NonLayerViewModel extends ViewModel implements DataSource.LoadCallback {
    private DataSource dataSource;
    private RxBus bus;

    private Disposable busSub, dataSub;

    //One or the other will be used for any given instance
    private Subject<List<Measurement>> liveMeasures;
    private Subject<List<String>> liveCategories;

    //Model data
    private boolean forCategoryTab;
    private String category;
    private List<Measurement> measurements;
    private List<String> categories;

    public NonLayerViewModel(DataSource dataSource, RxBus bus, boolean tab,
                             Subject<List<Measurement>> subject, Subject<List<String>> strSubject) {
        this.dataSource = dataSource;
        this.bus = bus;
        this.category = category != null ? category : Category.getAllCategory().getName();
        forCategoryTab = tab;
        liveMeasures = subject;
        liveCategories = strSubject;
    }

    public void startSubs() {
        busSub = bus.toObservable().subscribe(this::handleEvent);
    }

    public void cancelSubs() {
        busSub.dispose();
        if(dataSub != null)
            dataSub.dispose();
    }

    public void emitEvent(String data) {
        if(forCategoryTab)
            bus.send(new TapEvent(SELECT_MEASURE_TAB, null, null, data));
        else
            bus.send(new TapEvent(SELECT_LAYER_TAB, null, data, null));
    }

    public void getData() {
        dataSource.loadData(this);
    }

    public Subject<List<Measurement>> getLiveMeasures() {
        return liveMeasures;
    }

    public Subject<List<String>> getLiveCategories() {
        return liveCategories;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public List<String> getCategories() {
        return categories;
    }

    //FIXME: Replace loader, as they don't work right after config change (they do before that)
    @Override
    public void onDataLoaded() {
        if(!forCategoryTab)
            getMeasurementList(category);
        else {
            categories = getCategoryList();
            if(liveCategories.hasObservers())
                liveCategories.onNext(categories);
        }
    }

    @Override
    public void onDataNotAvailable() {}

    private void handleEvent(TapEvent tap) {
        if(tap != null && tap.getTab() == SELECT_MEASURE_TAB  && !forCategoryTab)
            getMeasurementList(tap.getCategory());
    }

    //Form the list of categories to show, but we must convert the objects to Strings
    private ArrayList<String> getCategoryList() {
        ArrayList<String> result = new ArrayList<>();
        for(Category c : dataSource.getCategories())
            result.add(c.getName());
        return result;
    }

    private void getMeasurementList(String category) {
        this.category = category;
        dataSub = dataSource.getMeasurementsForCategory(category)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(measurements -> {
                this.measurements = measurements;
                if(liveMeasures.hasObservers())
                    liveMeasures.onNext(measurements);
            });
    }
}
