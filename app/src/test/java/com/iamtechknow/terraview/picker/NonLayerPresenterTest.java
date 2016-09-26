package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.verify;

/**
 * Unit test for the implementation of {@link NonLayerPresenter}
 */
@RunWith(MockitoJUnitRunner.class)
public class NonLayerPresenterTest {

    @Mock
    private NonLayerView view;

    @Mock
    private DataSource data;

    @Captor
    ArgumentCaptor<ArrayList<String>> captor;

    private NonLayerPresenterImpl presenter;

    @Before
    public void setupPresenter() {
        presenter = new NonLayerPresenterImpl(RxBus.getInstance(), data);
        presenter.attachView(view);
    }

    @Test
    public void loadFakeDataIntoView() {
        //Get the data and verify the presenter as a LoadCallback
        presenter.getData();
        verify(data).loadData(presenter);

        //When data has been loaded, the first two tabs display the data
        presenter.onDataLoaded();
        verify(view).insertList(captor.capture());
        presenter.detachView();
    }

    @Test
    public void loadDataAfterConfigChange() {
        //set the category to simulate config change
        presenter.setCategory("Category");
        presenter.getData();
        verify(data).loadData(presenter);

        //When data loaded, presenter knows to insert measurement lists
        presenter.onDataLoaded();
        verify(view).insertMeasurements(captor.capture());
        presenter.detachView();
    }
}
