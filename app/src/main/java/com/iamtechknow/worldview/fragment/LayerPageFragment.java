package com.iamtechknow.worldview.fragment;

import android.content.Intent;
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

import com.iamtechknow.worldview.LayerActivity;
import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.adapter.*;
import com.iamtechknow.worldview.model.DataWrapper;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class LayerPageFragment extends Fragment implements LoaderManager.LoaderCallbacks<DataWrapper> {
    public static final int ARG_CAT = 0, ARG_MEASURE = 1, ARG_LAYER = 2;
    public static final String EXTRA_ARG = "arg";

    private RecyclerView mRecyclerView;
    private int mode;

    //Worldview Data
    private ArrayList<Layer> layers;
    private Hashtable<String, ArrayList<String>> categories, measurements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mode = getArguments().getInt(EXTRA_ARG, ARG_CAT);
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
        DataAdapter l = new DataAdapter();
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
        switch (mode) {
            case ARG_CAT: //Simple, get names of keys then display all categories
                ArrayList<String> cat_list = new ArrayList<>();
                for(Map.Entry<String, ArrayList<String>> e : categories.entrySet())
                    cat_list.add(e.getKey());
                ((DataAdapter) (mRecyclerView.getAdapter())).insertList(cat_list);
                break;
            case ARG_MEASURE:

                break;
            default:

        }
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}

    LayerAdapter.ItemOnClickListener mItemListener = new LayerAdapter.ItemOnClickListener() {
        @Override
        public void onClick(int idx, boolean checked) {
            Intent i = new Intent(getActivity(), LayerActivity.class);
            switch(mode) {
                case ARG_CAT:
                    i.setAction(LayerActivity.ACTION_SWITCH_MEASURE);
                    break;
                case ARG_MEASURE:
                    i.setAction(LayerActivity.ACTION_SWITCH_CATEGORY);
                    break;
                default:
            }
            startActivity(i);
        }
    };
}
