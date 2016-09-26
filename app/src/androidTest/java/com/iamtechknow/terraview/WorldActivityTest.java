package com.iamtechknow.terraview;

import android.annotation.SuppressLint;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.GravityCompat;
import android.view.View;

import com.dannyroa.espresso_samples.recyclerview.RecyclerViewMatcher;
import com.iamtechknow.terraview.colormaps.ColorMapViewImpl;
import com.iamtechknow.terraview.map.WorldActivity;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A test that starts up and verifies that the two default layers are shown on the map, then swipes them away.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class WorldActivityTest {
    @Rule
    public ActivityTestRule<WorldActivity> mActivityRule = new ActivityTestRule<>(WorldActivity.class);

    /**
     * Access the right hand drawer's RecyclerView and verify there are two entries in the list,
     * swipe them, and verify the list is now empty.
     */
    @Test
    public void checkMapDeleteLayers() {
        RecyclerViewMatcher matcher = new RecyclerViewMatcher(R.id.layer_list);
        onView(matcher.atPosition(0))
                .check(matches(hasDescendant(withText("Coastlines (OSM)"))));
        onView(matcher.atPosition(1))
                .check(matches(hasDescendant(withText("Corrected Reflectance (True Color, VIIRS, SNPP)"))));

        //Open the drawer, select items and swipe left
        onView(withId(R.id.drawer)).perform(open(GravityCompat.END));
        onView(matcher.atPosition(1)).perform(swipeLeftCenter());
        onView(matcher.atPosition(0)).perform(swipeLeftCenter());

        //Assert RecyclerView is empty
        onView(withId(R.id.layer_list))
                .check(matches(SizeMatcher.withListSize(0)));
    }

    /**
     * Access the right hand drawer's RecyclerView, long press on the first item, and drag it below the second item.
     */
    @Test
    public void swapLayerItems() {
        RecyclerViewMatcher matcher = new RecyclerViewMatcher(R.id.layer_list);
        onView(withId(R.id.drawer)).perform(open(GravityCompat.END));
        onView(matcher.atPosition(0)).perform(swipeDownEnd());

        onView(matcher.atPosition(1))
                .check(matches(hasDescendant(withText("Coastlines (OSM)"))));
        onView(matcher.atPosition(0))
                .check(matches(hasDescendant(withText("Corrected Reflectance (True Color, VIIRS, SNPP)"))));
    }

    /**
     * Select two layer items from the picker and verify color map views exist.
     */
    @SuppressLint("PrivateResource")
    @Test
    public void colorMapsTest() {
        //Go to layer picker and layer tab
        onView(withId(R.id.drawer)).perform(open(GravityCompat.START));
        onView(withText(getTargetContext().getString(R.string.layers))).perform(click());
        onView(allOf(withText("Layers"), isDescendantOfA(withId(R.id.tabs)))).perform(click());

        //Need to get the recycler view in view pager that is visible on screen
        onView(allOf(withId(R.id.recycler_view), isDisplayed())).perform(actionOnItemAtPosition(0, click()));
        onView(allOf(withId(R.id.recycler_view), isDisplayed())).perform(actionOnItemAtPosition(1, click()));

        //Press home button to go back, go to colormaps screen
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withId(R.id.drawer)).perform(open(GravityCompat.START));
        onView(withText(getTargetContext().getString(R.string.layer_info))).perform(click());

        //Verify no color map text for first view, colormaps view for third by checking descendants
        RecyclerViewMatcher matcher = new RecyclerViewMatcher(R.id.color_map_rv);
        onView(matcher.atPosition(0)).check(matches(hasDescendant(withText(R.string.colormap_none))));
        onView(matcher.atPosition(3)).check(matches(hasDescendant(withClassName(startsWith(ColorMapViewImpl.class.getName())))));
    }

    /**
     * Create a swipe action with "fast" speed, starting from the center and to the left.
     * @return ViewAction representing a swipe from the center to the left
     */
    private static ViewAction swipeLeftCenter() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER,
                GeneralLocation.CENTER_LEFT, Press.FINGER);
    }

    /**
     * Create a swipe action that starts at the center right and swipes to
     * the Y coordinate twice of what the original end position would be.
     * @return ViewAction of the above
     */
    private static ViewAction swipeDownEnd() {
        CoordinatesProvider my_bottom = view -> {
            float[] coordinates = GeneralLocation.BOTTOM_RIGHT.calculateCoordinates(view);
            coordinates[1] *= 2;
            return coordinates;
        };

        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER_RIGHT, my_bottom, Press.FINGER);
    }
}
