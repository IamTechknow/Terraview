package com.iamtechknow.terraview.map;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.widget.DatePicker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.iamtechknow.terraview.about.AboutActivity;
import com.iamtechknow.terraview.anim.AnimDialogActivity;
import com.iamtechknow.terraview.colormaps.ColorMapFragment;
import com.iamtechknow.terraview.events.EventActivity;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.picker.LayerActivity;
import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.CurrLayerAdapter;
import com.iamtechknow.terraview.adapter.ItemTouchHelperCallback;
import com.iamtechknow.terraview.adapter.DragAndHideListener;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.FeatureDiscovery;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;

import static com.iamtechknow.terraview.anim.AnimDialogActivity.*;

public class WorldActivity extends AppCompatActivity implements MapView, OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener, DragAndHideListener {
    public static final String RESULT_LIST = "list", PREFS_FILE = "settings", PREFS_DB_KEY = "have_db";
    public static final String RESTORE_TIME_EXTRA = "time", RESTORE_LAYER_EXTRA = "layer";
    public static final int LAYER_CODE = 1, EVENT_CODE = 2, SECONDS_PER_DAY = 24*60*60*1000, DEFAULT_HOME_ICON = 0;

    //UI fields
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private DatePickerDialog mDateDialog;
    private CurrLayerAdapter mItemAdapter;
    private ItemTouchHelper mDragHelper;

    //Presenters
    private MapPresenter mapPresenter;

    private boolean eventActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapPresenter = new WorldPresenter((int) Utils.dPToPixel(getResources(), R.dimen.poly_padding));
        mapPresenter.attachView(this);

