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
import com.iamtechknow.terraview.adapter.NonLayerDataAdapter;
import com.iamtechknow.terraview.model.Measurement;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class NonLayerFragment extends Fragment {
    public static final String EXTRA_ARG = "arg";
    private boolean isCategoryTab;

    private NonLayerDataAdapter adapter;
    private NonLayerViewModel viewModel;
    private Disposable dataSub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCategoryTab = getArguments().getBoolean(EXTRA_ARG);
        viewModel = ViewModelProviders.of(this, new PickerViewModelFactory(Injection.provideLocalSource(getContext()), isCategoryTab))
            .get(NonLayerViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.startSubs();
        dataSub = isCategoryTab ? viewModel.getLiveCategories().subscribe(this::insertCategories) :
            viewModel.getLiveMeasures().subscribe(this::insertMeasurements);

        viewModel.getData();
    }

    @Override
    public void onPause() {
        super.onPause();
        dataSub.dispose();
        viewModel.cancelSubs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isCategoryTab) //destroy the loader for the category fragment so it can reload
            getLoaderManager().destroyLoader(0);
    }

    //Inflate the fragment view and setup the RecyclerView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new NonLayerDataAdapter(viewModel);
        mRecyclerView.setAdapter(adapter);
        return rootView;
    }

    //Extract strings out of list before displaying it
    private void insertMeasurements(List<Measurement> list) {
        ArrayList<String> result = new ArrayList<>();
        for (Measurement m : list)
            result.add(m.getName());
        adapter.insertList(result);
    }

    private void insertCategories(List<String> list) {
        adapter.insertList(list);
    }
}
