package com.iamtechknow.worldview.fragment;

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

import com.iamtechknow.worldview.LayerActivity;
import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.RxBus;
import com.iamtechknow.worldview.adapter.*;
import com.iamtechknow.worldview.model.DataWrapper;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import rx.functions.Action1;

public class LayerPageFragment extends Fragment implements LoaderManager.LoaderCallbacks<DataWrapper> {
    public static final int ARG_CAT = 0, ARG_MEASURE = 1, ARG_LAYER = 2;
    public static final String EXTRA_ARG = "arg";

    private RecyclerView mRecyclerView;
    private int mode;
    private RxBus _rxBus;

    //Worldview Data
    private ArrayList<Layer> layers;
    private Hashtable<String, ArrayList<String>> categories, measurements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mode = getArguments().getInt(EXTRA_ARG, ARG_CAT);
        _rxBus = ((LayerActivity) getActivity()).getRxBusSingleton();
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
                    if (event instanceof LayerPageFragment.TapEvent)
                        //In the fragment that is also listening load the right data!
                        switch (((LayerPageFragment.TapEvent) event).tab) {
                            case ARG_LAYER:
                                if(mode == ARG_LAYER) {
                                    ArrayList<String> layer_list = new ArrayList<>();
                                    for (Layer l: layers)
                                        layer_list.add(l.getTitle());
                                    ((DataAdapter) (mRecyclerView.getAdapter())).insertList(layer_list);
                                }
                                break;

                            case ARG_MEASURE:
                                if(mode == ARG_MEASURE) {
                                    ArrayList<String> measure_list = new ArrayList<>();
                                    for (Map.Entry<String, ArrayList<String>> e : measurements.entrySet())
                                        measure_list.add(e.getKey());
                                    ((DataAdapter) (mRecyclerView.getAdapter())).insertList(measure_list);
                                }
                                break;

                            default: //category, do nothing?

                                break;
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

        //Populate just categories list, populate others when tapped on
        if(mode == ARG_CAT) {
            ArrayList<String> cat_list = new ArrayList<>();
            for (Map.Entry<String, ArrayList<String>> e : categories.entrySet())
                cat_list.add(e.getKey());
            ((DataAdapter) (mRecyclerView.getAdapter())).insertList(cat_list);
        }
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}

    public static class TapEvent { //Container with event parameter
        public int tab;

        public TapEvent(int tab_number) {
            tab = tab_number;
        }
    }
}
