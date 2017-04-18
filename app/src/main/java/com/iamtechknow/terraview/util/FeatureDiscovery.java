package com.iamtechknow.terraview.util;

import android.graphics.Rect;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.map.WorldActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Class designed to implement feature discovery of Material Design as a guided tour of the app.
 * Modularized to help keep WorldActivity.java clean.
 */
public final class FeatureDiscovery {
    private static final int HIGHLIGHT_DELAY = 500, IDX_LIMIT = 4;

    public static void guidedTour(WorldActivity map) {
        //Tour animation subscription
        final Disposable[] tour_sub = new Disposable[1];

        DrawerLayout mDrawerLayout = (DrawerLayout) map.findViewById(R.id.drawer);
        CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) map.findViewById(R.id.thelayout);
        NavigationView mNavLeft = (NavigationView) map.findViewById(R.id.nav_menu);
        Toolbar bar = (Toolbar) map.findViewById(R.id.tool_bar);

        //Calculate rect bounds, based on where the home button is
        View home_icon = Utils.findNavView(bar);
        final int[] coord = new int[2]; //X, Y of left corner of view
        home_icon.getLocationOnScreen(coord);

        Rect right_bounds = new Rect(mCoordinatorLayout.getWidth() - coord[0] - home_icon.getWidth(), coord[1], mCoordinatorLayout.getWidth(), coord[1] + home_icon.getHeight()),
                left_bounds = new Rect(coord[0], coord[1], coord[0] + home_icon.getWidth(), coord[1] + home_icon.getHeight());

        TapTarget part2 = TapTarget.forBounds(right_bounds, map.getString(R.string.tour_menu), map.getString(R.string.tour_menu_desc)),
                part3 = TapTarget.forBounds(left_bounds, map.getString(R.string.tour_menu_ctrls_title), map.getString(R.string.tour_menu_ctrls_desc));

        //Define the listeners for each part, must define last first
        TapTargetView.Listener part3_listener = new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                mDrawerLayout.closeDrawers();
                Snackbar.make(mCoordinatorLayout, R.string.tour_end, Snackbar.LENGTH_LONG).show();
            }
        }, part2_listener = new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mDrawerLayout.openDrawer(GravityCompat.END);
                TapTargetView.showFor(map, part3, part3_listener);
            }
        };

        //Set up and display the first target. The listener responds to the first tap.
        //Highlighting multiple items is not possible, do it one by one
        TapTargetView.showFor(map, TapTarget.forToolbarNavigationIcon(bar,
            map.getString(R.string.tour_start), map.getString(R.string.tour_start_sub)),
            new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    mDrawerLayout.openDrawer(GravityCompat.START);

                    tour_sub[0] = Observable.interval(HIGHLIGHT_DELAY, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            if(aLong == IDX_LIMIT) {
                                tour_sub[0].dispose();
                                mNavLeft.getMenu().getItem(IDX_LIMIT).setChecked(false);
                                TapTargetView.showFor(map, part2, part2_listener);
                            } else
                                mNavLeft.getMenu().getItem((int) (aLong + 1)).setChecked(true);
                        }, Throwable::printStackTrace);
                }
            });
    }
}
