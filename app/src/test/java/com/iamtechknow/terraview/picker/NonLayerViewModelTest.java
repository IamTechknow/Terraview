package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Measurement;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Unit test for the implementation of {@link NonLayerViewModel}
 */
public class NonLayerViewModelTest {

    @Mock
    private DataSource data;

    private RxBus bus;

    private Subject<TapEvent> subject = PublishSubject.create();

    private Subject<List<Measurement>> measureSub = PublishSubject.create();

    private Subject<List<String>> catSub = PublishSubject.create();

    private TestObserver<List<Measurement>> testObserver;

    private NonLayerViewModel viewModel;

    private String category;

    private List<Measurement> allMeasurements, testMeasurements;

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
        category = "Test";

        allMeasurements = new ArrayList<>();
        allMeasurements.add(new Measurement("Aerosol Optical Depth"));
        allMeasurements.add(new Measurement("Fires and Thermal Anomalies"));
        allMeasurements.add(new Measurement("Sulfur Dioxide"));

        testMeasurements = new ArrayList<>();
        testMeasurements.add(new Measurement("Coastlines"));

        //Accept any category, and use the argument to determine which data to mock
        doAnswer(invocation -> Single.just(invocation.getArgument(0) == "All" ? allMeasurements : testMeasurements))
            .when(data).getMeasurementsForCategory(any());
        testObserver = new TestObserver<>();

    }

    @Test
    public void loadAllMeasurements() {
        //Get the data and verify the presenter as a LoadCallback
        viewModel = new NonLayerViewModel(data, bus, false, measureSub, catSub);
        viewModel.startSubs();
        viewModel.getLiveMeasures().subscribe(testObserver);

        //Mock load callback for DataSource
        viewModel.getData();
        viewModel.onDataLoaded();

        //Verify the correct object was received
        testObserver.assertValue(allMeasurements);
    }

    @Test
    public void testCategoryTapped() {
        //Here we need two presenters that represent the Category and Measurement tab.
        viewModel = new NonLayerViewModel(data, bus, false, measureSub, catSub);
        viewModel.startSubs();
        viewModel.getLiveMeasures().subscribe(testObserver);

        NonLayerViewModel cat_vw = new NonLayerViewModel(data, bus, true, measureSub, catSub);
        cat_vw.startSubs();

        //Emit tap event and load data on the mock category
        cat_vw.emitEvent(category);

        //Verify layers for the category were shown
        testObserver.assertValue(testMeasurements);
    }
}
