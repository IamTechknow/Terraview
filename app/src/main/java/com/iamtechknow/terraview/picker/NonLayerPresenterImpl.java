package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.TapEvent;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_LAYER_TAB;
import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_MEASURE_TAB;

public class NonLayerPresenterImpl implements NonLayerContract.Presenter, DataSource.LoadCallback {
    private NonLayerContract.View view;
    private DataSource dataSource;
    private RxBus bus;
    private Disposable busSub, dataSub;

    private String category;

    public NonLayerPresenterImpl(NonLayerContract.View v, RxBus _bus, DataSource source, String category) {
        view = v;
        dataSource = source;
        bus = _bus;
        busSub = bus.toObserverable().subscribe(this::handleEvent);
        this.category = category != null ? category : Category.getAllCategory().getName();
    }

    @Override
    public void detachView() {
        busSub.dispose();
        busSub = null;
        if(dataSub != null) {
            dataSub.dispose();
            dataSub = null;
        }
        bus = null;
        view = null;
    }

    @Override
    public void emitEvent(String data) {
        if(view.isCategoryTab())
            bus.send(new TapEvent(SELECT_MEASURE_TAB, null, null, data));
        else
            bus.send(new TapEvent(SELECT_LAYER_TAB, null, data, null));
    }

    /**
     * Handle event from event bus, only used by measurement tab fragment
     * @param event Objected emitted from the RxBus
     */
    @Override
    public void handleEvent(Object event) {
        TapEvent tap = (TapEvent) event;
        if(tap != null && tap.getTab() == SELECT_MEASURE_TAB  && !view.isCategoryTab())
            getMeasurementListAndInsert(tap.getCategory());
    }

    @Override
    public void getData() {
        dataSource.loadData(this);
    }

    /**
     * Data is loaded. Load categories or measurements from the category
     */
    @Override
    public void onDataLoaded() {
        if(!view.isCategoryTab() && category != null)
            getMeasurementListAndInsert(category);
        else
            view.insertList(getCategoryList());
    }

    @Override
    public void onDataNotAvailable() {}

    //Form the list of categories to show, but we must convert the objects to Strings
    private ArrayList<String> getCategoryList() {
        ArrayList<String> result = new ArrayList<>();
        for(Category c : dataSource.getCategories())
            result.add(c.getName());
        return result;
    }

    private void getMeasurementListAndInsert(String category) {
        this.category = category;
        dataSub = dataSource.getMeasurementsForCategory(category)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(measurements -> view.insertMeasurements(this.category, measurements));
    }
}
