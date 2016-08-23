package com.iamtechknow.worldview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.colormaps.ColorMapViewImpl;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;

public class ColorMapAdapter extends RecyclerView.Adapter<ColorMapAdapter.ViewHolder> {
    private ArrayList<Layer> mItems;

    public ColorMapAdapter(ArrayList<Layer> list) {
        mItems = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView text;
        ColorMapViewImpl canvas;

        public ViewHolder(View itemView) {
            super(itemView);

            text = (TextView) itemView.findViewById(R.id.color_map_id);
            canvas = (ColorMapViewImpl) itemView.findViewById(R.id.color_map_palette);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_map_item, parent, false);
        return new ColorMapAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(mItems.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
