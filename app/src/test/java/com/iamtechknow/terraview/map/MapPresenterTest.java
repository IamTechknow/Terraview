package com.iamtechknow.terraview.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static org.mockito.Mockito.*;

public class MapPresenterTest {
    //Default calls to MapInteractor.addTile() due to loading default tiles
    private static final int DEFAULT_ADD_TILE_CALLS = 2;

    @Mock
    private WorldActivity view;

    @Mock
    private MapInteractor map;

    private ArrayList<Layer> stack, delete;

    private WorldPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        stack = new ArrayList<>();
        delete = new ArrayList<>();
        presenter = new WorldPresenter(map);
        presenter.attachView(view);

        prepMap();
    }

    /**
     * Properly mock the MapInteractor
     */
    private void prepMap() {
        //Mock creating a new tile overlay and mock getting the GoogleMap
        //This method may break upon changes to the GMaps library
        when(map.addTile(any(Layer.class), any(String.class))).thenReturn(new TileOverlay(new FakeZZW()));
        presenter.onMapReady(null);
    }

    //Test presenter initialization
    @Test
    public void testMockInit() {
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        Utils.getCalendarMidnightTime(c);

        verify(view).setDateDialog(c.getTimeInMillis());
        presenter.detachView();
    }

    @Test
    public void testSetMapEmpty() {
        //Prepare to delete default layers
        Layer viirs_del = getVIIRS(), coastlines_del = new Layer();
        coastlines_del.setIdentifier("Coastlines");
        delete.add(viirs_del);
        delete.add(coastlines_del);

        //Send empty list to presenter
        presenter.setLayersAndUpdateMap(stack, delete);

        //Verify empty list is sent to layer list adapter, no map tiles added
        verify(view).setLayerList(stack);
        verify(map, times(DEFAULT_ADD_TILE_CALLS)).addTile(any(Layer.class), any(String.class));
        verify(map, times(DEFAULT_ADD_TILE_CALLS)).removeTile(any(TileOverlay.class), any(Layer.class), eq(false));
        presenter.detachView();
    }

    @Test
    public void testSetListAndWarning() {
        Layer viirs = getVIIRS();
        //The VIIRS is already added, but here we have it for verification. Now change date three years back
        presenter.onDateChanged(new Date(1353715200000L));

        //Verify VIIRS tile was added, but warning occurred
        verify(map).addTile(viirs, Utils.parseDate(presenter.getCurrDate()));
        verify(view).warnUserAboutActiveLayers();
        presenter.detachView();
    }

    @Test
    public void testEvents() {
        //Act
        Event pointEvent = new Event("EONET_2856", "Seven Fire, NEW MEXICO", "https://inciweb.nwcg.gov/incident/5271/", new ArrayList<>(Collections.singletonList("2017-06-22T13:15:00Z")), 8, Collections.singletonList(new LatLng(33.514166666667,-108.46277777778))),
                polyEvent = new Event("EONET_2851", "Southwest U.S. Heat Wave", "https://earthobservatory…/IOTD/view.php?id=90443", "2017-06-21T00:00:00Z", 18,
                        new PolygonOptions().add(new LatLng(30.726902912103963, -117.392578125), new LatLng(38.15804159576718, -117.392578125), new LatLng(38.15804159576718, -102.75390625), new LatLng(30.726902912103963, -102.75390625), new LatLng(30.726902912103963, -117.392578125)));
        presenter.presentEvent(pointEvent);
        presenter.presentEvent(polyEvent);
        presenter.onClearEvent();

        //Assert events shown and proper objects used
        verify(map).moveCamera(pointEvent.getPoints().get(0));
        verify(map).drawPolygon(polyEvent.getPolygon());
        verify(view).clearEvent();
        presenter.detachView();
    }

    @Test
    public void testSwaping() {
        //Add overlays, set layers
        stack.add(getVIIRS());
        stack.add(new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, null, false));
        stack.add(new Layer("OMI_Aerosol_Index", "GoogleMapsCompatible_Level6", "png", "Aerosol Index (OMI, Aura)", "Aura / OMI", null, "2004-10-01", null, null, false));
        stack.add(new Layer("MODIS_Aqua_Chlorophyll_A", "GoogleMapsCompatible_Level7", "png", "Chlorophyll (MODIS, Aqua)", "Aqua / MODIS", null, "2013-07-02", null, null, false));
        presenter.setLayersAndUpdateMap(stack, delete);

        //Mock dragging Coastlines layer to the bottom, Aerosol to top
        presenter.onSwapNeeded(1, 2);
        presenter.onSwapNeeded(2, 3);
        presenter.onSwapNeeded(1, 0);
    }

    @Test
    public void testNavUI() {
        //When user chooses show layer info menu option
        presenter.presentColorMaps();
        presenter.chooseLayers();
        presenter.presentAbout();
        presenter.presentAnimDialog();
        presenter.presentHelp();
        presenter.presentEvents();

        //Color maps dialog is displayed
        verify(view).showColorMaps();
        verify(view).showPicker();
        verify(view).showAbout();
        verify(view).showAnimDialog();
        verify(view).showHelp();
        verify(view).showEvents();
    }

    private Layer getVIIRS() {
        return new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, VIIRS, SNPP)", "Suomi NPP / VIIRS", null, "2015-11-24", null, null, true);
    }
}
