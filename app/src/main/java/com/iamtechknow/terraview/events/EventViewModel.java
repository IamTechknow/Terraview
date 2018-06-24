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
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class EventViewModel extends ViewModel {
    private static final int EVENT_INTERVAL = 30;

    private EONET client;
    private RxBus bus;
    private Disposable busSub;

    //Events model state
    private int limit = EVENT_INTERVAL, category;
    private boolean showingClosed;
    private EventList data;

    //EventList live data treated as a variable
    private Subject<EventList> liveData;

    public EventViewModel(EONET client, RxBus bus) {
        this.client = client;
        this.bus = bus;

        busSub = bus.toObservable().subscribe(this::handleEvent);
        liveData = PublishSubject.create();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        busSub.dispose();
    }

    public int getLimit() {
        return limit;
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

    public EventList getData() {
        return data;
    }

    public void setData(EventList data) {
        this.data = data;
    }

    public Observable<EventList> getLiveData() {
        return liveData;
    }

    public Single<EventList> loadEvents(boolean onStart) {
        showingClosed = false;
        return category == 0 ? client.getOpenEvents() : client.getEventsByCategory(category);
    }

    public Single<EventList> loadClosedEvents(int limit) {
        this.limit = limit;
        showingClosed = true;
        return client.getClosedEvents(category, limit);
    }

    //Get the data not wrapped in a Single, which will be wrapped by the subject.
    //This is a bridge between the Rx API and a non-Rx API, like a RxRelay.
    public void handleEvent(TapEvent e) {
        if(e != null && e.getTab() == EventActivity.SELECT_EVENT_TAB) {
            category = e.getArg();
            liveData.onNext(client.getEventsForEventBus(category));
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
}
