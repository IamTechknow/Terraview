package com.iamtechknow.terraview.picker;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {
	//A subject is both an Observable and an Observer, which allows items to be emitted and observed on by subscribers
    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());
    private static RxBus INSTANCE;

    /**
     * Returns the single instance of this class, creating it if necessary.
     * @return the {@link RxBus} instance
     */
    public static RxBus getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RxBus();

        return INSTANCE;
    }

	//Posts an event to the observer of the subject
    public void send(Object o) {
        _bus.onNext(o);
    }

	//Returns the observable of the event bus to allow fragments to subscribe to new events
    public Observable<Object> toObserverable() {
        return _bus;
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }
}
