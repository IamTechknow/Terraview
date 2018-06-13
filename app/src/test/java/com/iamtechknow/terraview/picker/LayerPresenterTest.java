package com.iamtechknow.terraview.picker;

import android.util.SparseBooleanArray;

import com.iamtechknow.terraview.data.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    private LayerPresenterImpl presenter;

    private String measurement;

    @Before
    public void setupPresenter() {
        //Override default schedulers to be able to run the test on a JVM
        RxJavaPlugins.setIoSchedulerHandler(schedulerCallable -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());

        MockitoAnnotations.initMocks(this);
        measurement = "Test";
        presenter = new LayerPresenterImpl(view, RxBus.getInstance(), new ArrayList<>(), new ArrayList<>(), data, array, measurement);
    }

    @Test
    public void testLayerTabTapped() {
        //When layer tab is tapped //no measurements selected
        presenter.getData();
        verify(data).loadData(presenter);
        presenter.onDataLoaded();

        //Verify view has switched to layer tab and displays all layers
        verify(view, times(0)).updateLayerList(measurement, any());
        verify(view).populateList(any());
        presenter.detachView();
    }
}
