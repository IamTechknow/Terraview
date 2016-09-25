package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import uk.co.ribot.androidboilerplate.util.RxSchedulersOverrideRule;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link LayerPresenter}
 */
@RunWith(MockitoJUnitRunner.class)
public class LayerPresenterTest {

    //Needed to allow mocking of AndroidSchedulers.mainThread()
    @Rule
    public RxSchedulersOverrideRule rule = new RxSchedulersOverrideRule();

    @Mock
    private LayerView view;

    @Mock
    private ArrayList<Layer> list;

    @Mock
    private DataSource data;

    private LayerPresenterImpl presenter;

    @Before
    public void setupPresenter() {
        presenter = new LayerPresenterImpl(list, data);
        presenter.attachView(view);
    }

    @Test
    public void testMeasurementTapped() {
        //When an item in measurement tab is tapped
        presenter.setMeasurement("Blue Marble");
        presenter.getData();
        verify(data).loadData(presenter);
        presenter.onDataLoaded();

        //Verify view has switched to layer tab
        verify(view).onNewMeasurement("Blue Marble");
        verify(view, times(0)).populateList(data.getLayers());
        presenter.detachView();
    }

    @Test
    public void testLayerTabTapped() {
        //When layer tab is tapped
        presenter.getData();
        verify(data).loadData(presenter);
        presenter.onDataLoaded();

        //Verify view has switched to layer tab and displays all layers
        verify(view, times(0)).onNewMeasurement(null);
        verify(view).populateList(data.getLayers());
        presenter.detachView();
    }

    @Test
    public void testRetrofitAndShowInfo() {
        //When info icon is tapped
        presenter.useRetrofit("modis/Areas_NoData");

        String expectedHTML = "\n" + //HTML can be long, this one is the shortest possible
                "      <h1 id=\"modis-terra-aqua-areas-of-no-data\">MODIS (Terra/Aqua) Areas of No Data</h1>\n" +
                "<p>The MODIS Areas of No Data (Terra/Aqua) layer shows the areas on the earth in which the Terra or Aqua satellites have not covered on that particular day.</p>\n" +
                "\n";
        verify(view).showInfo(expectedHTML); //Verify expected HTML response
        presenter.detachView();
    }
}
