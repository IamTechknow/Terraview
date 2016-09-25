package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.model.ColorMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.co.ribot.androidboilerplate.util.RxSchedulersOverrideRule;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ColorMapPresenterTest {

    //Needed to allow mocking of AndroidSchedulers.mainThread()
    @Rule
    public RxSchedulersOverrideRule rule = new RxSchedulersOverrideRule();

    @Mock
    private ColorMapView view;

    private ColorMapPresenterImpl presenter;

    @Before
    public void setup() {
        presenter = new ColorMapPresenterImpl();
        presenter.attachView(view);
    }

    @Test
    public void testShowColorMap() {
        //When request for colormap is made
        presenter.parseColorMap("MODIS_Combined_Value_Added_AOD");

        //View has received colormap data to draw canvas
        ArgumentCaptor<ColorMap> argument = ArgumentCaptor.forClass(ColorMap.class);
        verify(view).setColorMapData(argument.capture());
        presenter.detachView();
    }
}
