package com.iamtechknow.terraview.picker;

import android.arch.lifecycle.ViewModelProviders;
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
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class LayerFragment extends Fragment {
    private LayerDataAdapter adapter;
    private LayerViewModel viewModel;

    private Disposable dataSub, metadataSub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        ArrayList<Layer> stack = getArguments().getParcelableArrayList(LayerActivity.RESULT_STACK),
                delete = getArguments().getParcelableArrayList(LayerActivity.DELETE_STACK);
        viewModel = ViewModelProviders.of(this, new PickerViewModelFactory(Injection.provideLocalSource(getLoaderManager(), getContext()), stack, delete))
            .get(LayerViewModel.class);
    }

    /**
     * Set up the event bus for the layer tab to response to taps on a measurement
     * to load all layers that are part of that measurement.
     */
    @Override
    public void onResume() {
        super.onResume();
        dataSub = viewModel.getLiveData().subscribe(this::populateList);
        metadataSub = viewModel.getMetaLiveData().subscribe(this::showInfo);
        viewModel.startSubs();
        if(viewModel.getCurrData() != null)
            populateList(viewModel.getCurrData());
        else
            viewModel.getData();
    }

    @Override
    public void onPause() {
        super.onPause();
        dataSub.dispose();
        metadataSub.dispose();
        viewModel.cancelSubs();
    }

    //Inflate the fragment view and setup the RecyclerView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new LayerDataAdapter(viewModel);
        mRecyclerView.setAdapter(adapter);
        return rootView;
    }

    /**
     * Called when data is loaded to make the view populate the RecyclerView
     * @param list List of titles from all available layers
     */
    public void populateList(List<Layer> list) {
        adapter.insertList(list);
    }

    public void showInfo(String html) {
        Utils.showWebPage(getActivity(), html, getString(R.string.about));
    }
}
