package com.iamtechknow.terraview.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.events.EventViewModel;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private ArrayList<Event> items;
    private EventViewModel viewModel;

    public EventAdapter(EventViewModel v) {
        super();
        viewModel = v;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text, sub;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            text = itemView.findViewById(R.id.item_text);
            sub = itemView.findViewById(R.id.item_sub);
            icon = itemView.findViewById(R.id.item_info);
        }

        @Override
        public void onClick(View v) {
            viewModel.presentEvent(items.get(getAdapterPosition()));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_with_sub, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Resources strResource = holder.itemView.getResources();
        String closedInfo = items.get(position).isOngoing()
            ? strResource.getString(R.string.ongoing) : Utils.parseDateForDialog(Utils.parseISODate(items.get(position).getClosedDate()));

        holder.text.setText(items.get(position).getTitle());
        holder.sub.setText(strResource.getString(R.string.closed, closedInfo));
        holder.icon.setOnClickListener(v -> {
            if(viewModel.isSourceValid(items.get(position).getSource()))
                showSource(holder.itemView.getContext(), items.get(position).getSource());
            else
                warnNoSource(holder.itemView);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void insertList(ArrayList<Event> list) {
        items = list;
        notifyDataSetChanged();
    }

    public void clearList() {
        if(items != null) {
            items.clear();
            notifyDataSetChanged();
        }
    }

    private void showSource(Context c, String url) {
        c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void warnNoSource(View v) {
        Snackbar.make(v, R.string.no_source, Snackbar.LENGTH_SHORT).show();
    }
}
