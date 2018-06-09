package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;
import com.iamtechknow.terraview.util.Utils;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class EventPresenterImpl implements EventPresenter {
    private EventView view;
    private Disposable sub, dataSub;
    private RxBus bus;
    private EONET client;

    //Whether open events have been loaded at startup
    private boolean loadedEvents, showingClosed;

    //Current category used for loading closed events
    private int currCat;

    public EventPresenterImpl(RxBus _bus, EventView v, EONET e, boolean closed, int cat) {
        view = v;
        client = e;
        bus = _bus;
        sub = _bus.toObserverable().subscribe(this::handleEvent);
        currCat = cat;
        showingClosed = closed;
    }

    @Override
    public void detachView() {
        if(dataSub != null) {
            dataSub.dispose();
            dataSub = null;
        }
        sub.dispose();
        sub = null;
        view = null;
    }

    /**
     * Load current events from EONET, if loaded from onStart, only gets loaded once.
     */
    @Override
    public void loadEvents(boolean onStart) {
        if(onStart && loadedEvents)
            return;

        loadedEvents = true;
        showingClosed = false;
        view.clearList();

        Single<EventList> observable = currCat == 0 ? client.getOpenEvents() : client.getEventsByCategory(currCat);
        dataSub = observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onEventsLoaded);
    }

    //Handle the event passed when a category is tapped
    //Need to handle whether to send open or close events
    @Override
    public void handleEvent(Object event) {
        if(event instanceof TapEvent) {
            TapEvent e = (TapEvent) event;
            if(e.getTab() == EventActivity.SELECT_EVENT_TAB) {
                currCat = e.getArg();
                view.clearList();

                Single<EventList> observable;
                if(showingClosed)
                    observable = client.getClosedEvents(currCat, view.getEventLimit());
                else if(currCat == 0)
                    observable = client.getOpenEvents();
                else
                    observable = client.getEventsByCategory(currCat);
                dataSub = observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onEventsLoaded);
            }
        }
    }

    //Send an event to the event bus when an event has been tapped
    @Override
    public void presentEvent(Event e) {
        bus.send(new TapEvent(EventActivity.SELECT_EVENT, e));
    }

    /**
     * Present the source URL after validating it
     * @param url web link to be checked first
     */
    @Override
    public void presentSource(String url) {
        if(Utils.getURLPattern().matcher(url).matches())
            view.showSource(url);
        else
            view.warnNoSource();
    }

    @Override
    public void presentClosed(int num) {
        showingClosed = true;
        view.clearList();
        dataSub = client.getClosedEvents(currCat, num)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onEventsLoaded);
    }

    @Override
    public int getCurrCategory() {
        return currCat;
    }

    @Override
    public void onEventsLoaded(EventList data) {
        view.insertList(currCat, data.list);
    }
}
