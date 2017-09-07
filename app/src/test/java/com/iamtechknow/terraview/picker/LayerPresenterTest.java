package com.iamtechknow.terraview.picker;

import android.util.SparseBooleanArray;

import com.iamtechknow.terraview.data.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

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

    @Captor
    private ArgumentCaptor<ArrayList<String>> captor;

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
        verify(view).populateList(captor.capture());
        presenter.detachView();
    }

    @Test
    public void testRetrofitAndShowInfo() {
        //When info icon is tapped
        presenter.useRetrofit("modis/Areas_NoData");

        String expectedHTML = "\n" + //HTML can be long, this one is the shortest possible
                "      <h1 id=\"modis-terra-aqua-areas-of-no-data\">MODIS (Terra/Aqua) Areas of No Data</h1>\n" +
                "<p>The MODIS Areas of No Data (Terra/Aqua) layer shows the areas on the earth in which the Terra or Aqua satellites have not covered on that particular day.</p>\n" +
                "\n";
        verify(view).showInfo(expectedHTML); //Verify expected HTML response
        presenter.detachView();
    }
}
