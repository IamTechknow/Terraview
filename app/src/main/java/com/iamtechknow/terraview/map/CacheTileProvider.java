package com.iamtechknow.terraview.map;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

/**
 * Tile provider that accesses tiles from an image cache, or makes a network request to access them from a remote source.
 * This tile provider provides tiles for a given layer and date. Needs a reference to the presenter for cache access.
 */
public class CacheTileProvider implements TileProvider {
    private static final int SIZE = 256; //Assume all images are 256x256 pixels

    private MapInteractor map;
    private Layer layer;
    private String date;

    public CacheTileProvider(Layer _layer, String isoDate, MapInteractor interactor) {
        layer = _layer;
        map = interactor;
        date = isoDate;
    }

    /**
     * This method runs on a custom background thread.
     * Attempt to fetch the cached tile, or download it from GIBS.
     * If a download fails due to a connection timeout, return null to try again later.
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] imageData = map.getMapTile(layer, date, zoom, y, x);
        return imageData.length != 0 ? new Tile(SIZE, SIZE, imageData) : null;
    }
}
