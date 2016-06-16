package com.iamtechknow.worldview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.RxBus;
import com.iamtechknow.worldview.model.Layer;

import static com.iamtechknow.worldview.fragment.LayerPageFragment.*;
import static com.iamtechknow.worldview.LayerActivity.*;

import java.util.ArrayList;

/**
 * Item adapter instance used for each layer picker fragment in LayerActivity's view pager
 * Uses an event bus implemented in RxJava to pass events to fragments when item is clicked
 */
public class DataAdapter extends RecyclerView.Adapter<LayerAdapter.ViewHolder> {
    private ArrayList<String> mItems;
    private ArrayList<Layer> mLayers;
    private RxBus _rxBus;
    private final int mode;

    /**
     * Set up an empty adapter
     */
    public DataAdapter(RxBus bus, int _mode) {
        super();
        mItems = new ArrayList<>();
        _rxBus = bus;
        mode = _mode;
    }

    @Override
    public LayerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layer_item, parent, false);
        return new LayerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final LayerAdapter.ViewHolder holder, int position) {
        holder.layer.setText(mItems.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.isSelected = !holder.isSelected;
                holder.itemView.setSelected(holder.isSelected);

                //Send event to RxBus based on the current mode (tab) of the fragment the item resides in
                switch(mode) {
                    case ARG_CAT:
                        _rxBus.send(new TapEvent(MEASURE_TAB, null, null, mItems.get(holder.getAdapterPosition())));
                        break;

                    case ARG_LAYER:
                        int queue = holder.isSelected ? LAYER_QUEUE : LAYER_DEQUE;
                        _rxBus.send(new TapEvent(queue, mLayers.get(holder.getAdapterPosition()), null, null));
                        break;

                    default: //Measurement
                        _rxBus.send(new TapEvent(LAYER_TAB, null, mItems.get(holder.getAdapterPosition()), null));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void insertList(ArrayList<String> strings) {
        mItems = strings;
        notifyDataSetChanged();
    }

    public void insertLayers(ArrayList<Layer> layers) {
        mLayers = layers;
    }
}
