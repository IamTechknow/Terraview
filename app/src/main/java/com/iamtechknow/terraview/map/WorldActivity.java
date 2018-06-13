package com.iamtechknow.terraview.map;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.view.View;
import android.widget.DatePicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.iamtechknow.terraview.about.AboutActivity;
import com.iamtechknow.terraview.anim.AnimDialogActivity;
import com.iamtechknow.terraview.colormaps.ColorMapContract;
import com.iamtechknow.terraview.colormaps.ColorMapFragment;
import com.iamtechknow.terraview.events.EventActivity;
import com.iamtechknow.terraview.model.ColorMap;
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
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.iamtechknow.terraview.anim.AnimDialogActivity.*;

public class WorldActivity extends AppCompatActivity implements MapContract.View, OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener, DragAndHideListener, SeekBar.OnSeekBarChangeListener {
    public static final String RESULT_LIST = "list", PREFS_FILE = "settings", PREFS_DB_KEY = "last_update";
    public static final String RESTORE_TIME_EXTRA = "time", RESTORE_LAYER_EXTRA = "layer", RESTORE_EVENT_EXTRA = "event";
    public static final int LAYER_CODE = 1, EVENT_CODE = 2, SECONDS_PER_DAY = 86400000, WEEK = SECONDS_PER_DAY * 7, DEFAULT_HOME_ICON = 0,
                            ANIM_DURATION_MILLS = 250;

    //UI fields
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private DatePickerDialog mDateDialog;
    private CurrLayerAdapter mItemAdapter;
    private ItemTouchHelper mDragHelper;

    //Event widget
    private View event_widget;
    private TextView event_date;
    private SeekBar event_stepper;

    //Colormap Widget;
    private View colormap_widget;
    private ColorMapContract.View colormap;

    //Presenters
    private MapContract.Presenter mapPresenter;

    private boolean eventActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapPresenter = new WorldPresenter(this, new MapInteractorImpl((int) Utils.dPToPixel(getResources(), R.dimen.poly_padding)));

        //Setup UI
        mItemAdapter = new CurrLayerAdapter(this);
        mDrawerLayout = findViewById(R.id.drawer);
        mCoordinatorLayout = findViewById(R.id.thelayout);
        Toolbar mToolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        NavigationView navigationView = findViewById(R.id.nav_menu);
        navigationView.setNavigationItemSelectedListener(this);

        // Adding menu icon to Toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Setup the layer list - initially empty list
        RecyclerView mCurrList = findViewById(R.id.layer_list);
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

    //View model not used here because ViewModel does not survive rotation changes from picker activity
    //and returning to this activity which also is recreated, but this method works.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save current date and layers
        outState.putParcelableArrayList(RESTORE_LAYER_EXTRA, mapPresenter.getCurrLayerStack());
        outState.putLong(RESTORE_TIME_EXTRA, mapPresenter.getCurrDate().getTime());
        outState.putParcelable(RESTORE_EVENT_EXTRA, mapPresenter.getCurrEvent());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mapPresenter.onRestoreInstanceState(savedInstanceState);
        //HACK: The last item from the app tour gets re-checked upon config change, un-check it.
        ((NavigationView) findViewById(R.id.nav_menu)).getMenu().getItem(3).setChecked(false);
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
        if(shouldUseLocalData(getSharedPreferences(PREFS_FILE, MODE_PRIVATE)))
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
                ArrayList<Layer> layer_stack = data.getParcelableArrayListExtra(LayerActivity.RESULT_STACK),
                        toDelete = data.getParcelableArrayListExtra(LayerActivity.DELETE_STACK);
                mapPresenter.setLayersAndUpdateMap(layer_stack, toDelete);
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
    public void onToggleLayer(Layer l, int visibility) {
        mapPresenter.onToggleLayer(l, visibility);
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
                mapPresenter.getRemoteData(WorldActivity.this);
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
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mapPresenter.onEventProgressChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mapPresenter.onEventProgressSelected(seekBar.getProgress());
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

        //Inflate, set up, and animate in event widget
        if(e.getDates().size() > 1) {
            if (event_widget == null) {
                event_widget = getLayoutInflater().inflate(R.layout.event_widget, mCoordinatorLayout, false);
                event_widget.setTranslationY(event_widget.getHeight());
                event_date = event_widget.findViewById(R.id.event_date);
                event_stepper = event_widget.findViewById(R.id.event_stepper);
                event_stepper.setOnSeekBarChangeListener(this);
                mCoordinatorLayout.addView(event_widget);
            }

            event_date.setText(Utils.parseDateForDialog(Utils.parseISODate(e.getDates().get(0))));
            event_stepper.setMax(e.getDates().size() - 1);
            event_stepper.setProgress(0);
            event_widget.animate().translationY(0).setDuration(ANIM_DURATION_MILLS).start();
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

        //Animate out event widget
        if(event_widget != null)
            event_widget.animate().translationY(event_widget.getHeight()).setDuration(ANIM_DURATION_MILLS).start();
    }

    @Override
    public void updateEventDateText(String date) {
        event_date.setText(date);
    }

    @Override
    public void warnUserAboutActiveLayers() {
        Snackbar bar = Snackbar.make(mCoordinatorLayout, R.string.start_warning, Snackbar.LENGTH_LONG);
        if(mapPresenter.isVIIRSActive())
            bar.setAction(R.string.fix, v -> mapPresenter.fixVIIRS());
        bar.show();
    }

    @Override
    public void warnNoLayersToAnim() {
        Snackbar.make(mCoordinatorLayout, getString(R.string.anim_warning_open), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showChangedEventDate(String date) {
        Snackbar.make(mCoordinatorLayout, getString(R.string.event_date, date), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showColorMap(ColorMap show) {
        if(show == null) { //hide UI
            if(colormap_widget != null)
                colormap_widget.animate().translationY(colormap_widget.getHeight()).setDuration(ANIM_DURATION_MILLS).start();
        } else {
            if(colormap_widget == null) {
                colormap_widget = getLayoutInflater().inflate(R.layout.color_map_widget, mCoordinatorLayout, false);
                colormap = colormap_widget.findViewById(R.id.color_map_palette);
                mCoordinatorLayout.addView(colormap_widget);

                //HACK: Need to invalidate it again to make the canvas appear.
                //There is an abrupt translation when animating for the first time
                Observable.intervalRange(0, 1, 0, 20, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> colormap.setColorMapData(show));
            }

            colormap.setColorMapData(show);
            colormap_widget.animate().translationY(0).setDuration(ANIM_DURATION_MILLS).start();
        }
    }

    //Has it been a week since layer data was last downloaded?
    private boolean shouldUseLocalData(SharedPreferences prefs) {
        return System.currentTimeMillis() - prefs.getLong(PREFS_DB_KEY, 0) < WEEK;
    }
}