        //Setup UI
        mItemAdapter = new CurrLayerAdapter(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.thelayout);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_menu);
        navigationView.setNavigationItemSelectedListener(this);

        // Adding menu icon to Toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Setup the layer list - initally empty list
        RecyclerView mCurrList = (RecyclerView) findViewById(R.id.layer_list);
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
    protected void onDestroy() {
        super.onDestroy();
        mapPresenter.detachView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save current date and layers
        outState.putParcelableArrayList(RESTORE_LAYER_EXTRA, mapPresenter.getCurrLayerStack());
        outState.putLong(RESTORE_TIME_EXTRA, mapPresenter.getCurrDate().getTime());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mapPresenter.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                if(eventActive)
                    mapPresenter.onClearEvent();
                else
                    mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_layerinfo:
                mapPresenter.presentColorMaps();
                break;
            case R.id.action_about:
                mapPresenter.presentAbout();
                break;
            case R.id.action_date:
                mDateDialog.show();
                break;
            case R.id.action_anim:
                mapPresenter.presentAnimDialog();
                break;
            case R.id.action_layers:
                mapPresenter.chooseLayers();
                break;
            case R.id.action_layersettings:
                mDrawerLayout.openDrawer(GravityCompat.END);
                break;
            case R.id.action_help:
                mapPresenter.presentHelp();
                break;
            case R.id.action_events:
                mapPresenter.presentEvents();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(getSharedPreferences(PREFS_FILE, MODE_PRIVATE).getBoolean(PREFS_DB_KEY, false))
            mapPresenter.getLocalData(getSupportLoaderManager(), this);
        else { //Check internet access to get layer data or set up receiver, also start tour for first timers
            if(Utils.isOnline(this)) {
                mapPresenter.getRemoteData(this);
                Snackbar.make(mCoordinatorLayout, R.string.tour_new, Snackbar.LENGTH_INDEFINITE).setAction(R.string.start_tour, view -> mapPresenter.presentHelp()).show();
            } else {
                Snackbar.make(mCoordinatorLayout, R.string.internet, Snackbar.LENGTH_LONG).show();
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(connectReceiver, filter);
            }
        }
        mapPresenter.onMapReady(googleMap);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case EVENT_CODE:
                if(resultCode == RESULT_OK)
                    mapPresenter.presentEvent(data.getParcelableExtra(EventActivity.EVENT_EXTRA));
                break;
            default:
                ArrayList<Layer> layer_stack = data.getParcelableArrayListExtra(LayerActivity.RESULT_STACK);
                mapPresenter.setLayersAndUpdateMap(layer_stack);
        }
    }

    @Override
    public void onBackPressed() {
        if(eventActive)
            mapPresenter.onClearEvent();
        else
            super.onBackPressed();
    }

    //Reload layers on date change. Cut off time, dates should always be midnight
    private DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            Utils.getCalendarMidnightTime(c);
            mapPresenter.onDateChanged(c.getTime());
        }
    };

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        //Called when drag handler is touched, start drag
        mDragHelper.startDrag(viewHolder);
    }

    @Override
    public void onToggleLayer(Layer l, boolean hide) {
        mapPresenter.onToggleLayer(l, hide);
    }

    @Override
    public void onSwapNeeded(int i, int i_new) {
        mapPresenter.onSwapNeeded(i, i_new);
    }

    @Override
    public void onLayerSwiped(int position, Layer l) {
        mapPresenter.onLayerSwiped(position, l);
    }

    /**
     * Used to indicate internet connectivity is available to load Worldview and GIBS data.
     * Not used when internet is already available or data already obtained
     */
    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) && Utils.isOnline(context)) {
                mapPresenter.getRemoteData(context);
                Snackbar.make(mCoordinatorLayout, R.string.tour_new, Snackbar.LENGTH_INDEFINITE).setAction(R.string.start_tour, view -> mapPresenter.presentHelp()).show();
                unregisterReceiver(this);
            }
        }
    };

    @Override
    public void setDateDialog(long today) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(today);

        mDateDialog = new DatePickerDialog(this, mDateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        mDateDialog.getDatePicker().setMaxDate(today + SECONDS_PER_DAY); //HACK: Increase max date to select current day
    }

    @Override
    public void updateDateDialog(long currDate) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(currDate);
        mDateDialog.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void setLayerList(ArrayList<Layer> stack) {
        mItemAdapter.insertList(stack);
    }

    @Override
    public void showColorMaps() {
        BottomSheetDialogFragment frag = new ColorMapFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ColorMapFragment.COLORMAP_ARG, mapPresenter.getCurrLayerStack());

        frag.setArguments(args);
        frag.show(getSupportFragmentManager(), frag.getTag());
    }

    @Override
    public void showAnimDialog() {
        Intent anim_i = new Intent(WorldActivity.this, AnimDialogActivity.class)
                .putExtra(AnimDialogActivity.ANIM_ARG, Utils.parseDateForDialog(mapPresenter.getCurrDate()))
                .putParcelableArrayListExtra(LAYER_EXTRA, mapPresenter.getCurrLayerStack());
        startActivity(anim_i);
    }

    @Override
    public void showPicker() {
        Intent i = new Intent(WorldActivity.this, LayerActivity.class)
                .putParcelableArrayListExtra(RESULT_LIST, mapPresenter.getCurrLayerStack());
        startActivityForResult(i, LAYER_CODE);
    }

    @Override
    public void showAbout() {
        startActivity(new Intent(WorldActivity.this, AboutActivity.class));
    }

    /**
     * Implements feature discovery of Material Design as a tour of the app
     */
    @Override
    public void showHelp() {
        FeatureDiscovery.guidedTour(this);
    }

    @Override
    public void showEvents() {
        Intent i = new Intent(WorldActivity.this, EventActivity.class);
        startActivityForResult(i, EVENT_CODE);
    }

    @Override
    public void showEvent(Event e) {
        eventActive = true;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(DEFAULT_HOME_ICON);
            actionBar.setTitle(e.getTitle());
        }
    }

    @Override
    public void clearEvent() {
        eventActive = false;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setTitle(R.string.app_name);
        }
    }


    @Override
    public void warnUserAboutActiveLayers() {
        Snackbar bar = Snackbar.make(mCoordinatorLayout, R.string.start_warning, Snackbar.LENGTH_LONG);
        //if(mapPresenter.isVIIRSActive())
        //    bar.setAction(R.string.fix, v -> mapPresenter.fixVIIRS()); //FIXME:Preserve unmodified tile overlays first
        bar.show();
    }
}
