package com.iamtechknow.worldview.map;

import com.iamtechknow.worldview.model.Layer;

/**
 * Interface for a presenter that controls a cache. Allows a client to access the cache,
 * which will be populated when cache misses occurs. Separated from the map presenter
 * for easier testing but a class can implement both.
 */
public interface CachePresenter {
    byte[] getMapTile(Layer l, int zoom, int y, int x);
}
