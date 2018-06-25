package com.iamtechknow.terraview.events;

import android.arch.lifecycle.ViewModel;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;
import com.iamtechknow.terraview.util.Utils;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class EventViewModel extends ViewModel {
    private static final int EVENT_INTERVAL = 30;

    private EONET client;
    private RxBus bus;
    private Disposable busSub, dataSub;

    //Events model state
    private int limit = EVENT_INTERVAL, category;
    private boolean showingClosed;
    private EventList data;

    //EventList live data treated as a variable
    private Subject<EventList> liveData;

    public EventViewModel(EONET client, RxBus bus) {
        this.client = client;
        this.bus = bus;


        liveData = PublishSubject.create();
    }

    public void startSub() {
        busSub = bus.toObservable().subscribe(this::handleEvent);
    }

    //Called when activity has been paused/exited or on config change
    public void cancelSubs() {
        busSub.dispose();
        dataSub.dispose();
    }

    public int getLimit() {
        return limit;
    }

    public int getCategory() {
        return category;
    }

    public boolean isShowingClosed() {
        return showingClosed;
    }

    public void setShowingClosed(boolean showingClosed) {
        this.showingClosed = showingClosed;
    }

    public EventList getData() {
        return data;
    }

    public Observable<EventList> getLiveData() {
        return liveData;
    }

    public void loadEvents(boolean onStart) {
        showingClosed = false;
        dataSub = category == 0 ? createSub(client.getOpenEvents()) : createSub(client.getEventsByCategory(category));
    }

    public void loadClosedEvents(int limit) {
        this.limit = limit;
        showingClosed = true;
        dataSub = createSub(client.getClosedEvents(category, limit));
    }

    //Category has changed, save it and make an API call to update the live data.
    public void handleEvent(TapEvent e) {
        if(e != null && e.getTab() == EventActivity.SELECT_EVENT_TAB) {
            category = e.getArg();
            dataSub = showingClosed ? createSub(client.getClosedEvents(category, limit)) : createSub(client.getEventsByCategory(category));
        }
    }

    //Send an event to the Activity when an event has been tapped
    public void presentEvent(Event e) {
        bus.send(new TapEvent(EventActivity.SELECT_EVENT, e));
    }

    /**
     * Present the source URL after validating it
     * @param url web link to be checked first
     */
    public boolean isSourceValid(String url) {
        return Utils.getURLPattern().matcher(url).matches();
    }

    /**
     * Create subscription for latest API request, for which the data will be sent to
     * the live observable when obtained. Invalidate current data too
     */
    private Disposable createSub(Single<EventList> o) {
        data = null;
        return o.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(rawData -> {
                data = rawData;
                liveData.onNext(data);
            });
    }
}
