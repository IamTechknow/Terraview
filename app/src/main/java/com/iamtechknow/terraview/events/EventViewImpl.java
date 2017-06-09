package com.iamtechknow.terraview.events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.picker.RxBus;
import com.iamtechknow.terraview.util.Utils;

import java.util.ArrayList;

public class EventViewImpl extends Fragment implements EventView {
    private static final int EVENT_INTERVAL = 30, EVENT_LIMIT = 300;

    private EventPresenter presenter;
    private EventAdapter adapter;

    //Default and empty views
    private RecyclerView mRecyclerView;
    private View empty_view;

    //Show closed or open events (API can't give both types)
    private boolean showingClosed;

    //How many events to show
    private int eventLimit = EVENT_INTERVAL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        presenter = new EventPresenterImpl(RxBus.getInstance(), this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Utils.isOnline(getActivity()))
            presenter.loadEvents(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.clearPresenter();
        presenter.detachView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabs, container, false);

        empty_view = rootView.findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new EventAdapter(presenter);
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
        item.setTitle(showingClosed ? R.string.open_toggle : R.string.closed_toggle);
        if(showingClosed) { //do show closed events
            eventLimit = EVENT_INTERVAL;
            presenter.presentClosed(eventLimit);
        } else
            presenter.loadEvents(false);

        return true;
    }

    private RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            //Check if can scroll down, if showing closed events, and can load more
            if(!recyclerView.canScrollVertically(1) && shouldLoadMore())
                presenter.presentClosed(eventLimit += EVENT_INTERVAL);
        }
    };

    @Override
    public void insertList(ArrayList<Event> list) {
        empty_view.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        adapter.insertList(list);
    }

    @Override
    public void clearList() {
        adapter.clearList();
        mRecyclerView.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSource(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void warnNoSource() {
        if(getView() != null)
            Snackbar.make(getView(), R.string.no_source, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public int getEventLimit() {
        return eventLimit;
    }

    //Is showing closed events, and is there more events to show, and not within the set limit?
    //If the list count and eventLimit aren't the same, means there are no more events to get.
    private boolean shouldLoadMore() {
        return showingClosed && eventLimit == adapter.getItemCount() && eventLimit < EVENT_LIMIT;
    }
}
