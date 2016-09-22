package com.iamtechknow.worldview;

import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.widget.DatePicker;

import com.iamtechknow.worldview.anim.AnimDialogActivity;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DialogTest {
    @Rule
    public IntentsTestRule<AnimDialogActivity> mAddTaskIntentsTestRule =
            new IntentsTestRule<>(AnimDialogActivity.class);

    @Test
    public void errorShownOnIncompleteDialog() {
        //Press animate, verify Snackbar text
        onView(withId(R.id.anim_start)).perform(click());
        String msg = getTargetContext().getString(R.string.anim_warning);
        onView(withText(msg)).check(matches(isDisplayed()));
    }

    @Test
    public void errorShownOnBadDates() {
        final String fromDate = "Wed, Aug 10, 2016", toDate = "Mon, Aug 01, 2016";

        //Open Datepicker and set the date
        onView(withId(R.id.start_date)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2016, 8, 10));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.end_date)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2016, 8, 1));
        onView(withId(android.R.id.button1)).perform(click());

        //Verify text on dates
        onView(withText(fromDate)).check(matches(withId(R.id.start_date)));
        onView(withText(toDate)).check(matches(withId(R.id.end_date)));

        //Press animate and verify Snackbar text
        onView(withId(R.id.anim_start)).perform(click());
        String msg = getTargetContext().getString(R.string.anim_warning_before);
        onView(withText(msg)).check(matches(isDisplayed()));
    }
}
