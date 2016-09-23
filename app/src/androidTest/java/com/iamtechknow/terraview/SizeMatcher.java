package com.iamtechknow.terraview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SizeMatcher {
    public static Matcher<View> withListSize (final int size) {
        return new TypeSafeMatcher<View>() {
            @Override public boolean matchesSafely (final View view) {
                return ((RecyclerView) view).getAdapter().getItemCount() == size;
            }

            @Override public void describeTo (final Description description) {
                description.appendText ("RecyclerView should have " + size + " items");
            }
        };
    }
}