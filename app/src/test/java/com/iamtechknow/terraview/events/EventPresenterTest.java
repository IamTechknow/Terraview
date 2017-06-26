package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.picker.RxBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.reactivex.subjects.Subject;

import static org.mockito.Mockito.*;

public class EventPresenterTest {

    @Mock
    private EventView view;

    @Mock
    private EONET eonet;

    @Mock
    private RxBus bus;

    @Mock
    private Subject<Object> subject;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    private EventPresenterImpl presenter;

    @Before
    public void setupPresenter() {
        MockitoAnnotations.initMocks(this);

        //Init the bus correctly
        when(bus.toObserverable()).thenReturn(subject);

        presenter = new EventPresenterImpl(bus, view, eonet);
    }

    @Test
    public void validSourceTest() {
        //When presenter receives source
        String source = "https://earthobservatory.nasa.gov/IOTD/view.php?id=90443";
        presenter.presentSource(source);

        //View presents source
        verify(view).showSource(source);
    }

    @Test
    public void badSourceTest() {
        //When presenter receives no source such as from icebergs
        presenter.presentSource("");

        //View shows snackbar message
        verify(view).warnNoSource();
    }
}
