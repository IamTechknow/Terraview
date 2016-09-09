package com.iamtechknow.worldview.map;

import com.iamtechknow.worldview.model.Layer;

public interface CachePresenter {
    byte[] getMapTile(Layer l, int zoom, int y, int x);
}
