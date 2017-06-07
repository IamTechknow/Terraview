package com.iamtechknow.terraview.map;

import android.support.v4.util.LruCache;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.iamtechknow.terraview.api.ImageAPI;
import com.iamtechknow.terraview.model.Layer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MapInteractorImpl implements MapInteractor {
    private static final float MAX_ZOOM = 9.0f;
    private static final String BASE_URL = "https://gibs.earthdata.nasa.gov";

    private GoogleMap gMaps;

    //Retrofit object used to fetch images
    private ImageAPI api;

    //Cache data to hold tile image data for a given parsable key
    private LruCache<String, byte[]> byteCache;

    private Polygon currPolygon;

    public MapInteractorImpl(GoogleMap map) {
        gMaps = map;
        gMaps.setMaxZoomPreference(MAX_ZOOM);
        gMaps.setMapType(GoogleMap.MAP_TYPE_NONE);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        api = retrofit.create(ImageAPI.class);

        //Set a overall size limit of the cache to 1/8 of memory available, defining cache size by the array length.
        byteCache = new LruCache<String, byte[]>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)) {
            @Override
            protected int sizeOf(String key, byte[] array) {
                // The cache size will be measured in kilobytes rather than number of items.
                return array.length / 1024;
            }
        };
    }

    @Override
    public TileOverlay addTile(Layer layer, String date) {
        CacheTileProvider provider = new CacheTileProvider(layer, date,this);
        return gMaps.addTileOverlay(new TileOverlayOptions().tileProvider(provider).fadeIn(false));
    }

    @Override
    public void removeTile(TileOverlay tile, Layer l) {
        //Get all keys and remove all entries that start with the identifier
        Set<String> keys =  byteCache.snapshot().keySet();
        for(String key : keys)
            if(key.startsWith(l.getIdentifier()))
                byteCache.remove(key);
        tile.remove();
    }

    /**
     * First the cache is checked to ensure tiles exist for the arguments
     * and the layer by generating a key and checking if it is in the cache already.
     * If not then go download them all via Retrofit and store them into its image cache.
     *
     * This method isn't executed when invisible tile overlays are created, only when they
     * become visible. Therefore the tiles for animation overlays are downloaded when shown during
     * an overlay's animation frame, so we can get the date on real-time.
     */
    @Override
    public byte[] getMapTile(Layer l, String date, int zoom, int y, int x) {
        String key = getCacheKey(l, date, zoom, y, x);
        byte[] data = byteCache.get(key);

        if(data == null) {
            data = fetchImage(l, date, zoom, y, x);
            byteCache.put(key, data);
        }

        return data;
    }

    /**
     * Simply move the camera to the specified point
     * @param point where to move the camera
     */
    @Override
    public void moveCamera(LatLng point) {
        gMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(point, MAX_ZOOM - 1));
    }

    /**
     * Draw the polygon and show it to the user
     * @param poly shape to draw the camera
     */
    @Override
    public void drawPolygon(PolygonOptions poly) {
        currPolygon = gMaps.addPolygon(poly);
        gMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(poly.getPoints().get(0), MAX_ZOOM - 2));
    }

    @Override
    public void clearPolygon() {
        if(currPolygon != null)
            currPolygon.remove();
    }

    /**
     * Return a key to be used in the cache based on given arguments.
     * Done by concatenating the parameters between slashes to allow parsing if needed
     * @return String to be used as a key for the byte cache
     */
    private String getCacheKey(Layer layer, String date, int zoom, int y, int x) {
        return String.format(Locale.US, "%s/%s/%d/%d/%d", layer.getIdentifier(), date, zoom, y, x);
    }

    /**
     * Fetch the image from GIBS to put onto the cache with Retrofit. This method is
     * executed in a GMaps background thread so RxJava is not necessary. Do account for
     * 404 error codes if no tile exists (can happen if zoomed in too far).
     * @return byte array of the image
     */
    private byte[] fetchImage(Layer l, String date, int zoom, int y, int x) {
        Call<ResponseBody> result = api.fetchImage(l.getIdentifier(), date, l.getTileMatrixSet(), Integer.toString(zoom), Integer.toString(y), Integer.toString(x), l.getFormat());
        byte[] temp = new byte[0];

        try {
            Response<ResponseBody> r = result.execute();
            if(r.isSuccessful())
                temp = r.body().bytes();
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(), e);
        }
        return temp;
    }
}