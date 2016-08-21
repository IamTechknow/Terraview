package com.iamtechknow.worldview.picker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iamtechknow.worldview.R;
import com.iamtechknow.worldview.map.WorldActivity;
import com.iamtechknow.worldview.model.Layer;
import com.iamtechknow.worldview.model.TapEvent;

import java.util.ArrayList;

import rx.functions.Action1;

public class LayerActivity extends AppCompatActivity {
    //Constants for RxBus events and Intent
    public static final int SELECT_MEASURE_TAB = 1, SELECT_LAYER_TAB = 2;
    public static final String RESULT_STACK = "result";

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
        result = getIntent().getParcelableArrayListExtra(WorldActivity.RESULT_LIST);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //back button pressed
                if (NavUtils.getParentActivityName(this) != null) {
                    setResult();
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	/**
	 * Subscribes to the RxBus with a response to new events.
	 * The Activity can find out what layers are selected, and what tab should be shown when an item is tapped.
	 */
    @Override
    public void onStart() {
        super.onStart();
        _rxBus.toObserverable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if(event instanceof TapEvent)
                        //If we have an event due to a button press then go to the tab
                        //And in the fragment that is also listening load the right data!
                        switch(((TapEvent) event).getTab()) {
                            case SELECT_LAYER_TAB:
                                mTabLayout.getTabAt(SELECT_LAYER_TAB).select();
                                break;

                            case SELECT_MEASURE_TAB:
                                mTabLayout.getTabAt(SELECT_MEASURE_TAB).select();
                                break;
                        }
                }
            });
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
