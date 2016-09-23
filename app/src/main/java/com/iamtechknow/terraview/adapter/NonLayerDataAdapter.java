package com.iamtechknow.terraview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import java.util.ArrayList;

import static com.iamtechknow.terraview.picker.LayerActivity.*;

/**
 * Item adapter for the category and measurement tab RecyclerViews
 * Uses an event bus implemented in RxJava to pass events to fragments when item is clicked
 */
public class NonLayerDataAdapter extends RecyclerView.Adapter<NonLayerDataAdapter.ViewHolder> {
    private ArrayList<String> mItems;
    private RxBus _rxBus;
    private final int mode; //Corresponds to its residing fragment

    /**
     * Set up an empty adapter
     */
    public NonLayerDataAdapter(RxBus bus, int _mode) {
        super();
        mode = _mode;
        _rxBus = bus;
        mItems = new ArrayList<>();
    }

    /**
     * View holder implementation for each list item
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            text = (TextView) itemView.findViewById(R.id.layer_text);
        }

        /**
         * When an item is tapped, its selection state will be toggled.
         * The selection state determines whether to insert or remove from the queue the layer from the position in the data set.
         * The underlying data is modified but not the UI, which will happen when notifyDataSetChanged is called.
         */
        @Override
        public void onClick(View v) {
            //Send event to RxBus based on the current mode (tab) of the fragment the item resides in
            switch(mode) {
                case 0: //Call from category tab
                    _rxBus.send(new TapEvent(SELECT_MEASURE_TAB, null, null, mItems.get(getAdapterPosition())));
                    break;

                default: //Call from measurement tab
                    _rxBus.send(new TapEvent(SELECT_LAYER_TAB, null, mItems.get(getAdapterPosition()), null));
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
}
