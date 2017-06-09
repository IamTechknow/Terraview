package com.iamtechknow.terraview.map;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.util.Utils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.verify;

/**
 * Note the lack of useful tests here, this is because GMaps cannot be mocked.
 * TODO: improve presenter/view contract of the map to improve tests
 */
public class MapPresenterTest {
    @Mock
    private WorldActivity view;

    @Mock
    private DataSource data;

    private ArrayList<Layer> stack;

    private WorldPresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        stack = new ArrayList<>();
        presenter = new WorldPresenter(32);
        presenter.attachView(view);
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
        //Send empty list to presenter
        //(GMaps cannot be mocked, so can't use a real layer list)
        presenter.setLayersAndUpdateMap(stack);

        //Empty list is sent to layer list adapter
        verify(view).setLayerList(stack);
        presenter.detachView();
    }

    @Test
    public void showColorMapsUI() {
        //When user chooses show layer info menu option
        presenter.presentColorMaps();

        //Color maps dialog is displayed
        verify(view).showColorMaps();
    }

    @Test
    public void showPickerUI() {
        presenter.chooseLayers();

        verify(view).showPicker();
    }

    @Test
    public void showAboutUI() {
        presenter.presentAbout();

        verify(view).showAbout();
    }
}
