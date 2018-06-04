package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Event;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class CategoryPresenterImpl implements CategoryPresenter, EONET.LoadCallback {
    private CategoryView view;
    private RxBus bus;
    private EONET client;
    private Disposable dataSub;

    private boolean loadedCategories;

    public CategoryPresenterImpl(RxBus _bus, CategoryView v, EONET eonet) {
        bus = _bus;
        view = v;
        client = eonet;
        client.setCallback(this);
    }

    @Override
    public void detachView() {
        if(dataSub != null) {
            dataSub.dispose();
            dataSub = null;
        }
        view = null;
        bus = null;
    }

    @Override
    public void loadCategories() {
        if(!loadedCategories)
            dataSub = client.getCategories();
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
