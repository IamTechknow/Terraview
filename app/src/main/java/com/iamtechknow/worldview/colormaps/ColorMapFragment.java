package com.iamtechknow.worldview.colormaps;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.adapter.ColorMapAdapter;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;

public class ColorMapFragment extends BottomSheetDialogFragment{
    public static final String COLORMAP_ARG = "layers";

    private RecyclerView mRecyclerView;
    private ColorMapAdapter adapter;

    /**
     * Behaviour for the bottom sheet, just dismiss it when swiped down
     */
    private BottomSheetBehavior.BottomSheetCallback mCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if(newState == BottomSheetBehavior.STATE_HIDDEN)
                dismiss();
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    /**
     * Setup the bottom sheet behaviour and pass the current layer data to the list
     */
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.color_map_frag, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior)
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mCallback);
        ArrayList<Layer> colorMapLayers = getArguments().getParcelableArrayList(COLORMAP_ARG);

        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.color_map_rv);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ColorMapAdapter(colorMapLayers);
        mRecyclerView.setAdapter(adapter);
    }
}
