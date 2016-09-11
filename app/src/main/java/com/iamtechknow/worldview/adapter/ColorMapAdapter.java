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
        TextView text, none;
        ColorMapViewImpl canvas;

        public ViewHolder(View itemView) {
            super(itemView);

            text = (TextView) itemView.findViewById(R.id.color_map_id);
            none = (TextView) itemView.findViewById(R.id.color_map_none);
            canvas = (ColorMapViewImpl) itemView.findViewById(R.id.color_map_palette);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_map_item, parent, false);
        return new ColorMapAdapter.ViewHolder(v);
    }

    /**
     * Upon binding, start XML parsing and draw the colormap, or set no color map text
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Layer l = mItems.get(position);
        holder.text.setText(l.getTitle());
        if(l.hasColorMap())
            holder.canvas.setLayerId(l.getIdentifier());
        else {
            holder.canvas.setVisibility(View.GONE);
            holder.none.setVisibility(View.VISIBLE);
            holder.none.setText(R.string.colormap_none);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
