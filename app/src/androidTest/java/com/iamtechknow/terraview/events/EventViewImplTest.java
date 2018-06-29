package com.iamtechknow.terraview.events;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.dannyroa.espresso_samples.recyclerview.RecyclerViewMatcher;
import com.google.android.gms.maps.model.LatLng;
import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.SingleFragmentActivity;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;
import com.iamtechknow.terraview.util.ViewModelUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.*;

/**
 * White box instrumentation tests to test the user flow of the event fragment.
 */
@RunWith(AndroidJUnit4.class)
public class EventViewImplTest {
    @Rule
    public ActivityTestRule<SingleFragmentActivity> activityRule = new ActivityTestRule<>(SingleFragmentActivity.class);

    private Subject<EventList> liveData = PublishSubject.create();

    private ArrayList<Event> events = new ArrayList<>();

    private ArrayList<String> date = new ArrayList<>();

    private Event event;

    @Before
    public void init() {
        //Create mock view model
        EventViewModel viewModel = mock(EventViewModel.class);
        when(viewModel.getLiveData()).thenReturn(liveData);

        //Pass it to the activity
        EventViewImpl frag = new EventViewImpl();
        frag.factory = ViewModelUtil.createFor(viewModel);
        activityRule.getActivity().setFragment(frag);

        date.add("2017-09-03T13:00:00Z");
        event = new Event("EONET_3249", "Mission Fire, CALIFORNIA",  "https://inciweb.nwcg.gov/incident/5588/", date, 8, Collections.singletonList(new LatLng(37.212777777778, -119.48277777778)));
    }

    @Test
    public void testLoading() {
        //Verify subscription, emit data
        events.add(event);
        waitForFragThenEmit(new EventList(events));

        //Verify data is loaded
        onView(withId(R.id.empty_view)).check(matches(not(isDisplayed())));
        RecyclerViewMatcher matcher = new RecyclerViewMatcher(R.id.recycler_view);
        onView(matcher.atPosition(0)).check(matches(hasDescendant(withText(event.getTitle()))));
    }

    @Test
    public void testBadSource() {
        date.set(0, "2017-09-03T13:00:00Z");
        events.add(new Event("", "Iceberg", null, date, 0, Collections.singletonList(new LatLng(0, 0))));
        waitForFragThenEmit(new EventList(events));

        //Click on info icon
        RecyclerViewMatcher matcher = new RecyclerViewMatcher(R.id.recycler_view);
        onView(matcher.atPositionOnView(0, R.id.item_info)).perform(ViewActions.click());

        //Verify snackbar appeared with warning text
        onView(allOf(withId(android.support.design.R.id.snackbar_text), withText(R.string.no_source))).check(matches(isDisplayed()));
    }

    private void waitForFragThenEmit(EventList list) {
        //Force espresso (this test) to wait until fragment has setup
        onView(withId(R.id.empty_view)).check(matches(isDisplayed()));
        Assert.assertTrue(liveData.hasObservers());
        activityRule.getActivity().runOnUiThread(() -> liveData.onNext(list));
    }
}
