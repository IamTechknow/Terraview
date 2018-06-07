package com.iamtechknow.terraview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.picker.LayerPresenter;

import java.util.List;

public class LayerDataAdapter extends RecyclerView.Adapter<LayerDataAdapter.ViewHolder> {
    private LayerPresenter presenter;
    private List<Layer> mItems;

    /**
     * Set up an empty adapter
     */
    public LayerDataAdapter(LayerPresenter _presenter) {
        super();
        presenter = _presenter;
    }

    /**
     * View holder implementation for each list item
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text, subtitle;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            text = (TextView) itemView.findViewById(R.id.item_text);
            subtitle = (TextView) itemView.findViewById(R.id.item_sub);
            icon = (ImageView) itemView.findViewById(R.id.item_info);
        }

        /**
         * When an item is tapped, its selection state will be toggled.
         * The selection state determines whether to insert or remove from the queue the layer from the position in the data set.
         * The underlying data is modified but not the UI, which will happen when notifyDataSetChanged is called.
         */
        @Override
        public void onClick(View v) {
            boolean prevState = presenter.isItemChecked(getAdapterPosition()); //selected when tapped?
            presenter.setItemChecked(getLayoutPosition(), !prevState);

            Layer temp = presenter.searchLayerByTitle(text.getText().toString());
            if(temp != null)
                presenter.changeStack(temp, !prevState);

            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_with_sub, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Called to update each visible ViewHolder when notifyDataSetChanged() is invoked.
     * The text is updated when the view holder is first binded, and the selection state is always updated. <br />
     * To avoid undefined behavior (items being selected when tapped), the UI is only updated here,
     * because it is possible for view holders to be selected when not done so, thus
     * its selection state is always refreshed with the underlying data.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(mItems.get(position).getTitle());

        Layer l = mItems.get(position);
        if(l != null) {
            holder.subtitle.setText(l.getSubtitle());
            holder.icon.setOnClickListener(v -> presenter.useRetrofit(l.getDescription()));
        }

        holder.itemView.setActivated(presenter.isItemChecked(position));
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }

    public void insertList(List<Layer> strings) {
        mItems = strings;
        notifyDataSetChanged();
    }

    public void clearPresenter() {
        presenter = null;
    }
}
