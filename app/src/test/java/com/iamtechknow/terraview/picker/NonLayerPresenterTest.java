package com.iamtechknow.terraview.picker;

import com.iamtechknow.terraview.data.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Unit test for the implementation of {@link NonLayerContract.Presenter}
 */
public class NonLayerPresenterTest {

    @Mock
    private NonLayerContract.View view;

    @Mock
    private DataSource data;

    private NonLayerPresenterImpl presenter;

    @Before
    public void setupPresenter() {
        MockitoAnnotations.initMocks(this);
        presenter = new NonLayerPresenterImpl(view, RxBus.getInstance(), data, "Test");
    }

    @Test
    public void loadFakeDataIntoView() {
        //Get the data and verify the presenter as a LoadCallback
        presenter.getData();
        verify(data).loadData(presenter);

        //When data has been loaded, the first two tabs display the data
        presenter.onDataLoaded();
        verify(view).insertList(any());
        presenter.detachView();
    }
}
