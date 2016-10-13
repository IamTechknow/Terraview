package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.TapEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import rx.Subscription;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_LAYER_TAB;
import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_MEASURE_TAB;

public class NonLayerPresenterImpl implements NonLayerPresenter, DataSource.LoadCallback {
    private WeakReference<NonLayerView> viewRef;
    private DataSource dataSource;
    private RxBus bus;
    private Subscription busSub;

    //Used for state restoration in config change
    private String category;

    public NonLayerPresenterImpl(RxBus _bus, DataSource source) {
        dataSource = source;
        bus = _bus;
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
    public TreeMap<String, ArrayList<String>> getMap(boolean isCategoryTab) {
        return dataSource == null ? null : (isCategoryTab ? dataSource.getCategories() : dataSource.getMeasurements());
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
            getView().insertMeasurements(getMeasurementList(tap.getCategory()));
    }

    @Override
    public ArrayList<String> getMeasurementList(String category) {
        this.category = category;
        return dataSource.getCategories().get(category);
    }

    @Override
    public ArrayList<String> getDefaultList() {
        ArrayList<String> result = new ArrayList<>();
        if(getView() != null) {
            TreeMap<String, ArrayList<String>> map = getMap(getView().isCategoryTab());

            for (Map.Entry<String, ArrayList<String>> e : map.entrySet())
                result.add(e.getKey());
        }
        return result;
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
     * Data is loaded. Either have the view load the default list (all measurements/categories)
     * or have the measurement tab load current measurements due to config change
     */
    @Override
    public void onDataLoaded() {
        if(getView() != null) {
            if(!getView().isCategoryTab() && category != null)
                getView().insertMeasurements(getMeasurementList(category));
            else
                getView().insertList(getDefaultList());
        }
    }

    @Override
    public void onDataNotAvailable() {

    }

    private NonLayerView getView() {
        return viewRef == null ? null : viewRef.get();
    }

    //Clean up references to avoid memory leaks
    private void cleanUp() {
        busSub.unsubscribe();
        busSub = null;
        bus = null;
    }
}
