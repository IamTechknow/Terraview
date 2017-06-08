package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.model.Event;

public interface EventPresenter {
    void detachView();

    void loadEvents();

    void handleEvent(Object event);

    void presentSource(String url);

    void presentEvent(Event e);

    void presentClosed(int num);
}
