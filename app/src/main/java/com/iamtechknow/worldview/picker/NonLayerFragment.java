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
import com.iamtechknow.worldview.adapter.NonLayerDataAdapter;
import com.iamtechknow.worldview.model.TapEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import rx.functions.Action1;

import static com.iamtechknow.worldview.picker.LayerActivity.SELECT_MEASURE_TAB;

public class NonLayerFragment extends Fragment implements NonLayerView {
    public static final String EXTRA_ARG = "arg";
    private boolean isCategoryTab;

    private NonLayerPresenter presenter;
    private RxBus _rxBus;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        presenter = new NonLayerPresenterImpl(this); //Should work, b/c seperate instance for frags
        _rxBus = RxBus.getInstance();
        isCategoryTab = getArguments().getBoolean(EXTRA_ARG);
    }

    /**
     * Set up the event bus for the measurement tab to response to taps on a category
     * to load all measurements in the category
     */
    @Override
    public void onStart() {
        super.onStart();
        presenter.getData(getLoaderManager(), getActivity());

        _rxBus.toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof TapEvent && ((TapEvent) event).getTab() == SELECT_MEASURE_TAB) { //call from Category tab
                        ArrayList<String> _measurelist = presenter.getMeasurementList(((TapEvent) event).getCategory());
                        ((NonLayerDataAdapter) (mRecyclerView.getAdapter())).insertList(_measurelist);
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
        NonLayerDataAdapter l = new NonLayerDataAdapter(RxBus.getInstance(), isCategoryTab ? 0 : 1);
        mRecyclerView.setAdapter(l);
        return rootView;
    }

    //Called when presenter has finished loading data, set up lists
    @Override
    public void insertList() {
        ArrayList<String> result = new ArrayList<>();
        TreeMap<String, ArrayList<String>> map = presenter.getMap(isCategoryTab);
        NonLayerDataAdapter adapter = (NonLayerDataAdapter) (mRecyclerView.getAdapter());

        for (Map.Entry<String, ArrayList<String>> e : map.entrySet())
            result.add(e.getKey());
        adapter.insertList(result);
    }
}
