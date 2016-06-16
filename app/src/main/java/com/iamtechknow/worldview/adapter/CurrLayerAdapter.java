package com.iamtechknow.worldview.adapter;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.Collections;

public class CurrLayerAdapter extends RecyclerView.Adapter<CurrLayerAdapter.CurrViewHolder>
        implements ItemTouchHelperAdapter {
    private ArrayList<Layer> mLayers;
    private final onStartDragListener mDragListener;

    public CurrLayerAdapter(onStartDragListener listener) {
        super();
        mLayers = new ArrayList<>();
        mDragListener = listener;
    }

    public class CurrViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        TextView tv;
        ImageView drag_handle;

        public CurrViewHolder(View itemView) {
            super(itemView);

            tv = (TextView) itemView.findViewById(R.id.curr_layer_text);
            drag_handle = (ImageView) itemView.findViewById(R.id.curr_layer_drag_handle);

            itemView.setClickable(true);
            drag_handle.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Call listener to start a drag when image is pressed down
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                mDragListener.onStartDrag(this);
            return false;
        }
    }

    @Override
    public CurrViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.curr_layer_item, parent, false);
        return new CurrViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CurrViewHolder holder, int position) {
        holder.tv.setText(mLayers.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mLayers.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(mLayers, i, i + 1);
        else
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(mLayers, i, i - 1);

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    //Called when an item is swiped, delete layer from map
    @Override
    public void onItemDismiss(int position) {
        mLayers.remove(position);
        notifyItemRemoved(position);
    }

    public void insertList(ArrayList<Layer> list) {
        mLayers = list;
        notifyDataSetChanged();
    }
}
