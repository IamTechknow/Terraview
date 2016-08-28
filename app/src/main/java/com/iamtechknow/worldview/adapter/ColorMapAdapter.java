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
import java.util.Locale;

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

    /**
     * Upon binding, start XML parsing and draw the colormap
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Layer l = mItems.get(position);
        if(l.hasColorMap()) {
            holder.text.setText(l.getTitle());
            holder.canvas.setLayerId(l.getIdentifier());
        } else
            holder.text.setText(String.format(Locale.US, "%s%s", l.getTitle(), " - no color map available"));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
