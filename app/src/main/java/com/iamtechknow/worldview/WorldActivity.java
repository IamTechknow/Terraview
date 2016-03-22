package com.iamtechknow.worldview;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class WorldActivity extends Activity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<ArrayList<Layer>> {
    public static final String XML_METADATA = "http://map1.vis.earthdata.nasa.gov/wmts-webmerc/1.0.0/WMTSCapabilities.xml",
                               JSON_METADATA = "https://worldview.sit.earthdata.nasa.gov/config/wv.json";
    public static final String TILE_URL = "http://map1.vis.earthdata.nasa.gov/wmts-webmerc/MODIS_Terra_Aerosol/default/2016-03-02/GoogleMapsCompatible_Level6/%d/%d/%d.png";
    public static final int TILE_SIZE = 256, DOWNLOAD_CODE = 0;

    //UI fields
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private NavigationView mNavLayers, mNavDate;

    //Map fields
    private GoogleMap mMap;
    private TileOverlay mCurrOverlay;

    //Worldview Data
    private ArrayList<Layer> layers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup UI
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.thelayout);
        mNavLayers = (NavigationView) findViewById(R.id.nav_layers);
        mNavDate = (NavigationView) findViewById(R.id.nav_date);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setActionBar(mToolbar);

        // Adding menu icon to Toolbar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Request the map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        addTileOverlay(TILE_URL);

        if(getSharedPreferences(DownloadService.PREFS_FILE, MODE_PRIVATE).getBoolean(DownloadService.PREFS_DB_KEY, false))
            getLoaderManager().initLoader(0, null, this);
        else
            getLayerData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Result comes from sending back the PendingIntent in the service
        if(resultCode == RESULT_OK) {
            layers = data.getParcelableArrayListExtra(DownloadService.RESULT_LIST);
            for(Layer l: layers)
                Log.i("System.out", l.getTitle());
        }
    }

    @Override
    public Loader<ArrayList<Layer>> onCreateLoader(int id, Bundle args) {
        return new LayerLoader(WorldActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Layer>> loader, ArrayList<Layer> lists) {
        layers = lists;
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Layer>> loader) {

    }

    public void addTileOverlay(final String url) {
        //Make a tile overlay
        UrlTileProvider provider = new UrlTileProvider(TILE_SIZE, TILE_SIZE) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String s = String.format(Locale.US, url, zoom, y, x);
                URL url = null;

                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                return url;
            }
        };

        if(mCurrOverlay != null)
            mCurrOverlay.remove();
        mCurrOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }

    public void getLayerData() {
        //Start an intent to a service that downloads in the background
        //Pass in an PendingIntent which will be used to send back the data
        PendingIntent p = createPendingResult(DOWNLOAD_CODE, new Intent(), 0);
        Intent i = new Intent(this, DownloadService.class)
            .putExtra(DownloadService.URL_EXTRA, XML_METADATA)
            .putExtra(DownloadService.PENDING_RESULT_EXTRA, p);
        startService(i);
    }
}
