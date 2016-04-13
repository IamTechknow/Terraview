package com.iamtechknow.worldview;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Intent;
import android.content.Loader;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.SearchView;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.iamtechknow.worldview.adapter.LayerAdapter;
import com.iamtechknow.worldview.model.DataWrapper;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.LayerLoader;
import com.iamtechknow.worldview.util.Utils;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class WorldActivity extends Activity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LoaderManager.LoaderCallbacks<DataWrapper> {
    public static final String XML_METADATA = "http://map1.vis.earthdata.nasa.gov/wmts-webmerc/1.0.0/WMTSCapabilities.xml",
                               JSON_METADATA = "https://worldview.sit.earthdata.nasa.gov/config/wv.json";
    public static final int TILE_SIZE = 256, DOWNLOAD_CODE = 0;
    public static final long DELAY_MILLIS = 1000;

    //UI fields
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private NavigationView mNavLayers, mNavDate;
    private RecyclerView mRecyclerView;
    private BottomBar mBottomBar;
    private DatePickerDialog mDateDialog;
    private SearchView mSearchView;

    //Map fields
    private GoogleMap mMap;
    private TileOverlay mCurrOverlay;

    //Worldview Data
    private ArrayList<Layer> layers;
    private Hashtable<String, ArrayList<String>> categories, measurements;
    private int mCurrLayerIdx = 0; //show VIIRS satellite imagery as default
    private Date currentDate;

    //Allow tasks to be delayed
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup UI
        mHandler = new Handler();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.thelayout);
        mNavLayers = (NavigationView) findViewById(R.id.nav_layers);
        mNavDate = (NavigationView) findViewById(R.id.nav_date);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setActionBar(mToolbar);

        //Set default date to be today
        currentDate = new Date(System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        mDateDialog = new DatePickerDialog(WorldActivity.this, mDateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        // Adding menu icon to Toolbar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Setup the RecyclerView with an empty adapter
        mRecyclerView = (RecyclerView) findViewById(R.id.layer_list);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LayerAdapter l = new LayerAdapter();
        l.setItemListener(mLayerAdapter);
        mRecyclerView.setAdapter(l);

        //Setup bottombar
        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.noTopOffset();
        mBottomBar.noNavBarGoodness();
        mBottomBar.setItemsFromMenu(R.menu.menu_bottombar, new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(int resId) {
                switch(resId) {
                    case R.id.action_about:
                        Utils.showAbout(WorldActivity.this);
                        break;
                    case R.id.action_date:
                        mDateDialog.show();
                        break;
                    case R.id.action_explore:

                        break;
                    case R.id.action_layers:

                        break;
                    case R.id.action_search:
                        mSearchView.onActionViewExpanded();
                        break;
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBottomBar.selectTabAtPosition(0, true); //allow item to be selected again
                    }
                }, DELAY_MILLIS);
            }

            @Override
            public void onMenuTabReSelected(int menuItemId) {

            }
        });

        mHandler = new Handler();

        //Request the map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        //Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setMaxWidth(Integer.MAX_VALUE); //ensures search would cover toolbar title

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                mSearchView.onActionViewCollapsed();
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.search:
                mSearchView.onActionViewExpanded();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }

    //Search query appears here
    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SEARCH))
            doGeoCoding(intent.getStringExtra(SearchManager.QUERY));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        showDefaultTiles();

        if(getSharedPreferences(DownloadService.PREFS_FILE, MODE_PRIVATE).getBoolean(DownloadService.PREFS_DB_KEY, false))
            getLoaderManager().initLoader(0, null, this);
        else
            getLayerData();
    }

    @Override
    public void onMapClick(LatLng l) {
        mSearchView.onActionViewCollapsed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Result comes from sending back the PendingIntent in the service
        if(resultCode == RESULT_OK) {
            layers = data.getParcelableArrayListExtra(DownloadService.RESULT_LIST);
            ((LayerAdapter) (mRecyclerView.getAdapter())).insertList(layers);
        }
    }

    @Override
    public Loader<DataWrapper> onCreateLoader(int id, Bundle args) {
        return new LayerLoader(WorldActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<DataWrapper> loader, DataWrapper data) {
        layers = data.layers;
        categories = data.cats;
        measurements = data.measures;

        ((LayerAdapter) (mRecyclerView.getAdapter())).insertList(layers);
    }

    @Override
    public void onLoaderReset(Loader<DataWrapper> loader) {}

    @Override
    public void onBackPressed() { //hide search when on and back pressed
        if(!mSearchView.isIconified())
            mSearchView.onActionViewCollapsed();
        else
            super.onBackPressed();
    }

    private DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            currentDate = c.getTime();
            addTileOverlay(layers.get(mCurrLayerIdx).generateURL(currentDate));
        }
    };

    private LayerAdapter.ItemOnClickListener mLayerAdapter = new LayerAdapter.ItemOnClickListener() {
        @Override
        public void onClick(int idx, boolean checked) {
            if(checked) {
                mCurrLayerIdx = idx;
                addTileOverlay(layers.get(idx).generateURL(currentDate));
            } else
                removeTileOverlay();
            mDrawerLayout.closeDrawers();
        }
    };

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

        removeTileOverlay();
        mCurrOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }

    public void removeTileOverlay() {
        if(mCurrOverlay != null)
            mCurrOverlay.remove();
    }

    /**
     * Show the VIIRS Corrected Reflectance (True Color) overlay for today
     */
    public void showDefaultTiles() {
        Layer l = new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color)", "Suomi NPP / VIIRS", null, "2015-11-24", true);
        addTileOverlay(l.generateURL(currentDate));
    }

    public void getLayerData() {
        //Start an intent to a service that downloads in the background
        //Pass in an PendingIntent which will be used to send back the data
        PendingIntent p = createPendingResult(DOWNLOAD_CODE, new Intent(), 0);
        Intent i = new Intent(this, DownloadService.class)
            .putExtra(DownloadService.URL_XML, XML_METADATA)
            .putExtra(DownloadService.URL_JSON, JSON_METADATA)
            .putExtra(DownloadService.PENDING_RESULT_EXTRA, p);
        startService(i);
    }

    public void doGeoCoding(String query) {
        mSearchView.onActionViewCollapsed(); //hide search
        Geocoder geocoder = new Geocoder(WorldActivity.this);
        try {
            List<Address> address = geocoder.getFromLocationName(query, 1);
            if(address.size() > 0) {
                LatLng coords = new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 8));
            } else //No results, let user retry
                Snackbar.make(mCoordinatorLayout, R.string.no_results, Snackbar.LENGTH_LONG).setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSearchView.onActionViewExpanded();
                    }
                }).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
