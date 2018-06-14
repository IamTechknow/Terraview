package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.model.TapEvent;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {
	//A subject is both an Observable and an Observer, which allows items to be emitted and observed on by subscribers
    private Subject<TapEvent> _bus;
    private static RxBus INSTANCE;

    public static RxBus getInstance() {
        return getInstance(INSTANCE == null ? PublishSubject.create() : null);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     * @param sub the Subject to use, allows DI for unit testing
     * @return the {@link RxBus} instance
     */
    public static RxBus getInstance(Subject<TapEvent> sub) {
        if (INSTANCE == null || sub != null)
            INSTANCE = new RxBus(sub);

        return INSTANCE;
    }

    private RxBus(Subject<TapEvent> subject) {
        _bus = subject;
    }

	//Posts an event to the observer of the subject
    public void send(TapEvent o) {
        _bus.onNext(o);
    }

	//Returns the observable of the event bus to allow fragments to subscribe to new events
    public Observable<TapEvent> toObservable() {
        return _bus;
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}
