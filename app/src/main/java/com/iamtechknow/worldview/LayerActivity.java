package com.iamtechknow.worldview;

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

import com.iamtechknow.worldview.fragment.LayerPageFragment;
import com.iamtechknow.worldview.model.Layer;

import java.util.ArrayList;

import rx.functions.Action1;

public class LayerActivity extends AppCompatActivity {
    //Constants for RxBus events and Intent
    public static final int LAYER_QUEUE = 0, MEASURE_TAB = 1, LAYER_TAB = 2, LAYER_DEQUE = 3;
    public static final String RESULT_STACK = "result";

    //UI handling
    private TabLayout mTabLayout;
    private RxBus _rxBus;

    //List for layers to be displayed handled as a stack
    private ArrayList<Layer> result;

    // This is better done with a DI Library like Dagger
    public RxBus getRxBusSingleton() {
        if (_rxBus == null)
            _rxBus = new RxBus();

        return _rxBus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup tabs, view pager, fragments
        setContentView(R.layout.activity_layer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//Create arguments for each fragments which will be used when they're created
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        Adapter adapter = new Adapter(getSupportFragmentManager());
        LayerPageFragment frag1 = new LayerPageFragment(), frag2 = new LayerPageFragment(), frag3 = new LayerPageFragment();
        Bundle extra1 = new Bundle(), extra2 = new Bundle(), extra3 = new Bundle();
        result = new ArrayList<>();
        extra1.putInt(LayerPageFragment.EXTRA_ARG, LayerPageFragment.ARG_CAT);
        extra2.putInt(LayerPageFragment.EXTRA_ARG, LayerPageFragment.ARG_MEASURE);
        extra3.putInt(LayerPageFragment.EXTRA_ARG, LayerPageFragment.ARG_LAYER);
        frag1.setArguments(extra1);
        frag2.setArguments(extra2);
        frag3.setArguments(extra3);

        adapter.addFragment(frag1, "Categories"); //Add the fragment and its tab title
        adapter.addFragment(frag2, "Measurements");
        adapter.addFragment(frag3, "Layers");
        viewPager.setAdapter(adapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);

        _rxBus = getRxBusSingleton();
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
                    if(event instanceof LayerPageFragment.TapEvent)
                        //If we have an event due to a button press then go to the tab
                        //And in the fragment that is also listening load the right data!
                        switch(((LayerPageFragment.TapEvent) event).getTab()) {
                            case LAYER_TAB:
                                mTabLayout.getTabAt(LayerPageFragment.ARG_LAYER).select();
                                break;

                            case MEASURE_TAB:
                                mTabLayout.getTabAt(LayerPageFragment.ARG_MEASURE).select();
                                break;

                            case LAYER_DEQUE:
                                result.remove(((LayerPageFragment.TapEvent) event).getLayer());
                                break;

                            default: //layer queue
                                result.add(((LayerPageFragment.TapEvent) event).getLayer());
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
