package com.iamtechknow.terraview.map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;
import com.iamtechknow.terraview.Injection;
import com.iamtechknow.terraview.anim.AnimPresenter;
import com.iamtechknow.terraview.anim.AnimView;
import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.data.LocalDataSource;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.iamtechknow.terraview.map.WorldActivity.*;
import static com.iamtechknow.terraview.anim.AnimDialogActivity.*;

public class WorldPresenter implements MapPresenter, AnimPresenter, DataSource.LoadCallback {
    private static final float Z_OFFSET = 5.0f, BASE_Z_OFFSET = -50.0f; //base layers cannot cover overlays
    private static final int DAY_IN_MILLS = 24*60*60*1000, DAYS_IN_WEEK = 7, DAYS_IN_MONTH = 30, DAYS_IN_YEAR = 365, MIN_FRAMES = 1;

    private WeakReference<MapView> mapViewRef;
    private WeakReference<AnimView> animViewRef;
    private DataSource dataSource;

    //Used to let presenter know to restore state after map loads
    private boolean isRestoring;

    //Worldview data
    private MapInteractor map;
    private ArrayList<Layer> layer_stack;
    private ArrayList<TileOverlay> mCurrLayers;
    private Date currentDate;

    //Animation data
    private Disposable animSub;
    private int interval, speed;
    private boolean loop, saveGif, animRunning, animInSession;
    private String startDate, endDate;
    private Date start, end;
    private Calendar currAnimCal;
    private int maxFrames, currFrame, delay;
    private ArrayList<ArrayList<TileOverlay>> animCache;

    public WorldPresenter() {
        mCurrLayers = new ArrayList<>();
        layer_stack = new ArrayList<>();
        animCache = new ArrayList<>();

        currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        Utils.getCalendarMidnightTime(c);
        currentDate = c.getTime();

        speed = DEFAULT_SPEED;
    }

    @Override
    public void attachView(MapView v) {
        mapViewRef = new WeakReference<>(v);
        mapViewRef.get().setDateDialog(currentDate.getTime());
    }

    @Override
    public void attachView(AnimView v) {
        animViewRef = new WeakReference<>(v);
    }

