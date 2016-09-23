package com.iamtechknow.terraview.data;

import com.iamtechknow.terraview.Injection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RemoteSourceTest {
    @Mock
    private DataSource.LoadCallback callback;

    private DataSource remoteSource;

    @Before
    public void setup() {
        remoteSource = Injection.provideRemoteSource(null);
    }

    @Test
    public void testGetValidData() {
        //Load data
        remoteSource.loadData(callback);

        //Verify callback and data
        verify(callback).onDataLoaded();
        assertTrue(remoteSource.getCategories() != null);
        assertTrue(remoteSource.getMeasurements() != null);
        assertTrue(remoteSource.getLayers() != null);
    }
}
