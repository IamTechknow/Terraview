package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.model.ColorMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.verify;

public class ColorMapPresenterTest {
    @Mock
    private ColorMapView view;

    @Captor
    private ArgumentCaptor<ColorMap> argument;

    private ColorMapPresenterImpl presenter;

    @BeforeClass
    public static void setupClass() {
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
        verify(view).setColorMapData(argument.capture());
        presenter.detachView();
    }
}
