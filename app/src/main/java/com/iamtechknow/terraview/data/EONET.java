package com.iamtechknow.terraview.data;

import com.google.gson.GsonBuilder;
import com.iamtechknow.terraview.api.CategoryAPI;
import com.iamtechknow.terraview.api.EventAPI;
import com.iamtechknow.terraview.model.EventCategoryList;
import com.iamtechknow.terraview.model.EventList;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API Client to access events and categories from EONET. This client is Async,
 * data comes from callbacks after they are loaded in the background.
 */
public class EONET {
    private static final String BASE = "https://eonet.sci.gsfc.nasa.gov";
    private static EONET INSTANCE;

    private Retrofit retrofit;
    private EventAPI api;

    public static EONET getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new EONET();
        }
        return INSTANCE;
    }

    private EONET() {
        retrofit = new Retrofit.Builder().baseUrl(BASE)
            .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().registerTypeAdapter(EventList.class, new EventDeserializer()).create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
        api = retrofit.create(EventAPI.class);
    }

    /**
     * Get events from the default endpoint, which returns all open events.
     */
    public Single<EventList> getOpenEvents() {
        return api.getOpenEvents();
    }

    /**
     * Filter open events by category
     * @param catID desired Category ID
     */
    public Single<EventList> getEventsByCategory(int catID) {
        return catID == 0 ? api.getOpenEvents() : api.getEventsByCategory(catID);
    }

    /**
     * Get events with a query to request closed events up to a limit.
     * @param limit Number of events to get
     */
    public Single<EventList> getClosedEvents(int catID, int limit) {
        return catID == 0 ? api.getClosedEvents(limit) : api.getClosedEventsByCategory(catID, limit);
    }

    /**
     * Get all known categories used in EONET.
     */
    public Single<EventCategoryList> getCategories() {
        return retrofit.create(CategoryAPI.class).fetchCategories();
    }
}
