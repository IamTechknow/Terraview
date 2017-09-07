package com.iamtechknow.terraview.colormaps;

import com.iamtechknow.terraview.api.ColorMapAPI;
import com.iamtechknow.terraview.model.ColorMap;
import com.iamtechknow.terraview.model.ColorMapEntry;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ColorMapPresenterTest {
    @Mock
    private ColorMapView view;

    @Mock
    private ColorMapAPI api;

    private ColorMap data;

    private ColorMapPresenterImpl presenter;

    @BeforeClass
    public static void setupClass() {
        //Override default schedulers to be able to run the test on a JVM
        RxJavaPlugins.setIoSchedulerHandler(schedulerCallable -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Before
    public void setup() {
        //Add some color map entries from the Aerosol Index colormap
        ArrayList<ColorMapEntry> entries = new ArrayList<>();
        entries.add(new ColorMapEntry("240,240,240", "[0.000,0.025)", "0.000 - 0.025"));
        entries.add(new ColorMapEntry("222,222,102", "[0.975,1.000)", "0.975 - 1.000"));
        entries.add(new ColorMapEntry("229,110,0", "[2.975,3.000)", "2.975 - 3.000"));
        entries.add(new ColorMapEntry("217,6,0", "[3.975,4.000)", "3.975 - 4.000"));
        entries.add(new ColorMapEntry("157,0,42", "[4.975,5.000)", "4.975 - 5.000"));
        data = new ColorMap(entries);

        MockitoAnnotations.initMocks(this);
        presenter = new ColorMapPresenterImpl(api);
        presenter.attachView(view);
    }

    @Test
    public void testShowColorMap() {
        //When request for colormap is made
        when(api.fetchData("OMI_Aerosol_Index")).thenReturn(Observable.just(data));
        presenter.parseColorMap("OMI_Aerosol_Index");

        //View has received colormap data to draw canvas
        verify(view).setColorMapData(data);
        presenter.detachView();
    }
}
