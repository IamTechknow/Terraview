package com.iamtechknow.terraview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.colormaps.ColorMapViewImpl;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class ColorMapAdapter extends RecyclerView.Adapter<ColorMapAdapter.ViewHolder> {
    private ArrayList<Layer> mItems;

    public ColorMapAdapter(ArrayList<Layer> list) {
        mItems = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView text, none, start, end;
        ColorMapViewImpl canvas;

        ViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.color_map_id);
            none = itemView.findViewById(R.id.color_map_none);
            start = itemView.findViewById(R.id.start_date);
            end = itemView.findViewById(R.id.end_date);
            canvas = itemView.findViewById(R.id.color_map_palette);
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
        if(l.hasColorMap()) {
            holder.itemView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            holder.canvas.setLayerId(l.getPalette());
        } else {
            holder.canvas.setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.color_map_info).setVisibility(View.GONE);
            holder.none.setVisibility(View.VISIBLE);
        }

        if(l.hasNoDates())
            holder.itemView.findViewById(R.id.dateContainer).setVisibility(View.GONE);
        else {
            if (l.getStartDateRaw() != null) {
                String start_str = Utils.parseDateForDialog(Utils.parseISODate(l.getStartDateRaw()));
                start_str = start_str.substring(start_str.indexOf(',') + 2);
                holder.start.setText(String.format(Locale.US, "Starts: %s", start_str));
            }

            if (l.getEndDateRaw() != null) {
                String end_str = Utils.parseDateForDialog(Utils.parseISODate(l.getEndDateRaw()));
                end_str = end_str.substring(end_str.indexOf(',') + 2);
                holder.end.setText(String.format(Locale.US, "Ends: %s", end_str));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
