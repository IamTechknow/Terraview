package com.iamtechknow.terraview.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;

import org.junit.Test;
import org.junit.Assert;

public class EventDeserializerTest {
    @Test
    public void testOpenEvent() {
        final Gson gson = new GsonBuilder().registerTypeAdapter(EventList.class, new EventDeserializer()).create();
        String json = "{\n" +
                "  \"title\": \"EONET Events\",\n" +
                "  \"description\": \"Natural events from EONET.\",\n" +
                "  \"link\": \"https://eonet.sci.gsfc.nasa.gov/api/v2.1/events\",\n" +
                "  \"events\": [\n" +
                "    {\n" +
                "      \"id\": \"EONET_3253\",\n" +
                "      \"title\": \"West Fork Fire, MONTANA\",\n" +
                "      \"description\": \"\",\n" +
                "      \"link\": \"http://eonet.sci.gsfc.nasa.gov/api/v2.1/events/EONET_3253\",\n" +
                "      \"categories\": [\n" +
                "        {\n" +
                "          \"id\": 8,\n" +
                "          \"title\": \"Wildfires\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"sources\": [\n" +
                "        {\n" +
                "          \"id\": \"InciWeb\",\n" +
                "          \"url\": \"https://inciweb.nwcg.gov/incident/5585/\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"geometries\": [\n" +
                "        {\n" +
                "          \"date\": \"2017-08-30T10:45:00Z\",\n" +
                "          \"type\": \"Point\",\n" +
                "          \"coordinates\": [\n" +
                "            -115.64888888889,\n" +
                "            48.507777777778\n" +
                "      ]}]}]}";

        EventList result = gson.fromJson(json, EventList.class);
        Event parsed = result.list.get(0);

        //Verify parsed details and that no closed date was parsed
        Assert.assertEquals(parsed.getCategory(), 8);
        Assert.assertEquals(parsed.getTitle(), "West Fork Fire, MONTANA");
        Assert.assertEquals(parsed.getId(), "EONET_3253");
        Assert.assertNull(parsed.getClosedDate());
    }
}
