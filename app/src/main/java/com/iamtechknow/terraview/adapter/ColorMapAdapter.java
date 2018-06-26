package com.iamtechknow.terraview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.colormaps.ColorMapViewImpl;
import com.iamtechknow.terraview.colormaps.ColorMapViewModel;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.disposables.Disposable;

public class ColorMapAdapter extends RecyclerView.Adapter<ColorMapAdapter.ViewHolder> {
    private ArrayList<Layer> mItems;

    private ColorMapViewModel viewModel;

    private Disposable dataSub;

    public ColorMapAdapter(ArrayList<Layer> list, ColorMapViewModel model) {
        mItems = list;
        viewModel = model;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
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
     * Upon binding, get the colormap, or set no color map text, then set dates
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Layer l = mItems.get(position);
        holder.text.setText(l.getTitle());
        if(l.hasColorMap()) {
            if(viewModel.getColorMap(l.getPalette()) != null) {
                holder.canvas.setVisibility(View.VISIBLE);
                holder.canvas.setColorMapData(viewModel.getColorMap(l.getPalette()));
            } else {
                viewModel.loadColorMap(holder.getAdapterPosition(), l.getPalette());
                holder.itemView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            }
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

    public void startSubs() {
        dataSub = viewModel.getLiveData().subscribe(this::onNewData);
    }

    public void cancelSubs() {
        dataSub.dispose();
        viewModel.cancelSub();
    }

    //Notify the adapter which item to update to show ColorMap
    private void onNewData(int position) {
        notifyItemChanged(position);
    }
}
