package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import java.util.ArrayList;

public class CategoryPresenterImpl implements CategoryPresenter, EONET.LoadCallback {
    private CategoryView view;
    private RxBus bus;
    private EONET client;

    private boolean loadedCategories;

    public CategoryPresenterImpl(RxBus _bus, CategoryView v) {
        bus = _bus;
        view = v;
        client = new EONET();
        client.setCallback(this);
    }

    @Override
    public void detachView() {
        view = null;
        bus = null;
    }

    @Override
    public void loadCategories() {
        if(!loadedCategories)
            client.getCategories();
    }

    //Send an event to the event bus when a category has been passed
    @Override
    public void emitEvent(int catId) {
        bus.send(new TapEvent(EventActivity.SELECT_EVENT_TAB, catId));
    }

    @Override
    public void onEventsLoaded(ArrayList<Event> data) {}

    @Override
    public void onCategoriesLoaded(ArrayList<Category> data) {
        loadedCategories = true;
        data.add(0, Category.getAll()); //Add missing "all" category
        view.insertList(data);
    }
}
