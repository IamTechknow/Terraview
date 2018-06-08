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
 * Unit tests for the implementation of {@link LayerPresenter}
 */
public class LayerPresenterTest {
    @Mock
    private LayerView view;

    @Mock
    private DataSource data;

    @Mock
    private SparseBooleanArray array;

    private LayerPresenterImpl presenter;

    @Before
    public void setupPresenter() {
        //Override default schedulers to be able to run the test on a JVM
        RxJavaPlugins.setIoSchedulerHandler(schedulerCallable -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());

        MockitoAnnotations.initMocks(this);
        presenter = new LayerPresenterImpl(RxBus.getInstance(), new ArrayList<>(), new ArrayList<>(), data, array);
        presenter.attachView(view);
    }

    @Test
    public void testLayerTabTapped() {
        //When layer tab is tapped //no measurements selected
        presenter.getData();
        verify(data).loadData(presenter);
        presenter.onDataLoaded();

        //Verify view has switched to layer tab and displays all layers
        verify(view, times(0)).updateLayerList(null);
        verify(view).populateList(any());
        presenter.detachView();
    }
}
