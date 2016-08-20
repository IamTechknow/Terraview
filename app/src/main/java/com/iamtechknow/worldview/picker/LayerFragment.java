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
import com.iamtechknow.worldview.util.Utils;

import java.util.ArrayList;
import java.util.HashSet;

public class LayerFragment extends Fragment implements LayerView {
    private LayerPresenter presenter;
    private RxBus _rxBus;
    private RecyclerView mRecyclerView;
    private ArrayList<Layer> layers, stack;

    //HashSet to keep track of selected elements from stack
    private HashSet<String> layerSet;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        presenter = new LayerPresenterImpl(this);
        stack = getArguments().getParcelableArrayList(LayerActivity.RESULT_STACK);
        layerSet = new HashSet<>();
        _rxBus = RxBus.getInstance();
    }

    //Inflate the fragment view and setup the RecyclerView
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_layer, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DataAdapter l = new DataAdapter(_rxBus, 2);
        mRecyclerView.setAdapter(l);
        return rootView;
    }

    @Override
    public void insertList(ArrayList<String> strings) {

    }

    @Override
    public void showInfo(String html) {
        Utils.showWebPage(getActivity(), html);
    }
}
