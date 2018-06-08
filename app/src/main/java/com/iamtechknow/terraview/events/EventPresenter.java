package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;

public interface EventPresenter {
    void detachView();

    void loadEvents(boolean onStart);

    void handleEvent(Object event);

    void presentSource(String url);

    void presentEvent(Event e);

    void presentClosed(int num);

    void restoreConfig(boolean showClosed, int cat);

    int getCurrCategory();

    void onEventsLoaded(EventList data);
}
