package com.iamtechknow.worldview.picker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.adapter.LayerDataAdapter;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.TapEvent;
import com.iamtechknow.worldview.util.Utils;

import java.util.ArrayList;

import rx.functions.Action1;

import static com.iamtechknow.worldview.picker.LayerActivity.SELECT_LAYER_TAB;

public class LayerFragment extends Fragment implements LayerView {
    private LayerPresenter presenter;
    private RxBus _rxBus;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        ArrayList<Layer> stack = getArguments().getParcelableArrayList(LayerActivity.RESULT_STACK);
        presenter = new LayerPresenterImpl(this, stack);
        _rxBus = RxBus.getInstance();
    }

    /**
     * Set up the event bus for the layer tab to response to taps on a measurement
     * to load all layers that are part of that measurement.
     */
    @Override
    public void onStart() {
        super.onStart();
        presenter.getData(getLoaderManager(), getActivity());
        _rxBus.toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof TapEvent && ((TapEvent) event).getTab() == SELECT_LAYER_TAB) { //call from measurement tab
                        ArrayList<String> layerTitles = presenter.getLayerTitlesForMeasurement(((TapEvent) event).getMeasurement());
                        ((LayerDataAdapter) mRecyclerView.getAdapter()).insertList(layerTitles);
                        presenter.updateSelectedItems(layerTitles);
                    }
                }
            });
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
    public void showInfo(String html) {
        Utils.showWebPage(getActivity(), html);
    }
}
