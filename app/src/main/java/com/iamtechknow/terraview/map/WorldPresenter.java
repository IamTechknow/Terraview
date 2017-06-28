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
import java.util.Date;
import java.util.Hashtable;

import static com.iamtechknow.terraview.map.WorldActivity.*;

public class WorldPresenter implements MapPresenter, DataSource.LoadCallback {
    private static final float Z_OFFSET = 5.0f, BASE_Z_OFFSET = -100.0f; //base layers cannot cover overlays

    private WeakReference<MapView> mapViewRef;
    private DataSource dataSource;

    //Used to let presenter know to restore state after map loads
    private boolean isRestoring;

    //Worldview data
    private MapInteractor map;
    private ArrayList<Layer> layer_stack;
    private Hashtable<String, TileOverlay> tileOverlays;
    private Date currentDate;
    private Event currEvent;

    public WorldPresenter(MapInteractor maps) {
        layer_stack = new ArrayList<>();
        tileOverlays = new Hashtable<>();
        map = maps;

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
        currentDate = new Date(savedInstanceState.getLong(RESTORE_TIME_EXTRA));
        currEvent = savedInstanceState.getParcelable(RESTORE_EVENT_EXTRA);

        if(getMapView() != null)
            getMapView().updateDateDialog(currentDate.getTime());
    }

