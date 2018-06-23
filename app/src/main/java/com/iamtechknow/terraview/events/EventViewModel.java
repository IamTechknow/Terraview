package com.iamtechknow.terraview.events;

import android.arch.lifecycle.ViewModel;

import com.iamtechknow.terraview.model.Event;

import java.util.ArrayList;

public class EventViewModel extends ViewModel {
    private static final int EVENT_INTERVAL = 30;

    private int limit = EVENT_INTERVAL, category;
    private boolean showingClosed;
    private ArrayList<Event> data;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public boolean isShowingClosed() {
        return showingClosed;
    }

    public void setShowingClosed(boolean showingClosed) {
        this.showingClosed = showingClosed;
    }

    public ArrayList<Event> getData() {
        return data;
    }

    public void setData(ArrayList<Event> data) {
        this.data = data;
    }
}
