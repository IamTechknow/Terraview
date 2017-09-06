package com.iamtechknow.terraview.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.iamtechknow.terraview.model.Layer;

/**
 * Interactor for Google Maps to encapsulate behaviour behind an interface to make it mockable,
 * because it is declared as a final class which limits unit testing.
 * Also encapsulates the caching of tiles.
 */
public interface MapInteractor extends OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
            GoogleMap.OnMapClickListener {

    //Callback for toggling colormap UI
    interface ToggleListener {
        void onToggleColorMap(boolean show);
    }

    TileOverlay addTile(Layer l, String date);

    void removeTile(TileOverlay tile, Layer l, boolean isDateChange);

    byte[] getMapTile(Layer l, String date, int zoom, int y, int x);

    void moveCamera(LatLng point);

    void drawPolygon(PolygonOptions poly);

    void clearPolygon();

    void setToggleListener(ToggleListener l);

    void setToggleState(boolean show);
}
