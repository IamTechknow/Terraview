package com.iamtechknow.worldview.map;

import android.content.Context;
import android.support.v4.app.LoaderManager;

import com.google.android.gms.maps.GoogleMap;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.Date;

public interface MapPresenter {

    void onMapReady(GoogleMap gmaps);

    void onDateChanged(Date date);

    void getRemoteData(Context c);

    void getLocalData(LoaderManager manager, Context c);

    void setLayersAndUpdateMap(ArrayList<Layer> stack);

    void onSwapNeeded(int i, int i_new);

    void onToggleLayer(Layer l, boolean hide);

    void onLayerSwiped(int position);

    ArrayList<Layer> getCurrLayerStack();
}
