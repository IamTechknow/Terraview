package com.iamtechknow.terraview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;
import java.util.Collections;

public class CurrLayerAdapter extends RecyclerView.Adapter<CurrLayerAdapter.CurrViewHolder>
        implements ItemTouchHelperAdapter {

    //Data set containing the current layers to be shown
    private ArrayList<Layer> mLayers;

    //Callbacks for ItemTouchHelper actions
    private final DragAndHideListener mDragListener;

    public CurrLayerAdapter(DragAndHideListener listener) {
        super();
        mLayers = new ArrayList<>();
        mDragListener = listener;
    }

    public class CurrViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, View.OnClickListener {
        TextView tv;
        ImageView drag_handle, vis;
        int visibility;

        public CurrViewHolder(View itemView) {
            super(itemView);

            tv = itemView.findViewById(R.id.curr_layer_text);
            drag_handle = itemView.findViewById(R.id.curr_layer_drag_handle);
            vis = itemView.findViewById(R.id.curr_layer_visibility);

            itemView.setClickable(true);
            drag_handle.setOnTouchListener(this);
            vis.setOnClickListener(this);
        }

        //For drag handle, Call drag listener to start a drag when image is pressed down
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                mDragListener.onStartDrag(this);
            return false;
        }

        //For visibility button, notify map to hide layer
        @Override
        public void onClick(View v) {
            visibility = getNewVisibility();
            vis.setImageAlpha(visibility == Layer.VISIBLE ? 255 : (visibility == Layer.TRANSPARENT ? 160 : 60));
            mDragListener.onToggleLayer(mLayers.get(getAdapterPosition()), visibility);
        }

        private int getNewVisibility() {
            return visibility == Layer.VISIBLE ? Layer.TRANSPARENT : (visibility == Layer.TRANSPARENT ? Layer.INVISIBLE : Layer.VISIBLE);
        }
    }

    @Override
    public CurrViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.curr_layer_item, parent, false);
        return new CurrViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CurrViewHolder holder, int position) {
        holder.visibility = mLayers.get(position).getVisibility();
        holder.tv.setText(mLayers.get(position).getTitle());
        holder.vis.setImageAlpha(holder.visibility == Layer.VISIBLE ? 255 : (holder.visibility == Layer.TRANSPARENT ? 160 : 60));
    }

    @Override
    public int getItemCount() {
        return mLayers.size();
    }

    /**
     * Swaps all layers inside the interval defined by the positions
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then resolved position of the moved item.
     * @return Always true
     */
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mLayers, i, i + 1);
                mDragListener.onSwapNeeded(i, i + 1);
            }
        else
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mLayers, i, i - 1);
                mDragListener.onSwapNeeded(i, i - 1);
            }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    //Called when an item is swiped, delete layer from map
    @Override
    public void onItemDismiss(int position) {
        Layer temp = mLayers.remove(position);
        notifyItemRemoved(position);
        mDragListener.onLayerSwiped(position, temp);
    }

    public void insertList(ArrayList<Layer> list) {
        mLayers = list;
        notifyDataSetChanged();
    }
}
