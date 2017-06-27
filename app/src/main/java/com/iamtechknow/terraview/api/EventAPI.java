package com.iamtechknow.terraview.api;

import com.iamtechknow.terraview.model.EventList;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface EventAPI {
    @GET("/api/v2.1/events")
    Observable<EventList> getOpenEvents();

    @GET("/api/v2.1/events/?status=closed")
    Observable<EventList> getClosedEvents(@Query("limit") int limit);

    @GET("/api/v2.1/categories/{cat}")
    Observable<EventList> getEventsByCategory(@Path("cat") int cat);

    @GET("/api/v2.1/categories/{cat}/?status=closed")
    Observable<EventList> getClosedEventsByCategory(@Path("cat") int cat, @Query("limit") int limit);
}
