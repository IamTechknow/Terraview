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
import com.iamtechknow.terraview.model.Measurement;

import java.util.ArrayList;
import java.util.List;

public class NonLayerFragment extends Fragment implements NonLayerView {
    public static final String EXTRA_ARG = "arg", CAT_EXTRA = "category";
    private boolean isCategoryTab;

    private NonLayerPresenter presenter;
    private NonLayerDataAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = new NonLayerPresenterImpl(RxBus.getInstance(), Injection.provideLocalSource(getLoaderManager(), getActivity()));
        presenter.attachView(this);
        isCategoryTab = getArguments().getBoolean(EXTRA_ARG);

        if(savedInstanceState != null && !isCategoryTab)
            presenter.setCategory(savedInstanceState.getString(CAT_EXTRA));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CAT_EXTRA, presenter.getCategory());
    }

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
        adapter = new NonLayerDataAdapter(presenter);
        mRecyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public boolean isCategoryTab() {
        return isCategoryTab;
    }

    //Called when presenter has finished loading data, set up lists
    @Override
    public void insertList(List<String> list) {
        adapter.insertList(list);
    }

    //Called after config change or when category was tapped, only called by measurement tab presenter
    @Override
    public void insertMeasurements(List<Measurement> list) {
        ArrayList<String> measurements = new ArrayList<>();
        for(Measurement m : list)
            measurements.add(m.getName());
        adapter.insertList(measurements);
    }
}
