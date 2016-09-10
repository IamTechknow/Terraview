package com.iamtechknow.worldview.map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.iamtechknow.worldview.api.ImageAPI;
import com.iamtechknow.worldview.data.DataSource;
import com.iamtechknow.worldview.data.LocalDataSource;
import com.iamtechknow.worldview.data.RemoteDataSource;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.iamtechknow.worldview.map.WorldActivity.*;

public class WorldPresenter implements MapPresenter, CachePresenter, DataSource.LoadCallback {
    private static final String BASE_URL = "http://gibs.earthdata.nasa.gov";
    private static final float Z_OFFSET = 5.0f, BASE_Z_OFFSET = -50.0f, MAX_ZOOM = 9.0f; //base layers cannot cover overlays

    private MapView mapView;
    private DataSource dataSource;

    //Used to let presenter know to restore state after map loads
    private boolean isRestoring;

    //Worldview data
    private GoogleMap gMaps;
    private ArrayList<Layer> layer_stack;
    private ArrayList<TileOverlay> mCurrLayers;
    private Date currentDate;

    //Cache data to hold tile image data for a given parsable key
    private LruCache<String, byte[]> byteCache;

    public WorldPresenter(MapView view) {
        mapView = view;
        mCurrLayers = new ArrayList<>();
        layer_stack = new ArrayList<>();

        //Set a overall size limit of the cache to 1/8 of memory available, defining cache size by the array length.
        byteCache = new LruCache<String, byte[]>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)) {
            @Override
            protected int sizeOf(String key, byte[] array) {
                // The cache size will be measured in kilobytes rather than number of items.
                return array.length / 1024;
            }
        };

        currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        mapView.setDateDialog(c.getTimeInMillis());
    }

    //Just restore model here
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        isRestoring = true;

        layer_stack = savedInstanceState.getParcelableArrayList(LAYER_EXTRA);
        Long l = savedInstanceState.getLong(TIME_EXTRA);
        currentDate = new Date(l);
        mapView.updateDateDialog(l);
    }

    //If needed, restore map tiles or set default
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMaps = googleMap;
        gMaps.setMaxZoomPreference(MAX_ZOOM);
        gMaps.setMapType(GoogleMap.MAP_TYPE_NONE);

        if(isRestoring) {
            isRestoring = false;
            setLayersAndUpdateMap(layer_stack);
            onDateChanged(currentDate);
        } else
            showDefaultTiles();
    }

    @Override
    public void onDateChanged(Date date) {
        currentDate = date;
        removeAllTileOverlays();
        for(Layer l: layer_stack)
            addTileOverlay(l);
        initZOffsets();
    }

    @Override
    public Date getCurrDate() {
        return currentDate;
    }

    @Override
    public void getLocalData(LoaderManager manager, Context c) {
        dataSource = new LocalDataSource(manager, c);
        dataSource.loadData(this);
    }

    @Override
    public void getRemoteData(Context c) {
        dataSource = new RemoteDataSource(c);
        dataSource.loadData(this);
    }

    @Override
    public void onDataLoaded() {

    }

    @Override
    public void onDataNotAvailable() {

    }

    /**
     * Called whenever layers are to be added at startup or when selected
     * Create the tile overlays to be shown on the map.
     * If layers were added and the screen is rotated, this can get called when gMaps in null
     * so wait until it gets called later.
     * @param stack list representing current layers to be shown
     */
    @Override
    public void setLayersAndUpdateMap(ArrayList<Layer> stack) {
        if(isRestoring)
            return;

        layer_stack = stack;
        mapView.setLayerList(layer_stack);
        removeAllTileOverlays();
        for(Layer l: layer_stack)
            addTileOverlay(l);
        initZOffsets();
    }

    @Override
    public void onSwapNeeded(int i, int i_new) {
        //Swap the objects in the underlying data and change the Z-order
        //If one of the tile overlays is a base layer, neither Z-order changes
        if(!layer_stack.get(i).isBaseLayer() && !layer_stack.get(i_new).isBaseLayer()) {
            TileOverlay above, below;
            if (i > i_new) { //swapping above
                above = mCurrLayers.get(i);
                below = mCurrLayers.get(i_new);
                above.setZIndex(above.getZIndex() + Z_OFFSET);
                below.setZIndex(below.getZIndex() - Z_OFFSET);
            } else { //swapping below
                above = mCurrLayers.get(i_new);
                below = mCurrLayers.get(i);
                above.setZIndex(above.getZIndex() + Z_OFFSET);
                below.setZIndex(below.getZIndex() - Z_OFFSET);
            }
        }
        Collections.swap(mCurrLayers, i, i_new);
    }

    /**
     * Access the tile overlay to change its visibility
     * @param l The layer corresponding to the tile overlay
     * @param hide Visibility of the tile overlay
     */
    @Override
    public void onToggleLayer(Layer l, boolean hide) {
        int pos = layer_stack.indexOf(l);
        mCurrLayers.get(pos).setVisible(hide);
    }

    /**
     * Called from the current layer adapter to delete a layer at the model level
     * @param position the position of the deleted list item
     */
    @Override
    public void onLayerSwiped(int position, Layer l) {
        TileOverlay temp = mCurrLayers.remove(position);
        temp.remove();

        //Get all keys and remove all entries that start with the identifier
        Set<String> keys =  byteCache.snapshot().keySet();
        for(String key : keys)
            if(key.startsWith(l.getIdentifier()))
                byteCache.remove(key);

        //Fix Z-Order of other overlays
        for(int i = 0; i < mCurrLayers.size(); i++) {
            TileOverlay t = mCurrLayers.get(i);
            t.setZIndex(t.getZIndex() - Z_OFFSET);
        }
    }

    @Override
    public ArrayList<Layer> getCurrLayerStack(){
        return layer_stack;
    }

    /**
     * First the cache is checked to ensure tiles exist for the arguments
     * and the layer by generating a key and checking if it is in the cache already.
     * If not then go download them all via Retrofit and store them into its image cache.
     */
    @Override
    public byte[] getMapTile(Layer l, int zoom, int y, int x) {
        String key = getCacheKey(l, zoom, y, x);
        byte[] data = byteCache.get(key);

        if(data == null) {
            data = fetchImage(l, Utils.parseDate(currentDate), zoom, y, x);
            byteCache.put(key, data);
        }

        return data;
    }

    /**
     * Show the VIIRS Corrected Reflectance (True Color) overlay for today and coastlines
     */
    private void showDefaultTiles() {
        Layer l = new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, VIIRS, SNPP)", "Suomi NPP / VIIRS", null, "2015-11-24", null, null, true),
                coastline = new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, null, false);
        layer_stack.add(coastline);
        layer_stack.add(l);

        addTileOverlay(coastline);
        addTileOverlay(l);
        initZOffsets();
        mapView.setLayerList(layer_stack);
    }

    /**
     * Set up the tile provider for the specified layer by allowing giving the necessary endpoint
     * @param layer Layer containing necessary data
     */
    private void addTileOverlay(final Layer layer) {
        //Make a tile overlay
        CacheTileProvider provider = new CacheTileProvider(layer, this);

        mCurrLayers.add(gMaps.addTileOverlay(new TileOverlayOptions().tileProvider(provider).fadeIn(false)));
    }

    /**
     * After tile overlays added, set the Z-Order from the default of 0.0
     * Layers at the top of the list have the highest Z-order
     * Base layers will be not affected to avoid covering overlays
     */
    private void initZOffsets() {
        for(int i = 0; i < mCurrLayers.size(); i++)
            if(layer_stack.get(i).isBaseLayer())
                mCurrLayers.get(i).setZIndex(BASE_Z_OFFSET);
            else
                mCurrLayers.get(i).setZIndex(Z_OFFSET * (mCurrLayers.size() - 1 - i));
    }

    //Remove all tile overlays, used to replace with new set
    //The default GMaps tile is always present
    private void removeAllTileOverlays() {
        for(TileOverlay t : mCurrLayers)
            t.remove();
        mCurrLayers.clear();
    }

    /**
     * Return a key to be used in the cache based on given arguments.
     * Done by concatenating the parameters between slashes to allow parsing if needed
     * @return String to be used as a key for the byte cache
     */
    private String getCacheKey(Layer layer, int zoom, int y, int x) {
        return String.format(Locale.US, "%s/%s/%d/%d/%d", layer.getIdentifier(), Utils.parseDate(currentDate), zoom, y, x);
    }

    /**
     * Fetch the image from GIBS to put onto the cache with Retrofit. This method is
     * executed in a GMaps background thread so RxJava is not necessary. Do account for
     * 404 error codes if no tile exists (can happen if zoomed in too far).
     * @return byte array of the image
     */
    private byte[] fetchImage(Layer l, String date, int zoom, int y, int x) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        ImageAPI api = retrofit.create(ImageAPI.class);
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
