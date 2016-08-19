package com.iamtechknow.worldview.map;

import com.google.android.gms.maps.GoogleMap;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;
import java.util.Date;

public interface MapPresenter {
    void onResume();

    void onDestroy();

    void onMapLoaded(GoogleMap gmaps); //TODO: Fetch data here then insert tile overlays

    void onDateChanged(Date date);

    void onConnectionAvailable();

    void setLayersAndUpdateMap(ArrayList<Layer> stack); //TODO: Replace stack, replace tiles (private), init z-offsets (private method)

    void swapLayerPositions(int i, int i_new); //TODO: Next three are responses to recyclerview gestures

    void toggleLayer(Layer l, boolean hide);

    void deleteLayer(int position);
}
