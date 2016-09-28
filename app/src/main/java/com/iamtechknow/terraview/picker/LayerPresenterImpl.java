package com.iamtechknow.terraview.picker;

import android.util.Log;
import android.util.SparseBooleanArray;

import com.iamtechknow.terraview.api.MetadataAPI;
import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.TapEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_LAYER_TAB;
import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_SUGGESTION;

public class LayerPresenterImpl implements LayerPresenter, DataSource.LoadCallback {
    private static final String BASE_URL = "https://worldview.earthdata.nasa.gov/";

    private WeakReference<LayerView> viewRef;
    private Retrofit retrofit;
    private RxBus bus;
    private Subscription busSub;

    private DataSource dataSource;

    //Manage the item positions that should be highlighted
    private SparseBooleanArray mSelectedPositions;

    //List for layers to be displayed handled as a stack
    private ArrayList<Layer> stack;

    //HashSet to keep track of selected elements from stack
    private HashSet<String> titleSet;

    //Used for state restoration in config change
    private String measurement;

    public LayerPresenterImpl(RxBus _bus, ArrayList<Layer> list, DataSource source, SparseBooleanArray array) {
        bus = _bus;
        dataSource = source;
        stack = list;
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        mSelectedPositions = array;
        titleSet = new HashSet<>();
    }

    @Override
    public void attachView(LayerView v) {
        viewRef = new WeakReference<>(v);
        busSub = bus.toObserverable().subscribe(this::handleEvent);
    }

    @Override
    public void detachView() {
        if(viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
        busSub.unsubscribe();
    }

    @Override
    public ArrayList<Layer> getCurrStack() {
        return stack;
    }

    @Override
    public void handleEvent(Object event) {
        TapEvent tap = (TapEvent) event;
        if(tap != null && getView() != null && tap.getTab() == SELECT_LAYER_TAB) {
            ArrayList<String> layerTitles = getLayerTitlesForMeasurement(tap.getMeasurement());
            getView().updateLayerList(layerTitles);
        } else if(tap != null && getView() != null && tap.getTab() == SELECT_SUGGESTION) {
            Layer l = searchLayerById(tap.getMeasurement());
            if(!titleSet.contains(l.getTitle())) { //check if not already selected first
                stack.add(l);
                updateListView();
            }
        }
    }

    /**
     * Simple use of Retrofit by obtaining the HTML page of a layer's description to be shown
     * @param description The portion of the URL for a layer's description
     */
    @Override
    public void useRetrofit(String description) {
        if(description == null)
            return;

        MetadataAPI api = retrofit.create(MetadataAPI.class);
        String[] temp = description.split("/"); //must split data for URL to work
        Call<ResponseBody> result = api.fetchData(temp[0], temp[1]);
        Observable.just(result).map(call -> {
            Response<ResponseBody> r = null;
            try {
                r = call.execute();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return r;
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Response<ResponseBody>>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    Log.w(getClass().getSimpleName(), e);
                }

                @Override
                public void onNext(Response<ResponseBody> r) {
                    try {
                        if(getView() != null)
                            getView().showInfo(r.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

    /**
     * Update the sparse boolean array by placing all layer titles onto the set
     * and then checking whether a given title from the measurement list is in the set,
     * then the list item should be highlighted by marking its position.
     * @param titles The list of titles currently shown on the layer tab
     */
    @Override
    public void updateSelectedItems(ArrayList<String> titles) {
        titleSet.clear();
        for(Layer l : stack)
            titleSet.add(l.getTitle());

        mSelectedPositions.clear();
        for (int i = 0; i < titles.size(); i++)
            if(titleSet.contains(titles.get(i)))
                setItemChecked(i, true);
    }

    @Override
    public void setItemChecked(int position, boolean isSelected) {
        mSelectedPositions.put(position, isSelected);
    }

    @Override
    public boolean isItemChecked(int position) {
        return mSelectedPositions.get(position);
    }

    @Override
    public Layer searchLayerByTitle(String title) {
        if(dataSource != null) {
            ArrayList<Layer> layers = dataSource.getLayers();

            for (Layer l : layers)
                if (l.getTitle().equals(title))
                    return l;
        }
        return null;
    }

    @Override
    public void getData() {
        dataSource.loadData(this);
    }

    /**
     * Modify the list of layers to be shown on the map
     * @param l The layer in question
     * @param queue Whether to place to or remove from the stack
     */
    @Override
    public void changeStack(Layer l, boolean queue) {
        if(queue)
            stack.add(l);
        else
            stack.remove(l);
    }

    /**
     * Get a list of the identifiers from the measurement and return all titles of corresponding layers
     * @param measurement String of the measurement that was tapped
     * @return A list of all titles of the layers belonging to the measurement
     */
    @Override
    public ArrayList<String> getLayerTitlesForMeasurement(String measurement) {
        this.measurement = measurement;
        ArrayList<String> id_list = dataSource.getMeasurements().get(measurement), _layerlist = new ArrayList<>();

        for(String id: id_list) {
            Layer temp = searchLayerById(id);
            _layerlist.add(temp != null ? temp.getTitle() : id);
        }

        updateSelectedItems(_layerlist);
        return _layerlist;
    }

    @Override
    public void setMeasurement(String str) {
        measurement = str;
    }

    @Override
    public String getMeasurement() {
        return measurement;
    }

    /**
     * If state restoration occurred and a measurement was saved (not when layer tab selected),
     * show the layer titles for that measurement, or show all the layer titles
     */
    @Override
    public void onDataLoaded() {
        updateListView();
    }

    @Override
    public void onDataNotAvailable() {

    }

    private LayerView getView() {
        return viewRef == null ? null : viewRef.get();
    }

    private void updateListView() {
        if(getView() != null) {
            if (measurement != null)
                getView().updateLayerList(getLayerTitlesForMeasurement(measurement));
            else {
                ArrayList<String> layer_list = new ArrayList<>();
                for (Layer l: dataSource.getLayers())
                    layer_list.add(l.getTitle());
                getView().populateList(layer_list);
                updateSelectedItems(layer_list);
            }
        }
    }

    /**
     * Find the layer in the hash table by using its ID as its key
     * @param id the layer's identifier
     * @return Either the layer containing that identifier or null
     */
    private Layer searchLayerById(String id) {
        return dataSource.getLayerTable().get(id);
    }
}
