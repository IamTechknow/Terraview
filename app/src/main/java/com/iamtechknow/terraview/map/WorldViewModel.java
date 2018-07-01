package com.iamtechknow.terraview.map;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.TileOverlay;
import com.iamtechknow.terraview.Injection;
import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static com.iamtechknow.terraview.map.WorldActivity.*;

public class WorldViewModel extends ViewModel implements OnMapReadyCallback, MapInteractor.ToggleListener, DataSource.LoadCallback {
    private static final String BASE_URL = "https://gibs.earthdata.nasa.gov";
    private static final float Z_OFFSET = 5.0f, BASE_Z_OFFSET = -100.0f; //base layers cannot cover overlays

    //Used to let view model know to restore state after map loads
    private boolean isRestoring;

    //Needed to update colormap if layers get swapped/deleted
    private boolean showColormap;

    //Worldview data
    private MapInteractor map;
    private ArrayList<Layer> layer_stack;
    private Hashtable<String, TileOverlay> tileOverlays;
    private Hashtable<String, ColorMap> colorMaps;
    private Date currentDate;
    private Event currEvent;
    private int currEventPoint;

    //Mechanism to pass null or valid ColorMap for View to react
    private Subject<ColorMap> colorMapSub;

    public WorldViewModel(MapInteractor maps, Subject<ColorMap> sub) {
        layer_stack = new ArrayList<>();
        tileOverlays = new Hashtable<>();
        colorMaps = new Hashtable<>();
        map = maps;
        map.setToggleListener(this);
        colorMapSub = sub;

        currentDate = new Date();
    }

