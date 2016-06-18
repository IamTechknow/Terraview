package com.iamtechknow.worldview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
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

    /**
     * View holder implementation for each list item
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text;
        boolean isSelected;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            text = (TextView) itemView.findViewById(R.id.layer_text);
        }

        @Override
        public void onClick(View v) {
            isSelected = !isSelected;
            itemView.setSelected(isSelected);

            //Send event to RxBus based on the current mode (tab) of the fragment the item resides in
            switch(mode) {
                case ARG_CAT:
                    _rxBus.send(new TapEvent(MEASURE_TAB, null, null, mItems.get(getAdapterPosition())));
                    break;

                case ARG_LAYER:
                    int queue = isSelected ? LAYER_QUEUE : LAYER_DEQUE;
                    Layer temp = searchLayer(text.getText().toString());

                    if(temp != null)
                        _rxBus.send(new TapEvent(queue, temp, null, null));
                    break;

                default: //Measurement
                    _rxBus.send(new TapEvent(LAYER_TAB, null, mItems.get(getAdapterPosition()), null));
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layer_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(mItems.get(position));
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

    //Given a title, find the layer it belongs to
    //Will never return null if a title exists
    private Layer searchLayer(String title) {
        for(Layer l : mLayers)
            if(l.getTitle().equals(title))
                return l;

        return null;
    }
}
