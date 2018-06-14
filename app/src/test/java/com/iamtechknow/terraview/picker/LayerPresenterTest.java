package com.iamtechknow.terraview.picker;

import android.util.SparseBooleanArray;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.TapEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link LayerContract.Presenter}
 */
public class LayerPresenterTest {
    @Mock
    private LayerContract.View view;

    @Mock
    private DataSource data;

    @Mock
    private SparseBooleanArray array;

    @Mock
    private RxBus bus;

    private Subject<TapEvent> subject = PublishSubject.create();

    private LayerPresenterImpl presenter;

    private String measurement;

    private List<Layer> layers;

    private ArrayList<Layer> add, delete;

    @BeforeClass
    public static void setupClass() {
        //Override default schedulers to be able to run the test on a JVM
        RxJavaPlugins.setIoSchedulerHandler(schedulerCallable -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Before
    public void setupPresenter() {
        MockitoAnnotations.initMocks(this);
        bus = RxBus.getInstance(subject); //allows mock bus events
        measurement = "Test";

        //Init mock data
        layers = new ArrayList<>();
        add = new ArrayList<>();
        delete = new ArrayList<>();
        layers.add(new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, null, false));
        layers.add(new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, VIIRS, SNPP)", "Suomi NPP / VIIRS", null, "2015-11-24", null, null, true));
    }

    @After
    public void cleanUp() {
        presenter.detachView();
    }

    @Test
    public void testLayerTabTappedOnly() {
        //Presenter needs to have no measurement
        presenter = new LayerPresenterImpl(view, bus, add, delete, data, array, null);

        //When layer tab is tapped, no measurements selected
        presenter.getData();
        verify(data).loadData(presenter);
        presenter.onDataLoaded();

        //Verify view has switched to layer tab and displays all layers
        verify(view, times(0)).updateLayerList(any(), any());
        verify(view).populateList(any());
    }

    @Test
    public void testMeasurementTapped() {
        presenter = new LayerPresenterImpl(view, bus, add, delete, data, array, measurement);

        doAnswer(invocation -> Single.just(layers))
            .when(data).getLayersForMeasurement(measurement);

        //Emit tap event on the mock measurement
        bus.send(new TapEvent(LayerActivity.SELECT_LAYER_TAB, null, measurement, null));

        //Verify layers for the measurement were shown
        verify(view).updateLayerList(measurement, layers);
    }

    @Test
    public void testSelectLayers() {
        presenter = new LayerPresenterImpl(view, bus, add, delete, data, array, null);
        when(data.getLayers()).thenReturn(layers);

        //Tap some layers and change the layer stack
        int idx = 0;
        boolean prevState = presenter.isItemChecked(idx); //false, default Mockito behavior
        presenter.setItemChecked(idx, !prevState);

        Layer temp = presenter.searchLayerByTitle("Coastlines (OSM)");
        if(temp != null)
            presenter.changeStack(temp, !prevState);

        //Coastlines layer should be queued to be added
        assertEquals(layers.get(idx), add.get(0));
    }
}
