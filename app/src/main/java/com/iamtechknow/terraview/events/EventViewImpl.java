package com.iamtechknow.terraview.events;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.iamtechknow.terraview.R;
import com.iamtechknow.terraview.adapter.EventAdapter;
import com.iamtechknow.terraview.model.EventCategory;
import com.iamtechknow.terraview.model.EventList;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;
import com.iamtechknow.terraview.util.Utils;

import io.reactivex.disposables.Disposable;

public class EventViewImpl extends Fragment {
    private static final int EVENT_INTERVAL = 30, EVENT_LIMIT = 300;

    @VisibleForTesting
    ViewModelProvider.Factory factory;

    //Default and empty views
    private RecyclerView mRecyclerView;
    private View empty_view;

    //Show closed or open events (API can't give both types)
    private boolean showingClosed;

    //How many events to show
    private int eventLimit;

    //MVVM objects
    private RxBus bus;
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private Disposable liveSub, busSub;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        viewModel = ViewModelProviders.of(this, factory != null ? factory : new EventsViewModelFactory()).get(EventViewModel.class);
        showingClosed = viewModel.isShowingClosed();
        eventLimit = viewModel.getLimit();
        bus = RxBus.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.startSub();
        busSub = bus.toObservable().subscribe(this::handleEvent);
        //Create subscription for the live data which gets triggered after each API request.
        liveSub = viewModel.getLiveData().subscribe(this::insertList);

        //Either use saved data before config change, load open events or if config change happened load prior category
        if(Utils.isOnline(getActivity())) {
            if(viewModel.getData() != null) {
                insertList(viewModel.getData()); //restore menu item later not here
            }  else {
                clearList();
                if (viewModel.getCategory() != EventCategory.getAll().getId() || showingClosed) { //Load events from saved or all categories
                    if (showingClosed)
                        viewModel.loadClosedEvents(eventLimit);
                    else
                        viewModel.handleEvent(new TapEvent(EventActivity.SELECT_EVENT_TAB, viewModel.getCategory()));
                } else
                    viewModel.loadEvents();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        busSub.dispose();
        liveSub.dispose();
        viewModel.cancelSubs();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        empty_view = rootView.findViewById(R.id.empty_view);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EventAdapter(viewModel);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addOnScrollListener(listener);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Gets called every time tab for fragment gets selected
        inflater.inflate(R.menu.event_menu, menu);
        menu.getItem(0).setTitle(showingClosed ? R.string.open_toggle : R.string.closed_toggle);
    }

    //If the user pressed the item when it says closed,
    //it will reset the number of events to show to the default
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showingClosed = !showingClosed;
        viewModel.setShowingClosed(showingClosed);

        item.setTitle(showingClosed ? R.string.open_toggle : R.string.closed_toggle);
        clearList();
        if(showingClosed) { //do show closed events
            eventLimit = EVENT_INTERVAL;
            viewModel.loadClosedEvents(eventLimit);
        } else
            viewModel.loadEvents();
        return true;
    }

    private RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            //Check if can scroll down, if showing closed events, and can load more
            if(!recyclerView.canScrollVertically(1) && shouldLoadMore()) {
                clearList();
                viewModel.loadClosedEvents(eventLimit += EVENT_INTERVAL);
            }
        }
    };

    private void insertList(EventList data) {
        empty_view.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        adapter.insertList(data.list);
    }

    private void clearList() {
        adapter.clearList();
        mRecyclerView.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    //Handle event emitted to set loading screen, because we don't have presenter to do it for us
    private void handleEvent(TapEvent e) {
        if(e != null && e.getTab() == EventActivity.SELECT_EVENT_TAB)
            clearList();
    }

    //Is showing closed events, and is there more events to show, and not within the set limit?
    //If the list count and eventLimit aren't the same, means there are no more events to get.
    private boolean shouldLoadMore() {
        return showingClosed && eventLimit == adapter.getItemCount() && eventLimit < EVENT_LIMIT;
    }
}
