package com.iamtechknow.terraview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.events.CategoryContract;
import com.iamtechknow.terraview.model.EventCategory;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<EventCategory> items;
    private CategoryContract.Presenter presenter;

    public CategoryAdapter(CategoryContract.Presenter p) {
        super();
        presenter = p;
        items = new ArrayList<>();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text;

        ViewHolder(View itemView) {
            super(itemView);


            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            text = itemView.findViewById(R.id.item_text);
        }

        @Override
        public void onClick(View v) {
            presenter.emitEvent(items.get(getAdapterPosition()).getId());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(items.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void insertList(ArrayList<EventCategory> list) {
        items = list;
        notifyDataSetChanged();
    }

    public void clearPresenter() {
        presenter = null;
    }
}
