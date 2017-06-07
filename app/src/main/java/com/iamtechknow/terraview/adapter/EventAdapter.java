package com.iamtechknow.terraview.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.events.EventPresenter;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private ArrayList<Event> items;
    private EventPresenter presenter;

    public EventAdapter(EventPresenter p) {
        super();
        presenter = p;
        items = new ArrayList<>();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text, sub;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            text = (TextView) itemView.findViewById(R.id.item_text);
            sub = (TextView) itemView.findViewById(R.id.item_sub);
            icon = (ImageView) itemView.findViewById(R.id.item_info);
        }

        @Override
        public void onClick(View v) {
            presenter.presentEvent(items.get(getAdapterPosition()));
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
        holder.icon.setOnClickListener(v -> presenter.presentSource(items.get(position).getSource()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void insertList(ArrayList<Event> list) {
        items = list;
        notifyDataSetChanged();
    }

    public void clearList() {
        items.clear();
        notifyDataSetChanged();
    }

    public void clearPresenter() {
        presenter = null;
    }
}
