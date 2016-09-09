package com.iamtechknow.worldview.map;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.iamtechknow.worldview.model.Layer;

/**
 * Tile provider that accesses tiles from an image cache, or makes a network request to access them from a remote source.
 * This tile provider provides tiles for a given layer and date. Needs a reference to the presenter for cache access.
 */
public class CacheTileProvider implements TileProvider {
    private static final int SIZE = 256; //Assume all images are 256x256 pixels

    private CachePresenter presenter;
    private Layer layer;

    public CacheTileProvider(Layer _layer, CachePresenter mapPresenter) {
        layer = _layer;
        presenter = mapPresenter;
    }

    /**
     * This method runs on a custom background thread. First the cache is checked to ensure tiles
     * exist for the arguments and the layer by generating a key and checking if it is in the cache already.
     * If not then go download them all via Retrofit and store them into its image cache.
     * The images should be in the cache now, so we can use the arguments to get the image, and then create a tile.
     */
    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] imageData = presenter.getMapTile(layer, zoom, y, x);
        return new Tile(SIZE, SIZE, imageData);
    }
}
