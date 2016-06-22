package com.iamtechknow.worldview.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
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
    private SparseBooleanArray mSelectedPositions;
    private RxBus _rxBus;
    private final int mode; //Corresponds to its residing fragment

    /**
     * Set up an empty adapter
     */
    public DataAdapter(RxBus bus, int _mode) {
        super();
        mode = _mode;
        _rxBus = bus;
        mItems = new ArrayList<>();

        if(mode == ARG_LAYER)
            mSelectedPositions = new SparseBooleanArray();
    }

    /**
     * View holder implementation for each list item
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text, subtitle;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            text = (TextView) itemView.findViewById(R.id.layer_text);
            subtitle = (TextView) itemView.findViewById(R.id.layer_sub); //can be null for other tabs
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
                case ARG_CAT:
                    _rxBus.send(new TapEvent(MEASURE_TAB, null, null, mItems.get(getAdapterPosition())));
                    break;

                //Selection states only apply to the Layer tab
                case ARG_LAYER:
                    boolean prevState = isItemChecked(getAdapterPosition()); //selected when tapped?
                    setItemChecked(getLayoutPosition(), !prevState);
                    int queue = !prevState ? LAYER_QUEUE : LAYER_DEQUE;
                    Layer temp = searchLayer(text.getText().toString());

                    if(temp != null)
                        _rxBus.send(new TapEvent(queue, temp, null, null));
                    notifyDataSetChanged();
                    break;

                default: //Measurement
                    _rxBus.send(new TapEvent(LAYER_TAB, null, mItems.get(getAdapterPosition()), null));
            }

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = mode == ARG_LAYER ? R.layout.layer_item_with_sub : R.layout.layer_item ;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Called to update each visible ViewHolder when notifyDataSetChanged() is invoked.
     * The text is updated when the view holder is first binded, and the selection state is always updated. <br />
     * To avoid undefined behavior (items being selected when tapped), the UI is only updated here,
     * because it is possible for view holders to be selected when not done so, thus
     * its selection state is always refreshed with the underlying data.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(mItems.get(position));
        if(holder.subtitle != null) { //if item in layer tab, show subtitle
            Layer l = searchLayer(mItems.get(position));
            if(l != null)
                holder.subtitle.setText(l.getSubtitle());
        }

        if(mode == ARG_LAYER)
            holder.itemView.setActivated(isItemChecked(position));
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

    /**
     * Given the current stack of layers, shown them as being selected in the list
     * @param stack The layer objects of the tile overlays that would be shown on the map
     */
    public void updateSelected(ArrayList<Layer> stack) {
        if(mode == ARG_LAYER)
            for(Layer l : stack) {
                int pos = searchLayer(l);
                if(pos != -1)
                    setItemChecked(pos, true);
            }
    }

    //Given a title, find the layer it belongs to
    //Will never return null if a title exists
    private Layer searchLayer(String title) {
        for(Layer l : mLayers)
            if(l.getTitle().equals(title))
                return l;

        return null;
    }

    //Given a title, find a layer that has the same title
    //If there is such a layer return its index
    //TODO: Sort the list, use binary search
    private int searchLayer(Layer arg) {
        int size = mLayers.size();
        for(int i = 0; i < size; i++)
            if(mLayers.get(i).getTitle().equals(arg.getTitle()))
                return i;
        return -1;
    }

    /**
     * Update the underlying data set for items selected.
     * Also may be used by the fragment to indicate what layers are already selected when
     * received from the LayerActivity.
     * @param position The position of a given ViewHolder
     * @param isSelected Whether or not the item is selected
     */
    private void setItemChecked(int position, boolean isSelected) {
        mSelectedPositions.put(position, isSelected);
    }

    private boolean isItemChecked(int position) {
        return mSelectedPositions.get(position);
    }
}
