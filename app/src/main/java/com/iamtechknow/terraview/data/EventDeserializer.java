package com.iamtechknow.terraview.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * A custom deserializer to account for the object notation and
 * inconsistent geometry types used in the Events API.
 */
public class EventDeserializer implements JsonDeserializer<EventList> {
    private static final String EVENTS = "events", ID = "id", TITLE = "title", CAT = "categories",
            SOURCE = "sources", GEOMETRY = "geometries", GEO_TYPE = "type", POINT = "Point",
            COORD = "coordinates", URL = "url", GEO_DATE = "date", CLOSED = "closed";

    @Override
    public EventList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ArrayList<Event> result = new ArrayList<>();
        JsonArray eventArray = json.getAsJsonObject().getAsJsonArray(EVENTS);

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
        return new EventList(result);
    }
}
