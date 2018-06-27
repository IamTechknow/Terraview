package com.iamtechknow.terraview.picker;

import android.util.SparseBooleanArray;

import com.iamtechknow.terraview.api.MetadataAPI;
import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.TapEvent;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the implementation of {@link LayerViewModel}
 */
public class LayerViewModelTest {
    @Mock
    private DataSource data;

    @Mock
    private MetadataAPI metaAPI;

    @Mock
    private SparseBooleanArray array;

    private RxBus bus;

    private Subject<TapEvent> subject = PublishSubject.create();

    private Subject<List<Layer>> layerSub = PublishSubject.create();

    private Subject<String> metaSub = PublishSubject.create();

    private TestObserver<List<Layer>> testObserver;

    private LayerViewModel viewModel;

    private List<Layer> layers;

    private ArrayList<Layer> add;

    @BeforeClass
    public static void setupClass() {
        //Override default schedulers to be able to run the test on a JVM
        RxJavaPlugins.setIoSchedulerHandler(schedulerCallable -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        bus = RxBus.getInstance(subject); //allows mock bus events
        testObserver = new TestObserver<>();

        //Init mock data
        layers = new ArrayList<>();
        add = new ArrayList<>();
        ArrayList<Layer> delete = new ArrayList<>();
        layers.add(new Layer("Coastlines", "GoogleMapsCompatible_Level9", "png", "Coastlines (OSM)", "OpenStreetMaps", null, null, null, null, false));
        layers.add(new Layer("VIIRS_SNPP_CorrectedReflectance_TrueColor", "GoogleMapsCompatible_Level9", "jpg", "Corrected Reflectance (True Color, VIIRS, SNPP)", "Suomi NPP / VIIRS", null, "2015-11-24", null, null, true));

        viewModel = new LayerViewModel(data, bus, metaAPI, array, add, delete, metaSub, layerSub);
        viewModel.startSubs();
        viewModel.getLiveData().subscribe(testObserver);
    }

    @Test
    public void testLayerTabTappedOnly() {
        //When layer tab is tapped, no measurements selected
        when(data.getLayers()).thenReturn(layers);
        viewModel.getData();
        viewModel.onDataLoaded();

        testObserver.assertValue(layers);
        assertNull(viewModel.getMeasurement());
    }

    @Test
    public void testMeasurementTapped() {
        String measurement = "Test";
        doAnswer(invocation -> Single.just(layers))
            .when(data).getLayersForMeasurement(measurement);

        //Emit tap event on the mock measurement
        bus.send(new TapEvent(LayerActivity.SELECT_LAYER_TAB, null, measurement, null));

        //Verify layers for the measurement were loaded
        testObserver.assertValue(layers);
    }

    @Test
    public void testSelectLayers() {
        when(data.getLayers()).thenReturn(layers);

        //Tap some layers and change the layer stack
        int idx = 0;
        boolean prevState = viewModel.isItemChecked(idx); //false, default Mockito behavior
        viewModel.setItemChecked(idx, !prevState);

        Layer temp = viewModel.searchLayerByTitle("Coastlines (OSM)");
        if(temp != null)
            viewModel.changeStack(temp, !prevState);

        //Coastlines layer should be queued to be added
        assertEquals(layers.get(idx), add.get(0));
    }
}