    @Override
    public void detachView() {
        if(mapViewRef != null) {
            mapViewRef.clear();
            mapViewRef = null;
        }

        if(animViewRef != null) {
            animViewRef.clear();
            animViewRef = null;
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
    public void newAnimation() {
        if(getAnimView() != null)
            getAnimView().showAnimDialog();
    }

    @Override
    public void setAnimation(String start, String end, int interval, int speed, boolean loop) {
        startDate = start;
        endDate = end;
        this.interval = interval;
        this.speed = speed;
        this.loop = loop;

        initAnim();
        animInSession = true;
        if(getAnimView() != null)
            getAnimView().setAnimButton(true);
    }

    @Override
    public Bundle getAnimationSettings() {
        Bundle b = new Bundle();
        if(start != null && end != null) { //no dialog restore on first animation
            b.putString(START_EXTRA, Utils.parseDateForDialog(start));
            b.putString(END_EXTRA, Utils.parseDateForDialog(end));
        }
        b.putBoolean(LOOP_EXTRA, loop);
        b.putInt(INTERVAL_EXTRA, interval);
        b.putInt(SPEED_EXTRA, speed);

        return b;
    }

    @Override
    public void run() {
        if(!animRunning)
            startAnim();
    }

    @Override
    public boolean isRunning() {
        return animInSession;
    }

    /**
     * Called by the view or an animation end to stop with or without the possibility to restore.
     * If terminated, the layer stack is restored, otherwise the play button may be pressed again.
     */
    @Override
    public void stop(boolean terminate) {
        if(animRunning) {
            animRunning = false;
            stopAnimTimer();
        }

        if(terminate && animInSession && getAnimView() != null) {
            animInSession = false;
            getAnimView().setAnimButton(false);
            setLayersAndUpdateMap(layer_stack); //restore layers FIXME: takes longer with long animations
            for(ArrayList<TileOverlay> list : animCache)
                for(TileOverlay t : list)
                    t.remove();
        }
    }

    private MapView getMapView() {
        return mapViewRef == null ? null : mapViewRef.get();
    }

    private AnimView getAnimView() {
        return animViewRef == null ? null : animViewRef.get();
    }

    /**
     * Called when an animation ends but can be restored by pressing the play button or a loop.
     */
    private void stopOrRepeat() {
        if(loop)
            startAnim();
        else {
            stop(false);
            restoreAnimTiles();
        }
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

    /**
     * Check input for the animation, that is calculate how many frames there are to animate,
     * and from there modify the start/end dates based on the start/end dates of the layers
     */
    private void initAnim() {
        delay = 1000 / speed;
        start = Utils.parseISODate(startDate);
        end = Utils.parseISODate(endDate);
        long delta_in_days = (end.getTime() - start.getTime()) / DAY_IN_MILLS;

        switch(interval) {
            case DAY:
                maxFrames = (int) delta_in_days;
                break;
            case WEEK:
                maxFrames = (int) delta_in_days / DAYS_IN_WEEK;
                break;
            case MONTH:
                maxFrames = (int) delta_in_days / DAYS_IN_MONTH;
                break;

            default: //year
                maxFrames = (int) delta_in_days / DAYS_IN_YEAR;
        }
        if(maxFrames == MIN_FRAMES)
            loop = false;

        currAnimCal = Calendar.getInstance();
        currAnimCal.setTimeZone(TimeZone.getTimeZone("UTC"));
        initAnimCache();
    }

    /**
     * Populate the animation tile overlays by adding all overlays for a specific date,
     * putting it into the list, incrementing the calendar by the interval, and
     * repeating for all frames
     */
    private void initAnimCache() {
        animCache.clear();
        currAnimCal.setTime(start);

        for(int i = 0; i > -maxFrames - 1; i--) {
            ArrayList<TileOverlay> temp = new ArrayList<>();

            for(Layer l: layer_stack)
                temp.add(map.addTileForAnimation(l, i, Utils.parseDate(currAnimCal.getTime())));
            animCache.add(temp);

            incrementCal(currAnimCal);
        }
    }

    @Override
    public void onNextFrame() {
        //Here we don't need the date, just an index since they are in order
        if (currFrame == maxFrames)
            stopOrRepeat();
        else {
            for (TileOverlay t : animCache.get(currFrame))
                t.setVisible(false);

            currFrame++;
        }
    }

    private void startAnim() {
        currFrame = 0;
        currAnimCal.setTime(start);

        //Create an observable to wait for the tiles to load, then process each animation frame
        if(!animRunning) {
            animRunning = true;
            animSub = Observable.interval(delay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> onNextFrame());
        } else  //Restore tiles
            restoreAnimTiles();
    }

    /**
     * Stop the animation, cancel the timer and delete the timer task, which must be remade again.
     */
    private void stopAnimTimer() {
        animSub.dispose();
    }

    private void restoreAnimTiles() {
        for(ArrayList<TileOverlay> list : animCache)
            for(TileOverlay t : list)
                t.setVisible(true);
    }

    //Increment calendar depending on interval
    private void incrementCal(Calendar currAnimCal) {
        switch (interval) {
            case DAY:
                currAnimCal.set(Calendar.DAY_OF_MONTH, currAnimCal.get(Calendar.DAY_OF_MONTH) + 1);
                break;
            case WEEK:
                currAnimCal.set(Calendar.WEEK_OF_MONTH, currAnimCal.get(Calendar.WEEK_OF_MONTH) + 1);
                break;
            case MONTH:
                currAnimCal.set(Calendar.MONTH, currAnimCal.get(Calendar.MONTH) + 1);
                break;
            default: //year
                currAnimCal.set(Calendar.YEAR, currAnimCal.get(Calendar.YEAR) + 1);
        }
    }
}
