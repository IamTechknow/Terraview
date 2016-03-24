package com.iamtechknow.worldview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;

public class LayerAdapter extends RecyclerView.Adapter<LayerAdapter.ViewHolder> {
    private ArrayList<Layer> mItems;
    private ItemOnClickListener mListener;

    /**
     * Interface method to allow the MainActivity to access the item's coordinates to start routing
     */
    public interface ItemOnClickListener {
        void onClick(int idx, boolean checked);
    }

    public void setItemListener(ItemOnClickListener mListener) {
        this.mListener = mListener;
    }

    /**
     * Setup the adapter with a list of items
     * @param items The list
     */
    public LayerAdapter(ArrayList<Layer> items) {
        super();
        mItems = items;
    }

    /**
     * Setup an empty adapter
     */
    public LayerAdapter() {
        super();
        mItems = new ArrayList<>();
    }

    /**
     * View holder implementation for each list item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView layer;

        public ViewHolder(View itemView) {
            super(itemView);

            layer = (TextView) itemView.findViewById(R.id.layer_text);
        }
    }

    @Override
    public LayerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layer_item, parent, false);
        return new ViewHolder(v);
    }

    //Set the checkbox and title for each list item
    @Override
    public void onBindViewHolder(final LayerAdapter.ViewHolder holder, int position) {
        final Layer l = mItems.get(position);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                l.setDisplaying(!l.isDisplaying());
                mListener.onClick(holder.getAdapterPosition(), l.isDisplaying());
            }
        };

        holder.layer.setText(l.getTitle());

        holder.itemView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void insertList(ArrayList<Layer> layers) {
        mItems = layers;
        notifyDataSetChanged();
    }
}
