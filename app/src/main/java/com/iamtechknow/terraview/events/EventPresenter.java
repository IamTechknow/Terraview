package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.model.Category;

public interface EventPresenter {
    void detachView();

    void loadEvents(Category c);

    void handleEvent(Object event);

    void presentSource(String url);
}
