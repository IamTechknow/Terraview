package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class EventPresenterImpl implements EventPresenter, EONET.LoadCallback {
    private EventView view;
    private Disposable sub;
    private EONET client;

    public EventPresenterImpl(RxBus _bus, EventView v) {
        view = v;
        client = new EONET();
        client.setCallback(this);
        sub = _bus.toObserverable().subscribe(this::handleEvent);
    }

    @Override
    public void detachView() {
        sub.dispose();
        sub = null;
        view = null;
    }

    @Override
    public void loadEvents(Category c) {
        if(c.getId() == 0)
            client.getOpenEvents();
        else
            client.getEventsByCategory(c.getId());
    }

    @Override
    public void handleEvent(Object event) {
        if(event instanceof TapEvent) {
            TapEvent e = (TapEvent) event;
            if(e.getTab() == EventActivity.SELECT_EVENT_TAB) {
                view.clearList();
                client.getEventsByCategory(e.getArg());
            }
        }
    }

    @Override
    public void presentSource(String url) {
        view.showSource(url);
    }

    @Override
    public void onEventsLoaded(ArrayList<Event> data) {
        view.insertList(data);
    }

    @Override
    public void onCategoriesLoaded(ArrayList<Category> data) {}
}
