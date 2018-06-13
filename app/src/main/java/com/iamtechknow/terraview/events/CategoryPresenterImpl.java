package com.iamtechknow.terraview.events;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.model.EventCategory;
import com.iamtechknow.terraview.model.EventCategoryList;
import com.iamtechknow.terraview.model.TapEvent;
import com.iamtechknow.terraview.picker.RxBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CategoryPresenterImpl implements CategoryContract.Presenter {
    private CategoryContract.View view;
    private RxBus bus;
    private EONET client;
    private Disposable dataSub;

    private boolean loadedCategories;

    public CategoryPresenterImpl(RxBus _bus, CategoryContract.View v, EONET eonet) {
        bus = _bus;
        view = v;
        client = eonet;
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
            dataSub = client.getCategories()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onCategoriesLoaded);
    }

    //Send an event to the event bus when a category has been passed
    @Override
    public void emitEvent(int catId) {
        bus.send(new TapEvent(EventActivity.SELECT_EVENT_TAB, catId));
    }

    private void onCategoriesLoaded(EventCategoryList data) {
        loadedCategories = true;
        data.list.add(0, EventCategory.getAll()); //Add missing "all" category
        view.insertList(data.list);
    }
}
