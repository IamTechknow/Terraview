package com.iamtechknow.terraview.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.iamtechknow.terraview.model.ColorMap;
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

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.mockito.Mockito.*;

public class WorldViewModelTest {
    //Default calls to MapInteractor.addTile() due to loading default tiles
    private static final int DEFAULT_ADD_TILE_CALLS = 2;

    @Mock
    private MapInteractor map;

    private Subject<ColorMap> subject = PublishSubject.create();

    private ArrayList<Layer> stack, delete;

    private WorldViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        stack = new ArrayList<>();
        delete = new ArrayList<>();
        viewModel = new WorldViewModel(map, subject);

        prepMap();
    }

    /**
     * Properly mock the MapInteractor
     */
    private void prepMap() {
        //Mock creating a new tile overlay and mock getting the GoogleMap
        //This method may break upon changes to the GMaps library
        when(map.addTile(any(Layer.class), any(String.class))).thenReturn(new TileOverlay(new FakeZZAC()));
        viewModel.onMapReady(null);
    }

    //Test presenter initialization
    @Test
    public void testMockInit() {
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        Utils.getCalendarMidnightTime(c);

    }

    @Test
    public void testSetMapEmpty() {
        //Prepare to delete default layers
        Layer viirs_del = getVIIRS(), coastlines_del = new Layer();
        coastlines_del.setIdentifier("Coastlines");
        delete.add(viirs_del);
        delete.add(coastlines_del);

        //Send empty list to presenter
        viewModel.setLayersAndUpdateMap(stack, delete);

        //Verify empty list is sent to layer list adapter, no map tiles added
        verify(map, times(DEFAULT_ADD_TILE_CALLS)).addTile(any(Layer.class), any(String.class));
        verify(map, times(DEFAULT_ADD_TILE_CALLS)).removeTile(any(TileOverlay.class), any(Layer.class), eq(false));
    }

    @Test
    public void testSetListAndWarning() {
        Layer viirs = getVIIRS();
        //The VIIRS is already added, but here we have it for verification. Now change date three years back
        viewModel.onDateChanged(new Date(1353715200000L));

        //Verify VIIRS tile was added, but warning occurred
        verify(map).addTile(viirs, Utils.parseDate(viewModel.getCurrDate()));
    }

    @Test
    public void testEvents() {
        //Act
        Event pointEvent = new Event("EONET_2856", "Seven Fire, NEW MEXICO", "https://inciweb.nwcg.gov/incident/5271/", new ArrayList<>(Collections.singletonList("2017-06-22T13:15:00Z")), 8, Collections.singletonList(new LatLng(33.514166666667,-108.46277777778))),
                polyEvent = new Event("EONET_2851", "Southwest U.S. Heat Wave", "https://earthobservatory…/IOTD/view.php?id=90443", "2017-06-21T00:00:00Z", 18,
                        new PolygonOptions().add(new LatLng(30.726902912103963, -117.392578125), new LatLng(38.15804159576718, -117.392578125), new LatLng(38.15804159576718, -102.75390625), new LatLng(30.726902912103963, -102.75390625), new LatLng(30.726902912103963, -117.392578125)));
        viewModel.presentEvent(pointEvent);
        viewModel.presentEvent(polyEvent);
        viewModel.onClearEvent();

        //Assert events shown and proper objects used
        verify(map).moveCamera(pointEvent.getPoints().get(0));
        verify(map).drawPolygon(polyEvent.getPolygon());
    }

    @Test
    public void testSwapping() {
        //Add overlays, set layers
        stack.add(getVIIRS());
        stack.add(new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, null, false));
        stack.add(new Layer("OMI_Aerosol_Index", "GoogleMapsCompatible_Level6", "png", "Aerosol Index (OMI, Aura)", "Aura / OMI", null, "2004-10-01", null, null, false));
        stack.add(new Layer("MODIS_Aqua_Chlorophyll_A", "GoogleMapsCompatible_Level7", "png", "Chlorophyll (MODIS, Aqua)", "Aqua / MODIS", null, "2013-07-02", null, null, false));
        viewModel.setLayersAndUpdateMap(stack, delete);

        //Mock dragging Coastlines layer to the bottom, Aerosol to top
        viewModel.onSwapNeeded(1, 2);
        viewModel.onSwapNeeded(2, 3);
        viewModel.onSwapNeeded(1, 0);
    }

    @Test
    public void testMapTapWithNonColorMapLayer() {
        TestObserver<ColorMap> observer = new TestObserver<>();
        stack.add(getVIIRS());
        viewModel.setLayersAndUpdateMap(stack, delete);
        viewModel.getColorMapSub().subscribe(observer);

        //When non colormap layer gets tapped on the map
        viewModel.onToggleColorMap(true);

        //Colormap UI is not shown
        observer.assertValue(new ColorMap(null));
        verify(map).setToggleState(false);
    }

    private Layer getVIIRS() {
        return new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, VIIRS, SNPP)", "Suomi NPP / VIIRS", null, "2015-11-24", null, null, true);
    }
}
