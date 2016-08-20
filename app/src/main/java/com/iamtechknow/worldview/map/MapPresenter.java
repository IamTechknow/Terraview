package com.iamtechknow.worldview.map;

import com.google.android.gms.maps.GoogleMap;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.Date;

public interface MapPresenter {

    void onMapReady(GoogleMap gmaps); //TODO: Fetch data here then insert tile overlays

    void onDateChanged(Date date);

    void getData(boolean needInternet);

    void setLayersAndUpdateMap(ArrayList<Layer> stack);

    void onSwapNeeded(int i, int i_new);

    void onToggleLayer(Layer l, boolean hide);

    void onLayerSwiped(int position);

    ArrayList<Layer> getCurrLayerStack();
}
