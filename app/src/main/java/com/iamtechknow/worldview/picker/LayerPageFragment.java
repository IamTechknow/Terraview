package com.iamtechknow.worldview.picker;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.worldview.picker.LayerActivity;
import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.picker.RxBus;
import com.iamtechknow.worldview.model.TapEvent;
import com.iamtechknow.worldview.adapter.*;
import com.iamtechknow.worldview.model.DataWrapper;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Map;

import rx.functions.Action1;

/**
 * Fragment with different modes to correspond to different behavior for each page for three different instances.
 */
public class LayerPageFragment extends Fragment implements LoaderManager.LoaderCallbacks<DataWrapper> {
    public static final int ARG_CAT = 0, ARG_MEASURE = 1, ARG_LAYER = 2;
    public static final String EXTRA_ARG = "arg";

    private RecyclerView mRecyclerView;
    private int mode;
    private RxBus _rxBus;

    //Worldview Data
    private ArrayList<Layer> layers, stack;
    private TreeMap<String, ArrayList<String>> categories, measurements;

    //HashSet to keep track of selected elements from stack
    private HashSet<String> layerSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mode = getArguments().getInt(EXTRA_ARG, ARG_CAT);
        _rxBus = ((LayerActivity) getActivity()).getRxBusSingleton();

        //For the layer tab fragment, check what layers were sent from LayerActivity
        if(mode == ARG_LAYER) {
            stack = getArguments().getParcelableArrayList(LayerActivity.RESULT_STACK);
            layerSet = new HashSet<>();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //Load data
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

	/**
	 * Subscribes to the RxBus with a response to new events.
	 * It is important to to subscribe to the same event bus used to post events, 
	 * thus it is a singleton object.
	 */
    @Override
    public void onStart() {
        super.onStart();
        _rxBus.toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof TapEvent) {
                        //For a event from the category/measurement tab, populate the appropriate measurements/layers
                        if(((TapEvent) event).getTab() == ARG_MEASURE && mode == ARG_MEASURE) {
                            String cat = ((TapEvent) event).getCategory();
                            ArrayList<String> _measurelist = categories.get(cat);
                            ((DataAdapter) (mRecyclerView.getAdapter())).insertList(_measurelist);
                        } else if(((TapEvent) event).getTab() == ARG_LAYER && mode == ARG_LAYER) {
                            String m = ((TapEvent) event).getMeasurement();

                            //Because multiple measurements may be mapped to one layer we can't use
                            //a hash table to store the layers, search for each layer by comparing identifiers
                            ArrayList<String> id_list = measurements.get(m), _layerlist = new ArrayList<>();
                            for(String id: id_list) {
                                Layer temp = searchLayer(id);
                                _layerlist.add(temp != null ? temp.getTitle() : id);
                            }

                            //Update list entries and selection status
                            updateSet();
                            DataAdapter adapter = (DataAdapter) mRecyclerView.getAdapter();
                            adapter.insertList(_layerlist);
                            adapter.updateSelected(layerSet);
                        }
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
        DataAdapter l = new DataAdapter(_rxBus, mode);
        mRecyclerView.setAdapter(l);
        return rootView;
    }

    @Override
    public Loader<DataWrapper> onCreateLoader(int id, Bundle args) {
        return new LayerLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<DataWrapper> loader, DataWrapper data) {
        layers = data.layers;
        categories = data.cats;
        measurements = data.measures;

        populateLists();
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}

    //Populate lists from Loader data
    private void populateLists() {
        DataAdapter adapter = (DataAdapter) (mRecyclerView.getAdapter());
        switch (mode) {
            case ARG_LAYER:
                ArrayList<String> layer_list = new ArrayList<>();
                for (Layer l: layers)
                    layer_list.add(l.getTitle());
                adapter.insertList(layer_list);
                adapter.insertLayers(layers);
                updateSet();
                adapter.updateSelected(layerSet);
                break;

            case ARG_MEASURE:
                ArrayList<String> measure_list = new ArrayList<>();
                for (Map.Entry<String, ArrayList<String>> e : measurements.entrySet())
                    measure_list.add(e.getKey());
                adapter.insertList(measure_list);
                break;

            default: //Categories
                ArrayList<String> cat_list = new ArrayList<>();
                for (Map.Entry<String, ArrayList<String>> e : categories.entrySet())
                    cat_list.add(e.getKey());
                adapter.insertList(cat_list);
                break;
        }
    }

    //Given an identifier, find the layer it belongs to
    private Layer searchLayer(String id) {
        for(Layer l : layers)
            if(l.getIdentifier().equals(id))
                return l;

        return null;
    }

    private void updateSet() {
        layerSet.clear();
        for(Layer l : stack)
            layerSet.add(l.getTitle());
    }
}
