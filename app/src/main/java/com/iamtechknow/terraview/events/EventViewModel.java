package com.iamtechknow.terraview.events;

import android.arch.lifecycle.ViewModel;

//TODO: Store list data as well to prevent redundant API calls
public class EventViewModel extends ViewModel {
    private int limit, category;
    private boolean showingClosed;

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
}
