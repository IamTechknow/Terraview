package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.TapEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_LAYER_TAB;
import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_MEASURE_TAB;

public class NonLayerPresenterImpl implements NonLayerPresenter, DataSource.LoadCallback {
    private WeakReference<NonLayerView> viewRef;
    private DataSource dataSource;
    private RxBus bus;
    private Disposable busSub, dataSub;

    //Used for state restoration in config change
    private String category;

    public NonLayerPresenterImpl(RxBus _bus, DataSource source) {
        dataSource = source;
        bus = _bus;
        category = Category.getAllCategory().getName();
    }

    @Override
    public void attachView(NonLayerView v) {
        viewRef = new WeakReference<>(v);
        busSub = bus.toObserverable().subscribe(this::handleEvent);
    }

    @Override
    public void detachView() {
        if(viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
        cleanUp();
    }

    @Override
    public void emitEvent(String data) {
        if(getView() != null) {
            if(getView().isCategoryTab())
                bus.send(new TapEvent(SELECT_MEASURE_TAB, null, null, data));
            else
                bus.send(new TapEvent(SELECT_LAYER_TAB, null, data, null));
        }
    }

    /**
     * Handle event from event bus, only used by measurement tab fragment
     * @param event Objected emitted from the RxBus
     */
    @Override
    public void handleEvent(Object event) {
        TapEvent tap = (TapEvent) event;
        if(tap != null && tap.getTab() == SELECT_MEASURE_TAB && getView() != null && !getView().isCategoryTab())
            getMeasurementListAndInsert(tap.getCategory());
    }

    @Override
    public void getData() {
        dataSource.loadData(this);
    }

    @Override
    public void setCategory(String cat) {
        category = cat;
    }

    @Override
    public String getCategory() {
        return category;
    }

    /**
     * Data is loaded. Load categories or measurements from the category
     */
    @Override
    public void onDataLoaded() {
        if(getView() != null) {
            if(!getView().isCategoryTab() && category != null)
                getMeasurementListAndInsert(category);
            else
                getView().insertList(getCategoryList());
        }
    }

    @Override
    public void onDataNotAvailable() {}

    private NonLayerView getView() {
        return viewRef == null ? null : viewRef.get();
    }

    //Clean up references to avoid memory leaks
    private void cleanUp() {
        busSub.dispose();
        busSub = null;
        if(dataSub != null) {
            dataSub.dispose();
            dataSub = null;
        }
        bus = null;
    }

    //Form the list of categories to show, but we must convert the objects to Strings
    private ArrayList<String> getCategoryList() {
        ArrayList<String> result = new ArrayList<>();
        if(getView() != null) {
            for(Category c : dataSource.getCategories())
                result.add(c.getName());
        }
        return result;
    }

    private void getMeasurementListAndInsert(String category) {
        this.category = category;
        dataSub = dataSource.getMeasurementsForCategory(category)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(getView()::insertMeasurements);
    }
}
