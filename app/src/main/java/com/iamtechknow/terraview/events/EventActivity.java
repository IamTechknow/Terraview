package com.iamtechknow.terraview.events;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.TabAdapter;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import io.reactivex.disposables.Disposable;

public class EventActivity extends AppCompatActivity {
    public static final int SELECT_EVENT_TAB = 3, SELECT_EVENT = 5;
    public static final String EVENT_EXTRA = "event";
    private static final int EVENT_TAB = 1, PAGE_LIMIT = 2;

    //UI handling
    private TabLayout mTabLayout;
    private Disposable sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Create arguments for each fragments which will be used when they're created
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());

        adapter.addFragment(new CategoryViewImpl(), getString(R.string.categories));
        adapter.addFragment(new EventViewImpl(), getString(R.string.events));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(PAGE_LIMIT);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //back button pressed
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
        sub = RxBus.getInstance().toObserverable().subscribe(this::handleEvent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sub.dispose();
        sub = null;
    }

    private void handleEvent(Object event) {
        if(event instanceof TapEvent)
            switch(((TapEvent) event).getTab()) {
                case SELECT_EVENT_TAB:
                    mTabLayout.getTabAt(EVENT_TAB).select();
                    break;

                case SELECT_EVENT:
                    Bundle b = new Bundle();
                    b.putParcelable(EVENT_EXTRA, ((TapEvent) event).getEonetEvent());
                    setResult(RESULT_OK, new Intent().putExtras(b));
                    finish();
            }
    }
}
