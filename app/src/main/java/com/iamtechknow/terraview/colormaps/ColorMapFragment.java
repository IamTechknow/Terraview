package com.iamtechknow.terraview.colormaps;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.ColorMapAdapter;

public class ColorMapFragment extends BottomSheetDialogFragment {
    public static final String COLORMAP_ARG = "layers";

    private ColorMapAdapter adapter;

    /**
     * Setup the bottom sheet behaviour and pass the current layer data to the list
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.color_map_frag, container);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.color_map_rv);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if(getArguments() != null) {
            adapter = new ColorMapAdapter(getArguments().getParcelableArrayList(COLORMAP_ARG),
                    ViewModelProviders.of(this, new ColorMapViewModelFactory()).get(ColorMapViewModel.class));
            mRecyclerView.setAdapter(adapter);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.startSubs();
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.cancelSubs();
    }
}
