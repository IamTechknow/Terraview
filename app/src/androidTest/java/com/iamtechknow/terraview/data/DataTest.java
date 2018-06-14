package com.iamtechknow.terraview.data;

import android.support.test.runner.AndroidJUnit4;

import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.SearchQuery;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class DataTest extends DbTest {
    @Test
    public void testDataInOrder() {
        //Get the data and sort it
        List<Layer> data = new ArrayList<>();
        data.add(new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, VIIRS, SNPP)", "Suomi NPP / VIIRS", null, "2015-11-24", null, null, true));
        data.add(new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, null, false));
        Collections.sort(data);

        db.getTVDao().insertLayers(data);

        //Test that first layer on list is Coastlines
        List<Layer> loaded = db.getTVDao().getLayers();
        assertEquals(data.get(0), loaded.get(0));
    }

    @Test
    public void testSearchQuery() {
        //Create a search query
        Layer coastlines = new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, null, false);
        List<SearchQuery> list = Collections.singletonList(new SearchQuery(coastlines.getTitle() , coastlines.getIdentifier()));
        db.getTVDao().insertQueries(list);

        //Pretend user types Coast on search bar, verify there is search result. Remember to add the % to allow the query to work
        assertEquals(1, db.getTVDao().searchQuery("Coast" + "%").getCount());
    }
}
