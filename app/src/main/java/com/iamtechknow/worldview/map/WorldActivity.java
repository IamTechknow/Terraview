package com.iamtechknow.worldview.map;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.Toolbar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.iamtechknow.worldview.DownloadService;
import com.iamtechknow.worldview.picker.LayerActivity;
import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.adapter.CurrLayerAdapter;
import com.iamtechknow.worldview.adapter.ItemTouchHelperCallback;
import com.iamtechknow.worldview.adapter.DragAndHideListener;
import com.iamtechknow.worldview.model.DataWrapper;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;
import com.iamtechknow.worldview.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class WorldActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<DataWrapper>,
        NavigationView.OnNavigationItemSelectedListener, DragAndHideListener {
    public static final String XML_METADATA = "http://map1.vis.earthdata.nasa.gov/wmts-webmerc/1.0.0/WMTSCapabilities.xml",
                               JSON_METADATA = "https://worldview.earthdata.nasa.gov/config/wv.json",
                               RESULT_LIST = "list";
    public static final int TILE_SIZE = 256, DOWNLOAD_CODE = 0, LAYER_CODE = 1, SECONDS_PER_DAY = 24*60*60*1000;
    public static final float Z_OFFSET = 5.0f, BASE_Z_OFFSET = -50.0f; //base layers cannot cover overlays

    //UI fields
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private NavigationView mNavLeft, mNavLayers;
    private DatePickerDialog mDateDialog;
    private RecyclerView mCurrList;
    private CurrLayerAdapter mItemAdapter;
    private ItemTouchHelper mDragHelper;

    //Map fields
    private GoogleMap mMap;

    //Worldview Data
    //layer_stack is used as the data source for the right hand drawer
    private ArrayList<Layer> layers, layer_stack;
    private ArrayList<TileOverlay> mCurrLayers;
    private Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup UI
        mCurrLayers = new ArrayList<>();
        layer_stack = new ArrayList<>();
        mItemAdapter = new CurrLayerAdapter(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.thelayout);
        mNavLeft = (NavigationView) findViewById(R.id.nav_menu);
        mNavLayers = (NavigationView) findViewById(R.id.nav_layers);
        mCurrList = (RecyclerView) findViewById(R.id.layer_list);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setActionBar(mToolbar);
        mNavLeft.setNavigationItemSelectedListener(this);

        //Set default date to be today, midnight time for consistency
        currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        mDateDialog = new DatePickerDialog(this, mDateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        mDateDialog.getDatePicker().setMaxDate(currentDate.getTime() + SECONDS_PER_DAY); //HACK: Increase max date to select current day

        // Adding menu icon to Toolbar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Setup the layer list - initally empty list
        mCurrList.setItemAnimator(new DefaultItemAnimator());
        mCurrList.setLayoutManager(new LinearLayoutManager(this));
        mCurrList.setAdapter(mItemAdapter);
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mItemAdapter); //Attach drag callbacks
        mDragHelper = new ItemTouchHelper(callback);
        mDragHelper.attachToRecyclerView(mCurrList);

        //Request the map - control flow goes to onMapReady()
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Utils.showAbout(WorldActivity.this);
                break;
            case R.id.action_date:
                mDateDialog.show();
                break;
            case R.id.action_layers:
                Intent i = new Intent(WorldActivity.this, LayerActivity.class).putParcelableArrayListExtra(RESULT_LIST, layer_stack);
                startActivityForResult(i, LAYER_CODE);
                break;
        }
        mDrawerLayout.closeDrawers();
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(9.0f);

        showDefaultTiles();

        if(getSharedPreferences(DownloadService.PREFS_FILE, MODE_PRIVATE).getBoolean(DownloadService.PREFS_DB_KEY, false))
            getSupportLoaderManager().initLoader(0, null, this);
        else { //Check internet access to get layer data or set up receiver
            if(Utils.isOnline(this))
                getLayerData();
            else {
                Snackbar.make(mCoordinatorLayout, R.string.internet, Snackbar.LENGTH_LONG).show();
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(connectReceiver, filter);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Result comes from sending back the PendingIntent in the service
        if(requestCode == DOWNLOAD_CODE && resultCode == RESULT_OK)
            layers = data.getParcelableArrayListExtra(DownloadService.RESULT_LIST);
        else if(requestCode == LAYER_CODE) {
            layer_stack = data.getParcelableArrayListExtra(LayerActivity.RESULT_STACK);
            mItemAdapter.insertList(layer_stack);
            removeAllTileOverlays();
            for(Layer l: layer_stack)
                addTileOverlay(l);
            initZOffsets();
        }
    }

    @Override
    public Loader<DataWrapper> onCreateLoader(int id, Bundle args) {
        return new LayerLoader(WorldActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<DataWrapper> loader, DataWrapper data) {
        layers = data.layers;
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}

    //Reload layers on date change. Cut off time, dates should always be midnight
    private DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            currentDate = c.getTime();
            removeAllTileOverlays();
            for(Layer l: layer_stack)
                addTileOverlay(l);
            initZOffsets();
        }
    };

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        //Called when drag handler is touched, start drag
        mDragHelper.startDrag(viewHolder);
    }

    @Override
    public void onToggleLayer(Layer l, boolean hide) {
        int pos = layer_stack.indexOf(l);
        mCurrLayers.get(pos).setVisible(hide);
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

    @Override
    public void onLayerSwiped(int position) {
        TileOverlay temp = mCurrLayers.remove(position);
        temp.remove();

        //Fix Z-Order of other overlays
        for(int i = 0; i < mCurrLayers.size(); i++) {
            TileOverlay t = mCurrLayers.get(0);
            t.setZIndex(t.getZIndex() - Z_OFFSET);
        }
    }

    public void addTileOverlay(final Layer layer) {
        //Make a tile overlay
        UrlTileProvider provider = new UrlTileProvider(TILE_SIZE, TILE_SIZE) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String s = String.format(Locale.US, layer.generateURL(currentDate), zoom, y, x);
                URL url = null;

                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                return url;
            }
        };

        mCurrLayers.add(mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider)));
    }

    //Remove all tile overlays, used to replace with new set
    //Remember that the default GMaps tile remains
    public void removeAllTileOverlays() {
        for(TileOverlay t : mCurrLayers)
            t.remove();
        mCurrLayers.clear();
    }

    /**
     * Show the VIIRS Corrected Reflectance (True Color) overlay for today and coastlines
     */
    public void showDefaultTiles() {
        Layer l = new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, VIIRS, SNPP)", "Suomi NPP / VIIRS", null, "2015-11-24", null, true),
                coastline = new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, false);
        layer_stack.add(coastline);
        layer_stack.add(l);
        mItemAdapter.insertList(layer_stack);

        addTileOverlay(coastline);
        addTileOverlay(l);
        initZOffsets();
    }

    private void initZOffsets() {
        //After tile overlays added, set the Z-Order from the default of 0.0
        //Layers at the top of the list have the highest Z-order
        //Base layers will be not affected to avoid covering overlays
        for(int i = 0; i < mCurrLayers.size(); i++)
            if(layer_stack.get(i).isBaseLayer())
                mCurrLayers.get(i).setZIndex(BASE_Z_OFFSET);
            else
                mCurrLayers.get(i).setZIndex(Z_OFFSET * (mCurrLayers.size() - 1 - i));
    }

    private void getLayerData() {
        //Start an intent to a service that downloads in the background
        //Pass in an PendingIntent which will be used to send back the data
        PendingIntent p = createPendingResult(DOWNLOAD_CODE, new Intent(), 0);
        Intent i = new Intent(this, DownloadService.class)
            .putExtra(DownloadService.URL_XML, XML_METADATA)
            .putExtra(DownloadService.URL_JSON, JSON_METADATA)
            .putExtra(DownloadService.PENDING_RESULT_EXTRA, p);
        startService(i);
    }

    /**
     * Used to indicate internet connectivity is available to load Worldview and GIBS data.
     * Not used when internet is already available or data already obtained
     */
    BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if(Utils.isOnline(context)) {
                    getLayerData();
                    unregisterReceiver(connectReceiver);
                }
            }
        }
    };
}
