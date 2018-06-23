package com.iamtechknow.terraview.events;

import android.arch.lifecycle.ViewModel;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.EventCategoryList;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import io.reactivex.Single;

public class CategoryViewModel extends ViewModel {
    private EONET client;
    private RxBus bus;

    public CategoryViewModel(EONET client, RxBus bus) {
        this.client = client;
        this.bus = bus;
    }

    public Single<EventCategoryList> loadCategories() {
        return client.getCategories();
    }

    //Send an event to the event bus when a category has been passed
    public void emitEvent(int catId) {
        bus.send(new TapEvent(EventActivity.SELECT_EVENT_TAB, catId));
    }
}
