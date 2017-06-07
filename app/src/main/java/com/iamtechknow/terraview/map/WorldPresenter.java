package com.iamtechknow.terraview.map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.iamtechknow.terraview.Injection;
import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.data.LocalDataSource;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static com.iamtechknow.terraview.map.WorldActivity.*;

public class WorldPresenter implements MapPresenter, DataSource.LoadCallback {
    private static final float Z_OFFSET = 5.0f, BASE_Z_OFFSET = -50.0f; //base layers cannot cover overlays

    private WeakReference<MapView> mapViewRef;
    private DataSource dataSource;

    //Used to let presenter know to restore state after map loads
    private boolean isRestoring;

    //Worldview data
    private MapInteractor map;
    private ArrayList<Layer> layer_stack;
    private ArrayList<TileOverlay> mCurrLayers;
    private Date currentDate;
    private Event currEvent;

    public WorldPresenter() {
        mCurrLayers = new ArrayList<>();
        layer_stack = new ArrayList<>();

        currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        Utils.getCalendarMidnightTime(c);
        currentDate = c.getTime();
    }

    @Override
    public void attachView(MapView v) {
        mapViewRef = new WeakReference<>(v);
        mapViewRef.get().setDateDialog(currentDate.getTime());
    }

    @Override
    public void detachView() {
        if(mapViewRef != null) {
            mapViewRef.clear();
            mapViewRef = null;
        }
    }

    //Just restore model here
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        isRestoring = true;

        layer_stack = savedInstanceState.getParcelableArrayList(RESTORE_LAYER_EXTRA);
        Long l = savedInstanceState.getLong(RESTORE_TIME_EXTRA);
        currentDate = new Date(l);

        if(getMapView() != null)
            getMapView().updateDateDialog(l);
    }

    //If needed, restore map tiles or set default
    @Override
    public void onMapReady(GoogleMap gMaps) {
        map = new MapInteractorImpl(gMaps);

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
        dataSource = Injection.provideRemoteSource(c);
        dataSource.loadData(this);
    }

    @Override
    public void onDataLoaded() {}

    @Override
    public void onDataNotAvailable() {}

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
        if(getMapView() != null)
            getMapView().setLayerList(layer_stack);
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
     * Called start the current layer adapter to delete a layer at the model level
     * @param position the position of the deleted list item
     */
    @Override
    public void onLayerSwiped(int position, Layer l) {
        map.removeTile(mCurrLayers.remove(position), l);

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

    @Override
    public void presentColorMaps() {
        if(getMapView() != null)
            getMapView().showColorMaps();
    }

    @Override
    public void chooseLayers() {
        if(getMapView() != null)
            getMapView().showPicker();
    }

    @Override
    public void presentEvents() {
        if(getMapView() != null)
            getMapView().showEvents();
    }

    @Override
    public void presentAbout() {
        if(getMapView() != null)
            getMapView().showAbout();
    }

    @Override
    public void presentHelp() {
        if(getMapView() != null)
            getMapView().showHelp();
    }

    @Override
    public void presentAnimDialog() {
        if(getMapView() != null)
            getMapView().showAnimDialog();
    }

    /**
     * Change all layers to the event date and animate the camera.
     * @param e the selected event data
     */
    @Override
    public void presentEvent(Event e) {
        currEvent = e;
        onDateChanged(Utils.parseISODate(e.getDate()));
        if(getMapView() != null) {
            getMapView().updateDateDialog(currentDate.getTime());
            getMapView().showEvent(e);
        }

        map.clearPolygon();
        if(e.hasPoint())
            map.moveCamera(e.getPoint());
        else
            map.drawPolygon(e.getPolygon());
    }

    @Override
    public void onClearEvent() {
        map.clearPolygon();
        if(getMapView() != null)
            getMapView().clearEvent();
    }

    private MapView getMapView() {
        return mapViewRef == null ? null : mapViewRef.get();
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
        if(getMapView() != null)
            getMapView().setLayerList(layer_stack);
    }

    /**
     * Set up the tile provider for the specified layer by allowing giving the necessary endpoint
     * @param layer Layer containing necessary data
     */
    private void addTileOverlay(Layer layer) {
        mCurrLayers.add(map.addTile(layer, Utils.parseDate(currentDate)));
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
}
