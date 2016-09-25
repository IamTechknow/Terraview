package com.iamtechknow.terraview.picker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.terraview.Injection;
import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.LayerDataAdapter;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

import rx.functions.Action1;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_LAYER_TAB;

public class LayerFragment extends Fragment implements LayerView {
    private static final String MEAS_EXTRA = "measurement";

    private LayerPresenter presenter;
    private RxBus _rxBus;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        ArrayList<Layer> stack = getArguments().getParcelableArrayList(LayerActivity.RESULT_STACK);
        presenter = new LayerPresenterImpl(stack, Injection.provideLocalSource(getLoaderManager(), getActivity()));
        presenter.attachView(this);
        _rxBus = RxBus.getInstance();

        if(savedInstanceState != null)
            presenter.setMeasurement(savedInstanceState.getString(MEAS_EXTRA));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(MEAS_EXTRA, presenter.getMeasurement());
    }

    /**
     * Set up the event bus for the layer tab to response to taps on a measurement
     * to load all layers that are part of that measurement.
     */
    @Override
    public void onStart() {
        super.onStart();
        presenter.getData();
        _rxBus.toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof TapEvent && ((TapEvent) event).getTab() == SELECT_LAYER_TAB) //call from measurement tab
                        onNewMeasurement(((TapEvent) event).getMeasurement());
                }
            });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    //Inflate the fragment view and setup the RecyclerView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_layer, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        LayerDataAdapter l = new LayerDataAdapter(presenter);
        mRecyclerView.setAdapter(l);
        return rootView;
    }

    /**
     * Called when data is loaded to make the view populate the RecyclerView
     * @param layers List of all available layers from data source
     */
    @Override
    public void populateList(ArrayList<Layer> layers) {
        LayerDataAdapter adapter = (LayerDataAdapter) mRecyclerView.getAdapter();
        ArrayList<String> layer_list = new ArrayList<>();
        for (Layer l: layers)
            layer_list.add(l.getTitle());
        adapter.insertList(layer_list);
        presenter.updateSelectedItems(layer_list);
    }

    @Override
    public void onNewMeasurement(String measurement) {
        ArrayList<String> layerTitles = presenter.getLayerTitlesForMeasurement(measurement);
        ((LayerDataAdapter) mRecyclerView.getAdapter()).insertList(layerTitles);
        presenter.updateSelectedItems(layerTitles);
    }

    @Override
    public void showInfo(String html) {
        Utils.showWebPage(getActivity(), html);
    }
}
