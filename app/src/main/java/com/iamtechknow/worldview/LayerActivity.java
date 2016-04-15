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

import java.util.ArrayList;

public class LayerActivity extends AppCompatActivity {
    public static final String ACTION_SWITCH_CATEGORY = "com.iamtechknow.worldview.CATEGORY",
                               ACTION_SWITCH_MEASURE = "com.iamtechknow.worldview.MEASURE";

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup tabs, view pager, fragments
        setContentView(R.layout.activity_layer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        Adapter adapter = new Adapter(getSupportFragmentManager());
        LayerPageFragment frag1 = new LayerPageFragment(), frag2 = new LayerPageFragment(), frag3 = new LayerPageFragment();
        Bundle extra1 = new Bundle(), extra2 = new Bundle(), extra3 = new Bundle();
        extra1.putInt(LayerPageFragment.EXTRA_ARG, LayerPageFragment.ARG_CAT);
        extra2.putInt(LayerPageFragment.EXTRA_ARG, LayerPageFragment.ARG_MEASURE);
        extra3.putInt(LayerPageFragment.EXTRA_ARG, LayerPageFragment.ARG_LAYER);
        frag1.setArguments(extra1);
        frag2.setArguments(extra2);
        frag3.setArguments(extra3);

        adapter.addFragment(frag1, "Categories"); //TODO: Set args for each frag
        adapter.addFragment(frag2, "Measurements");
        adapter.addFragment(frag3, "Layers");
        viewPager.setAdapter(adapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null)
                    NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO: Switch tabs
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getAction() != null)
            switch (intent.getAction()) {
                case ACTION_SWITCH_CATEGORY:
                    mTabLayout.getTabAt(LayerPageFragment.ARG_CAT).select();
                    break;
                case ACTION_SWITCH_MEASURE:
                    mTabLayout.getTabAt(LayerPageFragment.ARG_MEASURE).select();
                    break;
            }
    }

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
