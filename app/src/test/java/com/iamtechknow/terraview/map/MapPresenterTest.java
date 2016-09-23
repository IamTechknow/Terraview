package com.iamtechknow.terraview.map;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.verify;

/**
 * Note the lack of useful tests here, this is because GMaps cannot be mocked.
 * TODO: improve presenter/view contract of the map to improve tests
 */
@RunWith(MockitoJUnitRunner.class)
public class MapPresenterTest {
    @Mock
    private WorldActivity view;

    @Mock
    private DataSource data;

    private ArrayList<Layer> stack;

    private WorldPresenter presenter;

    @Before
    public void setup() {
        stack = new ArrayList<>();
        presenter = new WorldPresenter(view);
    }

    //Test presenter initialization
    @Test
    public void testMockInit() {
        Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        verify(view).setDateDialog(c.getTimeInMillis());
    }

    @Test
    public void testSetMapEmpty() {
        //Send empty list to presenter
        //(GMaps cannot be mocked, so can't use a real layer list)
        presenter.setLayersAndUpdateMap(stack);

        //Empty list is sent to layer list adapter
        verify(view).setLayerList(stack);
    }
}
