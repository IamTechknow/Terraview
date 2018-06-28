package com.iamtechknow.terraview.picker;

import android.arch.lifecycle.ViewModel;
import android.util.SparseBooleanArray;

import com.iamtechknow.terraview.api.MetadataAPI;
import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.TapEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_LAYER_TAB;
import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_SUGGESTION;

/**
 * ViewModel for the layer tab of the picker activity.
 * Manages the data in the fragment and RecyclerView adapter, and allow access to data for UI
 */
public class LayerViewModel extends ViewModel implements DataSource.LoadCallback {
    private DataSource dataSource;
    private RxBus bus;
    private MetadataAPI metadataAPI;

    private CompositeDisposable subs;

    //Manage the item positions that should be highlighted
    private SparseBooleanArray selectedPositions;

    //List for layers to be displayed handled as a stack
    private ArrayList<Layer> stack, toDelete;
    private List<Layer> currData;

    //HashSet to keep track of selected elements from stack
    private HashSet<String> titleSet;

    //Live data for Layers and metadata HTML
    private Subject<String> metaLiveData;
    private Subject<List<Layer>> liveData;

    private String measurement;

    public LayerViewModel(DataSource dataSource, RxBus bus, MetadataAPI metadataAPI, SparseBooleanArray array,
                          ArrayList<Layer> list, ArrayList<Layer> delete, Subject<String> strSubject, Subject<List<Layer>> dataSubject) {
        this.dataSource = dataSource;
        this.bus = bus;
        this.metadataAPI = metadataAPI;
        stack = list;
        toDelete = delete;
        selectedPositions = array;
        metaLiveData = strSubject;
        liveData = dataSubject;

        titleSet = new HashSet<>();
    }

    public String getMeasurement() {
        return measurement;
    }

    public List<Layer> getCurrData() {
        return currData;
    }

    public Observable<String> getMetaLiveData() {
        return metaLiveData;
    }

    public Observable<List<Layer>> getLiveData() {
        return liveData;
    }

    public void startSubs() {
        subs = new CompositeDisposable(); //Create new disposable because they are not reusable
        subs.add(bus.toObservable().subscribe(this::handleEvent));
    }

    public void cancelSubs() {
        subs.dispose();
    }

    public void useRetrofit(String description) {
        if(description == null)
            return;

        int last_slash = description.lastIndexOf('/');
        String[] temp = { description.substring(0, last_slash), description.substring(last_slash + 1)}; //must split data at the last / for URL to work
        Call<ResponseBody> result = metadataAPI.fetchData(temp[0], temp[1]);

        subs.add(Single.just(result).map(call -> {
            Response<ResponseBody> r = null;
            try {
                r = call.execute();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return r;
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(r -> {
                try {
                    if(r.body() != null && metaLiveData.hasObservers())
                        metaLiveData.onNext(r.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
    }

    public void setItemChecked(int position, boolean isSelected) {
        selectedPositions.put(position, isSelected);
    }

    public boolean isItemChecked(int position) {
        return selectedPositions.get(position);
    }

    public Layer searchLayerByTitle(String title) {
        if(dataSource != null) {
            List<Layer> layers = dataSource.getLayers();

            for (Layer l : layers)
                if (l.getTitle().equals(title))
                    return l;
        }
        return null;
    }

    public void getData() {
        dataSource.loadData(this);
    }

    /**
     * Modify the list of layers to be shown on the map.
     * Calling this does not force a refresh of the hash set and boolean array.
     * @param l The layer in question
     * @param queue Whether to place to or remove from the stack
     */
    public void changeStack(Layer l, boolean queue) {
        if(queue) {
            stack.add(l);
            titleSet.add(l.getTitle());
            toDelete.remove(l);
        }
        else {
            stack.remove(l);
            titleSet.remove(l.getTitle());
            toDelete.add(l);
        }
    }

    /**
     * If state restoration occurred and a measurement was saved (not when layer tab selected),
     * show the layer titles for that measurement, or show all the layer titles
     */
    @Override
    public void onDataLoaded() {
        updateLayerData();
    }

    @Override
    public void onDataNotAvailable() {}

    private void handleEvent(TapEvent tap) {
        if(tap != null && tap.getTab() == SELECT_LAYER_TAB) {
            measurement = tap.getMeasurement();
            getLayerTitlesForMeasurement(tap.getMeasurement());
        } else if(tap != null && tap.getTab() == SELECT_SUGGESTION) {
            Layer l = searchLayerById(tap.getMeasurement());
            if(!titleSet.contains(l.getTitle())) { //check if not already selected first
                changeStack(l, true);
                updateLayerData(); //Without adapter, must force refresh of selected states
            }
        }
    }

    /**
     * Update the sparse boolean array by placing all layer titles onto the set
     * and then checking whether a given title from the measurement list is in the set,
     * then the list item should be highlighted by marking its position.
     * @param titles The list of titles currently shown on the layer tab
     */
    private void updateSelectedItems(List<Layer> titles) {
        titleSet.clear();
        for(Layer l : stack)
            titleSet.add(l.getTitle());

        selectedPositions.clear();
        for (int i = 0; i < titles.size(); i++)
            if(titleSet.contains(titles.get(i).getTitle()))
                setItemChecked(i, true);
    }

    private void updateLayerData() { //Get all layers for the current measurement
        if (measurement != null)
            getLayerTitlesForMeasurement(measurement); //async call
        else {
            currData = dataSource.getLayers();
            updateSelectedItems(currData);
            if(liveData.hasObservers())
                liveData.onNext(currData);
        }
    }

    private Layer searchLayerById(String id) {
        return dataSource.getLayerTable().get(id);
    }

    /**
     * Get a list of the identifiers from the measurement and return all titles of corresponding layers
     * @param measurement String of the measurement that was tapped
     */
    private void getLayerTitlesForMeasurement(String measurement) {
        subs.add(dataSource.getLayersForMeasurement(measurement)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(layers -> {
                currData = layers;
                updateSelectedItems(layers);
                if(liveData.hasObservers())
                    liveData.onNext(layers);
            }));
    }
}
