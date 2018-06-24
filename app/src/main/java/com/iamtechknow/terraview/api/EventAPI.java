package com.iamtechknow.terraview.api;

import com.iamtechknow.terraview.model.EventList;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * API calls to parse all open events, closed events, and events by categories.
 */
public interface EventAPI {
    @GET("/api/v2.1/events")
    Single<EventList> getOpenEvents();

    @GET("/api/v2.1/events/?status=closed")
    Single<EventList> getClosedEvents(@Query("limit") int limit);

    @GET("/api/v2.1/categories/{cat}")
    Single<EventList> getEventsByCategory(@Path("cat") int cat);

    @GET("/api/v2.1/categories/{cat}")
    EventList getEventsForEventBus(@Path("cat") int cat);

    @GET("/api/v2.1/categories/{cat}/?status=closed")
    Single<EventList> getClosedEventsByCategory(@Path("cat") int cat, @Query("limit") int limit);
}
