package com.iamtechknow.terraview.events;

import com.google.android.gms.maps.model.LatLng;
import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;
import com.iamtechknow.terraview.picker.RxBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;

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

    private EventPresenterImpl presenter;

    private Event event;

    @Before
    public void setupPresenter() {
        MockitoAnnotations.initMocks(this);

        //Init the bus correctly
        when(bus.toObserverable()).thenReturn(subject);

        //Add some events
        ArrayList<String> date = new ArrayList<>();
        date.add("2017-09-03T13:00:00Z");
        event = new Event("EONET_3249", "Mission Fire, CALIFORNIA",  "https://inciweb.nwcg.gov/incident/5588/", date, 8, Collections.singletonList(new LatLng(37.212777777778, -119.48277777778)));

        presenter = new EventPresenterImpl(bus, view, eonet);
    }

    @Test
    public void getEventsTest() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);

        //Call the presenter callback to mock successful parsing
        doAnswer(invocation -> {
            presenter.onEventsLoaded(new EventList(events));
            return null;
        }).when(eonet).getOpenEvents();

        //When presenter loads open events, view presents them
        presenter.loadEvents(false);
        verify(view).insertList(events);

        presenter.detachView();
    }

    @Test
    public void getClosedEventsTest() {
        ArrayList<Event> events = new ArrayList<>(), moreEvents = new ArrayList<>();
        events.add(event);
        for(int i = 0; i < 10; i++)
            events.add(event);

        doAnswer(invocation -> {
            presenter.onEventsLoaded(new EventList(events));
            return null;
        }).when(eonet).getClosedEvents(0, 1);

        doAnswer(invocation -> {
            presenter.onEventsLoaded(new EventList(moreEvents));
            return null;
        }).when(eonet).getClosedEvents(0, 10);

        //When presenter loads closed events with limits, view presents them
        presenter.presentClosed(1);
        verify(view).insertList(events);

        presenter.presentClosed(10);
        verify(view).insertList(moreEvents);
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