    //Needed to account for config change when user selects layers or events
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        isRestoring = true;
        layer_stack = savedInstanceState.getParcelableArrayList(RESTORE_LAYER_EXTRA);
    }

    //Map is ready, set up the map interactor and current date.
    @Override
    public void onMapReady(GoogleMap gMaps) {
        map.onMapReady(gMaps);
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        currentDate = Utils.getCalendarMidnightTime(c);

        if(isRestoring) {
            isRestoring = false;
            setLayersAndUpdateMap(layer_stack, null);
            onDateChanged(currentDate);
        } else
            showDefaultTiles();
    }

    public void onDateChanged(Date date) {
        currentDate = date;
        removeAllTileOverlays();
        for(Layer l: layer_stack)
            addTileOverlay(l);
        initZOffsets();
    }

    public Date getCurrDate() {
        return currentDate;
    }

    public Event getCurrEvent() {
        return currEvent;
    }

    public ArrayList<Layer> getCurrLayerStack(){
        return layer_stack;
    }

    public Subject<ColorMap> getColorMapSub() {
        return colorMapSub;
    }

    public void getRemoteData(Context c) {
        Injection.provideRemoteSource(c).loadData(this);
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
    public void setLayersAndUpdateMap(ArrayList<Layer> stack, ArrayList<Layer> delete) {
        layer_stack = stack;
        if(isRestoring)
            return;

        //delete layers and cached tiles
        if(delete != null)
            for(Layer l : delete)
                if(tileOverlays.containsKey(l.getIdentifier()))
                    map.removeTile(tileOverlays.remove(l.getIdentifier()), l, false);

        //Add layers not already on and download colormap if they have one
        for(Layer l : layer_stack)
            if(!tileOverlays.containsKey(l.getIdentifier())) {
                addTileOverlay(l);
                if(l.hasColorMap())
                    getColorMapForId(l.getIdentifier(), l.getPalette());
            }
        initZOffsets();
    }

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

            if(showColormap)
                onToggleColorMap(true);
        }
    }

    /**
     * Access the tile overlay to change its visibility
     * @param l The layer corresponding to the tile overlay
     * @param visibility Invisible, visible, 50% transparent
     */
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
     * Called by the current layer adapter to delete a layer at the model level
     */
    public void onLayerSwiped(Layer l) {
        //If deleted layer was only layer with colormap, hide UI
        if(showColormap && findTopColorMap() == null) {
            showColormap = false;
            onToggleColorMap(false);
        } else if(showColormap)
            onToggleColorMap(showColormap);

        map.removeTile(tileOverlays.remove(l.getIdentifier()), l, false);
    }

    /**
     * Change all layers to the event date and animate the camera.
     * Also warn the user if the date change affects viewable data.
     * @param e the selected event data
     */
    public void presentEvent(Event e) {
        currEvent = e;
        if(!isGMapsAvailable())
            return;

        onDateChanged(Utils.parseISODate(e.getDates().get(0)));
        if(e.getDates().size() > 1 && showColormap) { //hide colormap if event widget shown
            onToggleColorMap(false);
            map.setToggleState(false);
        }

        map.clearPolygon();
        if(e.hasPoint())
            map.moveCamera(e.getPoints().get(0));
        else
            map.drawPolygon(e.getPolygon());
    }

    public void onClearEvent() {
        map.clearPolygon();
        currEvent = null;
        currEventPoint = 0;
    }

    public boolean isVIIRSActive() {
        TileOverlay viirs = tileOverlays.get("VIIRS_SNPP_CorrectedReflectance_TrueColor");
        return viirs != null && viirs.isVisible();
    }

    /**
     * If the VIIRS layer is active but current date is before its start date,
     * disable it and add the Terra satellite layer.
     */
    public void fixVIIRS() {
        for(Layer viirs_layer : layer_stack)
            if(viirs_layer.getIdentifier().equals("VIIRS_SNPP_CorrectedReflectance_TrueColor"))
                viirs_layer.setVisible(Layer.INVISIBLE);

        TileOverlay viirs = tileOverlays.get("VIIRS_SNPP_CorrectedReflectance_TrueColor");
        viirs.setVisible(false);

        Layer terra = new Layer("MODIS_Terra_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, MODIS, Terra)", "Terra / MODIS", null, "2009-01-01", null, null, true);
        layer_stack.add(terra);
        addTileOverlay(terra);
        initZOffsets();
    }

    public void onEventProgressSelected(int progress) {
        if(progress != currEventPoint) { //Don't reload layers if same date
            currEventPoint = progress;
            onDateChanged(Utils.parseISODate(currEvent.getDates().get(progress)));
            map.moveCamera(currEvent.getPoints().get(progress));
        }
    }

    @Override
    public void onToggleColorMap(boolean show) {
        showColormap = show;
        ColorMap toShow = new ColorMap(null); //dummy object
        if((currEvent != null && currEvent.getDates().size() > 1) || !show) {
            map.setToggleState(false);
        } else {
            Layer l = findTopColorMap();
            if(l != null && colorMaps.get(l.getIdentifier()) != null)
                toShow = colorMaps.get(l.getIdentifier());
            else
                map.setToggleState(false);
        }
        if(colorMapSub.hasObservers())
            colorMapSub.onNext(toShow);
    }

    public boolean isGMapsAvailable() {
        return map.isGMapsAvailable();
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
        for(Layer l : layer_stack)
            map.removeTile(tileOverlays.get(l.getIdentifier()), l, true);
        tileOverlays.clear();
    }

    //Called when a layer is added or date is changed, verify if data cannot be shown
    public boolean isLayerStartAfterCurrent() {
        for(Layer l : layer_stack)
            if(currentDate.compareTo(Utils.parseISODate(l.getStartDate())) < 0 && l.getVisibility() != Layer.INVISIBLE && currEventPoint == 0)
                return true;
        return false;
    }

    //Find the first layer with a colormap.
    private Layer findTopColorMap() {
        for(Layer l : layer_stack)
            if(l.hasColorMap())
                return l;
        return null;
    }

    //Download and parse the colormap if not already found in the hash table.
    //In rare exceptions, the palette may differ from the id
    private void getColorMapForId(String id, String palette) {
        if(colorMaps.get(id) == null) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

            retrofit.create(ColorMapAPI.class).fetchData(palette)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(colorMap -> {
                    Utils.cleanColorMap(colorMap);
                    colorMaps.put(id, colorMap);
                });
        }
    }
}
