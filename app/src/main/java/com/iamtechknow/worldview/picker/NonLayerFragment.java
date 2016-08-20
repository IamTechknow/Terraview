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
import com.iamtechknow.worldview.adapter.DataAdapter;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;

public class NonLayerFragment extends Fragment implements NonLayerView {
    private boolean isCategoryTab;

    private NonLayerPresenter presenter;
    private RxBus _rxBus;
    private RecyclerView mRecyclerView;
    private ArrayList<Layer> layers, stack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        _rxBus = RxBus.getInstance();
        presenter = new NonLayerPresenterImpl(this);
    }

    //Inflate the fragment view and setup the RecyclerView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_layer, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DataAdapter l = new DataAdapter(_rxBus, isCategoryTab ? 0 : 1 );
        mRecyclerView.setAdapter(l);
        return rootView;
    }

    @Override
    public void insertList(ArrayList<String> strings) {

    }
}
