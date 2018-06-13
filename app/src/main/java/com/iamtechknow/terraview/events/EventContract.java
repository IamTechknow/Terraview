package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;

import java.util.ArrayList;

public interface EventContract {
    interface View {
        void insertList(int category, ArrayList<Event> list);

        void clearList();

        void showSource(String url);

        void warnNoSource();

        int getEventLimit();
    }

    interface Presenter{
        void detachView ();

        void loadEvents ( boolean onStart);

        void handleEvent (Object event);

        void presentSource (String url);

        void presentEvent (Event e);

        void presentClosed ( int num);

        int getCurrCategory ();

        void onEventsLoaded (EventList data);
    }
}
