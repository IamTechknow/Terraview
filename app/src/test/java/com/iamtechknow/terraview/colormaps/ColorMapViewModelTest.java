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

import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ColorMapViewModelTest {
    @Mock
    private ColorMapAPI api;

    private ColorMap data;

    private ColorMapViewModel viewModel;

    private Subject<Integer> subject = PublishSubject.create();

    private TestObserver<Integer> testObserver;

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
        entries.add(new ColorMapEntry("0,0,0", "Invalid", "No Data"));
        entries.add(new ColorMapEntry("240,240,240", "[0.000,0.025)", "0.000 - 0.025"));
        entries.add(new ColorMapEntry("222,222,102", "[0.975,1.000)", "0.975 - 1.000"));
        entries.add(new ColorMapEntry("229,110,0", "[2.975,3.000)", "2.975 - 3.000"));
        entries.add(new ColorMapEntry("217,6,0", "[3.975,4.000)", "3.975 - 4.000"));
        entries.add(new ColorMapEntry("157,0,42", "[4.975,5.000)", "4.975 - 5.000"));
        data = new ColorMap(entries);

        MockitoAnnotations.initMocks(this);
        viewModel = new ColorMapViewModel(api, subject);
        testObserver = new TestObserver<>();
    }

    @Test
    public void testShowColorMap() {
        String palette = "OMI_Aerosol_Index";
        //When request for colormap is made
        when(api.fetchData(palette)).thenReturn(Single.just(data));
        viewModel.getLiveData().subscribe(testObserver);

        //Assert the colormap is loaded
        viewModel.loadColorMap(0, palette);
        assertTrue(viewModel.getColorMap(palette) != null);

        for(ColorMapEntry c : viewModel.getColorMap(palette).getList())
            assertFalse(c.isInvalid());

        viewModel.cancelSub();
    }
}
