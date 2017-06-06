package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.model.Event;

import java.util.ArrayList;

public interface EventView {
    void insertList(ArrayList<Event> list);

    void clearList();

    void showSource(String url);
}
