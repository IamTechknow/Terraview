package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.model.ColorMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ColorMapPresenterTest {
    @Mock
    private ColorMapView view;

    private ColorMapPresenterImpl presenter;

    @BeforeClass
    public void setupClass() {
        //Allow AndroidSchedulers.mainThread() to be overridden
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(__ -> Schedulers.trampoline());
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
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
