package com.iamtechknow.terraview.picker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.terraview.Injection;
import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.LayerDataAdapter;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

public class LayerFragment extends Fragment implements LayerView {
    private static final String MEAS_EXTRA = "measurement";

    private LayerPresenter presenter;
    private LayerDataAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        ArrayList<Layer> stack = getArguments().getParcelableArrayList(LayerActivity.RESULT_STACK);
        presenter = new LayerPresenterImpl(RxBus.getInstance(), stack, Injection.provideLocalSource(getLoaderManager(), getActivity()), new SparseBooleanArray());
        presenter.attachView(this);

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

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new LayerDataAdapter(presenter);
        mRecyclerView.setAdapter(adapter);
        return rootView;
    }

    /**
     * Called when data is loaded to make the view populate the RecyclerView
     * @param list List of titles from all available layers
     */
    @Override
    public void populateList(ArrayList<String> list) {
        adapter.insertList(list);
    }

    @Override
    public void updateLayerList(ArrayList<String> list) {
        adapter.insertList(list);
    }

    @Override
    public void showInfo(String html) {
        Utils.showWebPage(getActivity(), html);
    }
}
