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
import com.iamtechknow.terraview.adapter.NonLayerDataAdapter;
import com.iamtechknow.terraview.model.TapEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import rx.functions.Action1;

import static com.iamtechknow.terraview.picker.LayerActivity.SELECT_MEASURE_TAB;

public class NonLayerFragment extends Fragment implements NonLayerView {
    public static final String EXTRA_ARG = "arg", CAT_EXTRA = "category";
    private boolean isCategoryTab;

    private NonLayerPresenter presenter;
    private RxBus _rxBus;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        presenter = new NonLayerPresenterImpl(this, Injection.provideLocalSource(getLoaderManager(), getActivity()));
        _rxBus = RxBus.getInstance();
        isCategoryTab = getArguments().getBoolean(EXTRA_ARG);

        if(savedInstanceState != null && !isCategoryTab)
            presenter.setCategory(savedInstanceState.getString(CAT_EXTRA));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CAT_EXTRA, presenter.getCategory());
    }

    /**
     * Set up the event bus for the measurement tab to response to taps on a category
     * to load all measurements in the category
     */
    @Override
    public void onStart() {
        super.onStart();
        presenter.getData();

        if(!isCategoryTab)
            _rxBus.toObserverable()
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof TapEvent && ((TapEvent) event).getTab() == SELECT_MEASURE_TAB) //call from Category tab
                            insertMeasurements(((TapEvent) event).getCategory());
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
    //The measurement tab needs to determine whether to display the previous measurement due to config change
    @Override
    public void insertList() {
        if(!isCategoryTab && presenter.getCategory() != null)
            insertMeasurements(presenter.getCategory());
        else {
            ArrayList<String> result = new ArrayList<>();
            TreeMap<String, ArrayList<String>> map = presenter.getMap(isCategoryTab);
            NonLayerDataAdapter adapter = (NonLayerDataAdapter) (mRecyclerView.getAdapter());

            for (Map.Entry<String, ArrayList<String>> e : map.entrySet())
                result.add(e.getKey());
            adapter.insertList(result);
        }
    }

    private void insertMeasurements(String cat) {
        ArrayList<String> measure_list = presenter.getMeasurementList(cat);
        ((NonLayerDataAdapter) (mRecyclerView.getAdapter())).insertList(measure_list);
    }
}
