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

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.adapter.LayerAdapter;
import com.iamtechknow.worldview.model.DataWrapper;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;

import java.util.ArrayList;
import java.util.Hashtable;

public class LayerPageFragment extends Fragment implements LoaderManager.LoaderCallbacks<DataWrapper> {

    private RecyclerView mRecyclerView;

    //Worldview Data
    private ArrayList<Layer> layers;
    private Hashtable<String, ArrayList<String>> categories, measurements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //Load data
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // The last two arguments ensure LayoutParams are inflated properly.
        View rootView = inflater.inflate(R.layout.fragment_layer, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        LayerAdapter l = new LayerAdapter();
        l.setItemListener(mItemListener);
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

        //TODO populate lists
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}

    LayerAdapter.ItemOnClickListener mItemListener = new LayerAdapter.ItemOnClickListener() {
        @Override
        public void onClick(int idx, boolean checked) {
            //TODO
        }
    };
}
