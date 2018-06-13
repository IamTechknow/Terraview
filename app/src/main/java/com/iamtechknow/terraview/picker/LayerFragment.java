package com.iamtechknow.terraview.picker;

import android.arch.lifecycle.ViewModelProviders;
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
import java.util.List;

public class LayerFragment extends Fragment implements LayerContract.View {
    private LayerContract.Presenter presenter;
    private LayerDataAdapter adapter;
    private PickerViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        viewModel = ViewModelProviders.of(this).get(PickerViewModel.class);
        ArrayList<Layer> stack = getArguments().getParcelableArrayList(LayerActivity.RESULT_STACK),
                delete = getArguments().getParcelableArrayList(LayerActivity.DELETE_STACK);
        presenter = new LayerPresenterImpl(this, RxBus.getInstance(), stack, delete, Injection.provideLocalSource(getLoaderManager(), getActivity()), new SparseBooleanArray(), viewModel.getMeasurement());
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
        adapter.clearPresenter();
        presenter = null;
    }

    //Inflate the fragment view and setup the RecyclerView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

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
    public void populateList(List<Layer> list) {
        adapter.insertList(list);
    }

    @Override
    public void updateLayerList(String measurement, List<Layer> list) {
        viewModel.setMeasurement(measurement);
        adapter.insertList(list);
    }

    @Override
    public void showInfo(String html) {
        Utils.showWebPage(getActivity(), html, getString(R.string.about));
    }
}
