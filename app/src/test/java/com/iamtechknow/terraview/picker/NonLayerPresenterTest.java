package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Measurement;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the implementation of {@link NonLayerContract.Presenter}
 */
public class NonLayerPresenterTest {

    @Mock
    private NonLayerContract.View view;

    @Mock
    private NonLayerContract.View cat_view;

    @Mock
    private DataSource data;

    private RxBus bus;

    private Subject<TapEvent> subject = PublishSubject.create();

    private NonLayerPresenterImpl presenter;

    private String category;

    private List<Measurement> allMeasurements, testMeasurements;

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
    }

    @After
    public void cleanUp() {
        presenter.detachView();
    }

    @Test
    public void loadAllMeasurements() {
        //Get the data and verify the presenter as a LoadCallback
        presenter = new NonLayerPresenterImpl(view, bus, data, null);
        presenter.getData();
        verify(data).loadData(presenter);

        //Verify measurement was not set
        presenter.onDataLoaded(); //loads data from All category
        verify(view).insertMeasurements("All", allMeasurements);
    }

    @Test
    public void testCategoryTapped() {
        //Here we need two presenters that represent the Category and Measurement tab.
        presenter = new NonLayerPresenterImpl(view, bus, data, null);
        NonLayerPresenterImpl cat_presenter = new NonLayerPresenterImpl(cat_view, bus, data, null);

        when(view.isCategoryTab()).thenReturn(false); //Mock the views as a certain tab
        when(cat_view.isCategoryTab()).thenReturn(true);

        //Emit tap event and load data on the mock category
        cat_presenter.emitEvent(category);

        //Verify layers for the category were shown
        verify(view).insertMeasurements(category, testMeasurements);
    }
}
