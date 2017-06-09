package com.iamtechknow.terraview.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * API Client to access events and categories from EONET. This client is Async,
 * data comes from callbacks after they are loaded in the background.
 */
public class EONET {
    public interface LoadCallback {
        void onEventsLoaded(ArrayList<Event> data);

        void onCategoriesLoaded(ArrayList<Category> data);
    }

    private static final String EVENTS_ENDPOINT = "https://eonet.sci.gsfc.nasa.gov/api/v2.1/events",
        CATEGORIES_ENDPOINT = "https://eonet.sci.gsfc.nasa.gov/api/v2.1/categories",
        CLOSED_LIMIT = "?status=closed&limit=%d", CAT_FILTER = "/%d";

    private static final String EVENTS = "events", ID = "id", TITLE = "title", CAT = "categories",
            SOURCE = "sources", GEOMETRY = "geometries", GEO_TYPE = "type", POINT = "Point",
            COORD = "coordinates", URL = "url", GEO_DATE = "date", CLOSED = "closed";

    private OkHttpClient client;

    private LoadCallback callback;

    public EONET() {
        client = new OkHttpClient();
    }

    /**
     * Get events from the default endpoint, which returns all open events.
     */
    public Disposable getOpenEvents() {
        return getEvents(EVENTS_ENDPOINT);
    }

    /**
     * Filter open events by category
     * @param catID desired Category ID
     */
    public Disposable getEventsByCategory(int catID) {
        return getEvents(String.format(Locale.US, CATEGORIES_ENDPOINT + CAT_FILTER, catID));
    }

    /**
     * Get events with a query to request closed events up to a limit.
     * @param limit Number of events to get
     */
    public Disposable getClosedEvents(int catID, int limit) {
        return catID == 0 ? getEvents(String.format(Locale.US, EVENTS_ENDPOINT + CLOSED_LIMIT, limit))
            : getEvents(String.format(Locale.US, CATEGORIES_ENDPOINT + CAT_FILTER + CLOSED_LIMIT, catID, limit));
    }

    public void setCallback(LoadCallback callback) {
        this.callback = callback;
    }

    private Disposable getEvents(String endpoint) {
        Request r = new Request.Builder().url(endpoint).build();

        return Observable.just(r).map(request -> {
            ArrayList<Event> result = new ArrayList<>();
            try (Response response = client.newCall(r).execute()) {
                JsonObject root = new JsonParser().parse(response.body().string()).getAsJsonObject();
                JsonArray eventArray = root.getAsJsonArray(EVENTS);

                //Parse each event element
                for(JsonElement e : eventArray) {
                    JsonObject obj = e.getAsJsonObject(), geo_obj, source_obj;
                    JsonArray geo = obj.getAsJsonArray(GEOMETRY), coord_array, src_array;
                    String id = obj.get(ID).getAsString(), title = obj.get(TITLE).getAsString(),
                            source = "", date;

                    //Rarely an event won't have a source, check for that.
                    src_array = obj.get(SOURCE).getAsJsonArray();
                    if(src_array.size() > 0)
                        source = src_array.get(0).getAsJsonObject().get(URL).getAsString();

                    int c = obj.get(CAT).getAsJsonArray().get(0).getAsJsonObject().get(ID).getAsInt();
                    Event event;

                    //Get geometry object first before getting the points
                    geo_obj = geo.get(0).getAsJsonObject();
                    date = geo_obj.get(GEO_DATE).getAsString();
                    coord_array = geo_obj.get(COORD).getAsJsonArray();
                    if(geo_obj.get(GEO_TYPE).getAsString().equals(POINT)) {
                        LatLng coord = new LatLng(coord_array.get(1).getAsDouble(), coord_array.get(0).getAsDouble());
                        event = new Event(id, title, source, date, c, coord);
                    } else {
                        ArrayList<LatLng> coords = new ArrayList<>();
                        for(JsonElement poly_e : coord_array.get(0).getAsJsonArray()) {
                            JsonArray poly_coord = poly_e.getAsJsonArray();
                            coords.add(new LatLng(poly_coord.get(1).getAsDouble(), poly_coord.get(0).getAsDouble()));
                        }
                        event = new Event(id, title, source, date, c, new PolygonOptions().addAll(coords));
                    }

                    //Set closed date if any
                    if(obj.has(CLOSED))
                        event.setClosedDate(obj.get(CLOSED).getAsString());

                    result.add(event);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(events -> callback.onEventsLoaded(events));
    }

    /**
     * Get all known categories used in EONET.
     */
    public Disposable getCategories() {
        Request r = new Request.Builder().url(CATEGORIES_ENDPOINT).build();
        return Observable.just(r).map(request -> {
            ArrayList<Category> result = new ArrayList<>();

            //Put "All" category at top of list
            result.add(Category.getAll());

            try (Response response = client.newCall(r).execute()) {
                JsonObject root = new JsonParser().parse(response.body().string()).getAsJsonObject();
                JsonArray catArray = root.getAsJsonArray(CAT);

                //Get all categories from the array
                for (JsonElement e : catArray) {
                    JsonObject obj = e.getAsJsonObject();
                    result.add(new Category(obj.get(ID).getAsInt(), obj.get(TITLE).getAsString()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(categories -> callback.onCategoriesLoaded(categories));
    }
}