    //If needed, restore map tiles or set default
    @Override
    public void onMapReady(GoogleMap gMaps) {
        map.onMapReady(gMaps);

        if(isRestoring) {
            isRestoring = false;
            setLayersAndUpdateMap(layer_stack, null);
            onDateChanged(currentDate);

            if(currEvent != null)
                presentEvent(currEvent);
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

        if(isLayerStartAfterCurrent(layer_stack) && getMapView() != null)
            getMapView().warnUserAboutActiveLayers();
    }

    @Override
    public Date getCurrDate() {
        return currentDate;
    }

    @Override
    public Event getCurrEvent() {
        return currEvent;
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
     * so wait until it gets called later. Warn user if current date is before any active layers
     * @param stack list representing current layers to be shown
     * @param delete list of layers to delete first
     */
    @Override
    public void setLayersAndUpdateMap(ArrayList<Layer> stack, ArrayList<Layer> delete) {
        layer_stack = stack;
        if(isRestoring)
            return;

        if(getMapView() != null)
            getMapView().setLayerList(layer_stack);

        //delete layers and cached tiles
        if(delete != null)
            for(Layer l : delete)
                if(tileOverlays.containsKey(l.getIdentifier()))
                    map.removeTile(tileOverlays.remove(l.getIdentifier()), l);

        //Add layers not already on
        for(Layer l : layer_stack)
            if(!tileOverlays.containsKey(l.getIdentifier()))
                addTileOverlay(l);
        initZOffsets();

        if(isLayerStartAfterCurrent(layer_stack) && getMapView() != null)
            getMapView().warnUserAboutActiveLayers();
    }

    @Override
    public void onSwapNeeded(int i, int i_new) {
        //Get the tile overlays and change the z-indices.
        //If one of the tile overlays is a base layer, neither Z-order changes
        if(!layer_stack.get(i).isBaseLayer() && !layer_stack.get(i_new).isBaseLayer()) {
            TileOverlay above, below;
            if (i > i_new) { //swapping above, above layer is the one going up
                above = tileOverlays.get(layer_stack.get(i_new).getIdentifier());
                below = tileOverlays.get(layer_stack.get(i).getIdentifier());
            } else { //swapping below, below layer is the one going down
                above = tileOverlays.get(layer_stack.get(i).getIdentifier());
                below = tileOverlays.get(layer_stack.get(i_new).getIdentifier());
            }
            above.setZIndex(above.getZIndex() + Z_OFFSET);
            below.setZIndex(below.getZIndex() - Z_OFFSET);
        }
    }

    /**
     * Access the tile overlay to change its visibility
     * @param l The layer corresponding to the tile overlay
     * @param visibility Invisible, visible, 50% transparent
     */
    @Override
    public void onToggleLayer(Layer l, int visibility) {
        l.setVisible(visibility);
        switch(visibility) {
            case Layer.VISIBLE:
                tileOverlays.get(l.getIdentifier()).setVisible(true);
                tileOverlays.get(l.getIdentifier()).setTransparency(0.0f);
                break;
            case Layer.INVISIBLE:
                tileOverlays.get(l.getIdentifier()).setVisible(false);
                break;
            default: //Transparent
                tileOverlays.get(l.getIdentifier()).setTransparency(0.5f);
        }
    }

    /**
     * Called start the current layer adapter to delete a layer at the model level
     * @param position the position of the deleted list item
     */
    @Override
    public void onLayerSwiped(int position, Layer l) {
        map.removeTile(tileOverlays.remove(l.getIdentifier()), l);
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
     * Also warn the user if the date change affects viewable data.
     * @param e the selected event data
     */
    @Override
    public void presentEvent(Event e) {
        currEvent = e;
        onDateChanged(Utils.parseISODate(e.getDates().get(0)));
        if(getMapView() != null) {
            getMapView().updateDateDialog(currentDate.getTime());
            getMapView().showEvent(e);
            if(!isLayerStartAfterCurrent(layer_stack))
                getMapView().showChangedEventDate(Utils.parseDateForDialog(currentDate));
        }

        map.clearPolygon();
        if(e.hasPoint())
            map.moveCamera(e.getPoints().get(0));
        else
            map.drawPolygon(e.getPolygon());
    }

    @Override
    public void onClearEvent() {
        map.clearPolygon();
        currEvent = null;
        if(getMapView() != null)
            getMapView().clearEvent();
    }

    @Override
    public boolean isVIIRSActive() {
        TileOverlay viirs = tileOverlays.get("VIIRS_SNPP_CorrectedReflectance_TrueColor");
        return viirs != null && viirs.isVisible();
    }

    /**
     * If the VIIRS layer is active but current date is before its start date,
     * disable it and add the Terra satellite layer.
     */
    @Override
    public void fixVIIRS() {
        for(Layer viirs_layer : layer_stack)
            if(viirs_layer.getIdentifier().equals("VIIRS_SNPP_CorrectedReflectance_TrueColor"))
                viirs_layer.setVisible(Layer.INVISIBLE);

        TileOverlay viirs = tileOverlays.get("VIIRS_SNPP_CorrectedReflectance_TrueColor");
        viirs.setVisible(false);

        Layer terra = new Layer("MODIS_Terra_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, MODIS, Terra)", "Terra / MODIS", null, "2012-01-01", null, null, true);
        layer_stack.add(terra);
        addTileOverlay(terra);
        initZOffsets();
        if(getMapView() != null)
            getMapView().setLayerList(layer_stack);
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
     * Add the tile to the map, and set visibility
     * @param layer Layer containing necessary data
     */
    private void addTileOverlay(Layer layer) {
        tileOverlays.put(layer.getIdentifier(), map.addTile(layer, Utils.parseDate(currentDate)));
        onToggleLayer(layer, layer.getVisibility());
    }

    /**
     * After tile overlays added, set the Z-Order from the default of 0.0
     * Layers at the top of the list have the highest Z-order
     * Base layers will be not affected to avoid covering overlays
     */
    private void initZOffsets() {
        int z_index_mod = 0; //need separate index for overlays
        for(int i = 0; i < layer_stack.size(); i++)
            if(layer_stack.get(i).isBaseLayer())
                tileOverlays.get(layer_stack.get(i).getIdentifier()).setZIndex(BASE_Z_OFFSET);
            else
                tileOverlays.get(layer_stack.get(i).getIdentifier()).setZIndex(Z_OFFSET * (tileOverlays.size() - 1 - z_index_mod++));
    }

    //Remove all tile overlays and cached tiles, used to replace with new set
    private void removeAllTileOverlays() {
        for(Layer l  : layer_stack)
            map.removeTile(tileOverlays.get(l.getIdentifier()), l);
        tileOverlays.clear();
    }

    //Called when a layer is added or date is changed, verify if data cannot be shown
    private boolean isLayerStartAfterCurrent(ArrayList<Layer> list) {
        for(Layer l : list)
            if(currentDate.compareTo(Utils.parseISODate(l.getStartDate())) < 0)
                return true;
        return false;
    }
}
