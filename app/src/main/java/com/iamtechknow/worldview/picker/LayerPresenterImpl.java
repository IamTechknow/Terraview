package com.iamtechknow.worldview.picker;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.util.SparseBooleanArray;

import com.iamtechknow.worldview.api.MetadataAPI;
import com.iamtechknow.worldview.data.DataSource;
import com.iamtechknow.worldview.data.LocalDataSource;
import com.iamtechknow.worldview.model.Layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LayerPresenterImpl implements LayerPresenter, DataSource.LoadCallback {
    private static final String BASE_URL = "https://worldview.earthdata.nasa.gov/";

    private LayerView view;
    private Retrofit retrofit;

    private DataSource dataSource;

    //Manage the item positions that should be highlighted
    private SparseBooleanArray mSelectedPositions;

    //List for layers to be displayed handled as a stack
    private ArrayList<Layer> stack;

    //HashSet to keep track of selected elements from stack
    private HashSet<String> titleSet;

    public LayerPresenterImpl(LayerView _view, ArrayList<Layer> list) {
        view = _view;
        stack = list;
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        mSelectedPositions = new SparseBooleanArray();
        titleSet = new HashSet<>();
    }

    @Override
    public ArrayList<Layer> getCurrStack() {
        return stack;
    }

    @Override
    public void useRetrofit(String description) {
        if(description == null)
            return;

        MetadataAPI api = retrofit.create(MetadataAPI.class);
        String[] temp = description.split("/"); //must split data for URL to work
        final Call<ResponseBody> result = api.fetchData(temp[0], temp[1]);
        Observable.just(true).map(new Func1<Boolean, Response<ResponseBody> >() {
            @Override
            public Response<ResponseBody> call(Boolean aBoolean) {
                Response<ResponseBody> r = null;
                try {
                    r = result.execute();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return r;
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Response<ResponseBody>>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {}

                @Override
                public void onNext(Response<ResponseBody> r) {
                    try {
                        view.showInfo(r.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
    }

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
    public void getData(LoaderManager manager, Context c) {
        dataSource = new LocalDataSource(manager, c);
        dataSource.loadData(this);
    }

    @Override
    public void changeStack(Layer l, boolean queue) {
        if(queue)
            stack.add(l);
        else
            stack.remove(l);
    }

    @Override
    public ArrayList<String> getLayerTitlesForMeasurement(String measurement) {
        TreeMap<String, ArrayList<String>> measurements = dataSource.getMeasurements();

        ArrayList<String> id_list = measurements.get(measurement), _layerlist = new ArrayList<>();
        for(String id: id_list) {
            Layer temp = searchLayerById(id);
            _layerlist.add(temp != null ? temp.getTitle() : id);
        }

        return _layerlist;
    }

    @Override
    public void onDataLoaded() {
        view.populateList(dataSource.getLayers());
    }

    @Override
    public void onDataNotAvailable() {

    }

    private Layer searchLayerById(String id) {
        if(dataSource != null) {
            ArrayList<Layer> layers = dataSource.getLayers();

            for (Layer l : layers)
                if (l.getIdentifier().equals(id))
                    return l;
        }
        return null;
    }
}
