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

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.observers.TestObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EventViewModelTest {

    private EONET eonet; //Mocked instance

    private RxBus bus;

    private Subject<TapEvent> subject = PublishSubject.create();

    private Subject<EventList> eventSubject = PublishSubject.create();

    private TestObserver<EventList> testObserver;

    private EventViewModel viewModel;

    private Event event;

    @BeforeClass
    public static void setupClass() {
        //Override default schedulers to be able to run the test on a JVM
        RxJavaPlugins.setIoSchedulerHandler(schedulerCallable -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Before
    public void setup() {
        eonet = mock(EONET.class);
        bus = RxBus.getInstance(subject); //allows mock bus events
        testObserver = new TestObserver<>();

        //Add some events
        ArrayList<String> date = new ArrayList<>();
        date.add("2017-09-03T13:00:00Z");
        event = new Event("EONET_3249", "Mission Fire, CALIFORNIA",  "https://inciweb.nwcg.gov/incident/5588/", date, 8, Collections.singletonList(new LatLng(37.212777777778, -119.48277777778)));

        viewModel = new EventViewModel(eonet, bus, eventSubject);
        viewModel.startSub();
    }

    @After
    public void cleanUp() {
        viewModel.cancelSubs();
    }

    @Test
    public void testNoSubscription() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);
        EventList list = new EventList(events);

        doAnswer(invocation -> Single.just(list))
            .when(eonet).getOpenEvents();

        //load data then subscribe
        viewModel.loadEvents();
        viewModel.getLiveData().subscribe(testObserver);

        //Data has been updated, but View does not know
        assertTrue(viewModel.getData() != null);
        testObserver.assertValueCount(0);
    }

    @Test
    public void getOpenEventsTest() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);
        EventList list = new EventList(events);

        //Since EONET is mocked, we may mock the Retrofit call by providing our own Single object
        doAnswer(invocation -> Single.just(list))
            .when(eonet).getOpenEvents();

        //Act. Subscribe and fetch
        viewModel.getLiveData().subscribe(testObserver);
        viewModel.loadEvents();

        //Verify. Get the list of events, which contains the list of values emitted
        testObserver.assertValueCount(1);
        testObserver.assertValue(list);
        verify(eonet, never()).getClosedEvents(anyInt(), anyInt());
    }

    @Test
    public void getClosedEventsTest() {
        ArrayList<Event> events = new ArrayList<>(), moreEvents = new ArrayList<>();
        events.add(event);
        for(int i = 0; i < 10; i++)
            moreEvents.add(event);
        EventList list = new EventList(moreEvents);

        doAnswer(invocation -> Single.just(new EventList(events)))
            .when(eonet).getClosedEvents(0, 1);

        doAnswer(invocation -> Single.just(list))
            .when(eonet).getClosedEvents(0, 10);

        //When subscribed to live data and requested closed events twice
        viewModel.getLiveData().subscribe(testObserver);
        viewModel.loadClosedEvents(1);
        viewModel.loadClosedEvents(10);

        //Two separate lists are acquired
        testObserver.assertValueCount(2);
        testObserver.assertValueAt(1, list);
        verify(eonet, never()).getOpenEvents();
    }

    @Test
    public void validSourceTest() {
        //When View receives source, view model verifies it
        assertTrue(viewModel.isSourceValid(event.getSource()));
    }

    @Test
    public void badSourceTest() {
        //When View receives source such as from icebergs, view model informs view it is invalid
        assertFalse(viewModel.isSourceValid(""));
    }

    @Test
    public void testSelectCategory() {
        ArrayList<Event> events = new ArrayList<>();
        events.add(event);
        int category = 8; //Wildfires category
        EventList list = new EventList(events);

        //Mock observables
        doAnswer(invocation -> Single.just(list))
            .when(eonet).getEventsByCategory(category);

        //User tap triggers event
        viewModel.getLiveData().subscribe(testObserver);
        bus.send(new TapEvent(EventActivity.SELECT_EVENT_TAB, category));

        verify(eonet).getEventsByCategory(category);
        testObserver.assertValue(list);
    }
}
