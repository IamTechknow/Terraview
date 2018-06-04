package com.iamtechknow.terraview.data;

import com.google.gson.GsonBuilder;
import com.iamtechknow.terraview.api.CategoryAPI;
import com.iamtechknow.terraview.api.EventAPI;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;

import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API Client to access events and categories from EONET. This client is Async,
 * data comes from callbacks after they are loaded in the background.
 */
public class EONET {
    public interface LoadCallback {
        void onEventsLoaded(ArrayList<Event> data);

        void onCategoriesLoaded(ArrayList<Category> data);
    }

    private static final String BASE = "https://eonet.sci.gsfc.nasa.gov";

    private Retrofit retrofit;

    private EventAPI api;

    private LoadCallback callback;

    public EONET() {
        retrofit = new Retrofit.Builder().baseUrl(BASE)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().registerTypeAdapter(EventList.class, new EventDeserializer()).create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
        api = retrofit.create(EventAPI.class);
    }

    /**
     * Get events from the default endpoint, which returns all open events.
     */
    public Disposable getOpenEvents() {
        return api.getOpenEvents()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(eventList -> callback.onEventsLoaded(eventList.list));
    }

    /**
     * Filter open events by category
     * @param catID desired Category ID
     */
    public Disposable getEventsByCategory(int catID) {
        return api.getEventsByCategory(catID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(eventList -> callback.onEventsLoaded(eventList.list));
    }

    /**
     * Get events with a query to request closed events up to a limit.
     * @param limit Number of events to get
     */
    public Disposable getClosedEvents(int catID, int limit) {
        Single<EventList> o = catID == 0 ? api.getClosedEvents(limit) : api.getClosedEventsByCategory(catID, limit);

        return o.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(eventList -> callback.onEventsLoaded(eventList.list));
    }

    public void setCallback(LoadCallback callback) {
        this.callback = callback;
    }

    /**
     * Get all known categories used in EONET.
     */
    public Disposable getCategories() {
        return retrofit.create(CategoryAPI.class).fetchCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(categoryList -> callback.onCategoriesLoaded(categoryList.list));
    }
}
