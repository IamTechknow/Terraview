package com.iamtechknow.terraview.events;

import com.google.android.gms.maps.model.LatLng;
import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.EventList;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.mockito.Mockito.*;

public class EventPresenterTest {

    @Mock
    private EventContract.View view;

    @Mock
    private EONET eonet;

    @Mock
    private RxBus bus;

    private Subject<TapEvent> subject = PublishSubject.create();

    private EventPresenterImpl presenter;

    private Event event;

    private int category;

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

        //Add some events
        ArrayList<String> date = new ArrayList<>();
        date.add("2017-09-03T13:00:00Z");
        event = new Event("EONET_3249", "Mission Fire, CALIFORNIA",  "https://inciweb.nwcg.gov/incident/5588/", date, 8, Collections.singletonList(new LatLng(37.212777777778, -119.48277777778)));
        category = 0;

        presenter = new EventPresenterImpl(bus, view, eonet, false, category);
    }

    @After
    public void cleanUp() {
        presenter.detachView();
    }

    @Test
    public void getEventsTest() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);

        //Since EONET is mocked, we may mock the Retrofit call by providing our own Single object
        doAnswer(invocation -> Single.just(new EventList(events)))
            .when(eonet).getOpenEvents();

        //When presenter loads open events, view presents them
        presenter.loadEvents(false);
        verify(view).insertList(category, events);
    }

    @Test
    public void getClosedEventsTest() {
        ArrayList<Event> events = new ArrayList<>(), moreEvents = new ArrayList<>();
        events.add(event);
        for(int i = 0; i < 10; i++)
            moreEvents.add(event);

        doAnswer(invocation -> Single.just(new EventList(events)))
            .when(eonet).getClosedEvents(0, 1);

        doAnswer(invocation -> Single.just(new EventList(moreEvents)))
            .when(eonet).getClosedEvents(0, 10);

        //When presenter loads closed events with limits, view presents them
        presenter.presentClosed(1);
        verify(view).insertList(category, events);

        presenter.presentClosed(10);
        verify(view).insertList(category, moreEvents);
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

    @Test
    public void testSelectCategory() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);
        int category = 8;

        //Mock observables
        doAnswer(invocation -> Single.just(new EventList(events)))
            .when(eonet).getEventsByCategory(category);

        //User tap triggers event
        bus.send(new TapEvent(EventActivity.SELECT_EVENT_TAB, category));

        //Verify the correct list was shown
        verify(view).insertList(category, events);
    }
}
