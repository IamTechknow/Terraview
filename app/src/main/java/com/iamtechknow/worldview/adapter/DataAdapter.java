package com.iamtechknow.worldview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.worldview.R;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<LayerAdapter.ViewHolder> {
    private ArrayList<String> mItems;
    private LayerAdapter.ItemOnClickListener mListener;

    /**
     * Setup an empty adapter
     */
    public DataAdapter() {
        super();
        mItems = new ArrayList<>();
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
                mListener.onClick(holder.getAdapterPosition(), false);
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

    public void setItemListener(LayerAdapter.ItemOnClickListener mListener) {
        this.mListener = mListener;
    }
}
