package com.iamtechknow.worldview;

import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.GravityCompat;

import com.dannyroa.espresso_samples.recyclerview.RecyclerViewMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.contrib.DrawerActions.open;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A test that starts up and verifies that the two default layers are shown on the map, then swipes them away.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartupTest {
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
                .check(matches(hasDescendant(withText("Coastlines"))));
        onView(matcher.atPosition(1))
                .check(matches(hasDescendant(withText("Corrected Reflectance (True Color)"))));

        //Open the drawer, select items and swipe left
        onView(withId(R.id.drawer))
                .perform(open(GravityCompat.END));
        onView(matcher.atPosition(1)).perform(swipeLeftCenter());
        onView(matcher.atPosition(0)).perform(swipeLeftCenter());

        //Assert RecyclerView is empty
        onView(withId(R.id.layer_list))
                .check(matches(SizeMatcher.withListSize(0)));
    }

    /**
     * Create a swipe action with "fast" speed, starting from the center and to the left.
     * @return ViewAction representing a swipe from the center to the left
     */
    private static ViewAction swipeLeftCenter() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER,
                GeneralLocation.CENTER_LEFT, Press.FINGER);
    }
}