package com.iamtechknow.terraview.picker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.map.WorldActivity;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.TapEvent;

import java.util.ArrayList;

import rx.functions.Action1;

public class LayerActivity extends AppCompatActivity {
    //Constants for RxBus events and Intent
    public static final int SELECT_MEASURE_TAB = 1, SELECT_LAYER_TAB = 2;
    public static final String RESULT_STACK = "result", LAYER_EXTRA = "layer";

    //UI handling
    private TabLayout mTabLayout;
    private RxBus _rxBus;

    //Reference to layer stack from map
    private ArrayList<Layer> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//Create arguments for each fragments which will be used when they're created
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        Adapter adapter = new Adapter(getSupportFragmentManager());
        NonLayerFragment frag1 = new NonLayerFragment(), frag2 = new NonLayerFragment();
        LayerFragment frag3 = new LayerFragment();
        Bundle extra1 = new Bundle(), extra2 = new Bundle(), extra3 = new Bundle();

        extra1.putBoolean(NonLayerFragment.EXTRA_ARG, true);
        extra2.putBoolean(NonLayerFragment.EXTRA_ARG, false);
        frag1.setArguments(extra1);
        frag2.setArguments(extra2);

        //Check if intent extras are received, which would be the layer data from WorldActivity
        //List for layers to be displayed handled as a stack
        if(getIntent() != null)
            result = getIntent().getParcelableArrayListExtra(WorldActivity.RESULT_LIST);
        else
            result = savedInstanceState.getParcelableArrayList(LAYER_EXTRA);

        if(result != null) //if it exists, send to data adapter in layer tab
            extra3.putParcelableArrayList(RESULT_STACK, result);
        frag3.setArguments(extra3);

        adapter.addFragment(frag1, "Categories"); //Add the fragment and its tab title
        adapter.addFragment(frag2, "Measurements");
        adapter.addFragment(frag3, "Layers");
        viewPager.setAdapter(adapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);

        _rxBus = RxBus.getInstance();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(LAYER_EXTRA, result);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //back button pressed
                setResult();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	/**
	 * Subscribes to the RxBus with a response to new events.
	 * The Activity just changes the current tab shown depending on the event
	 */
    @Override
    public void onStart() {
        super.onStart();
        _rxBus.toObserverable().subscribe(this::handleEvent);
    }

    @Override
    public void onBackPressed() {
        setResult();
        super.onBackPressed();
    }

    /**
     * Put the layer stack in a bundle to be returned. If the layer fragment is never created
     * (user doesn't go to layer tab) result is unchanged, otherwise it may be changed by LayerPresenter
     */
    private void setResult() {
        Bundle b = new Bundle();
        b.putParcelableArrayList(RESULT_STACK, result);
        Intent i = new Intent().putExtras(b);

        setResult(RESULT_OK, i);
    }

    /**
     * If we have an event due to a button press then go to the tab
     * and in the fragment that is also listening load the right data.
     * @param event Object from the RxBus
     */
    private void handleEvent(Object event) {
        if(event instanceof TapEvent)
            switch(((TapEvent) event).getTab()) {
                case SELECT_LAYER_TAB:
                    mTabLayout.getTabAt(SELECT_LAYER_TAB).select();
                    break;

                case SELECT_MEASURE_TAB:
                    mTabLayout.getTabAt(SELECT_MEASURE_TAB).select();
                    break;
            }
    }

	/**
	 * The Adapter provides the relevant information needed to setup the ViewPager's data source and views.
	 */
    static class Adapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> mFragmentList = new ArrayList<>();
        private final ArrayList<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
