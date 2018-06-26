package com.iamtechknow.terraview.events;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.iamtechknow.terraview.data.EONET;
import com.iamtechknow.terraview.picker.RxBus;

/**
 * View Model Factory to inject dependencies needed by the EONET ViewModels.
 */
public class EventsViewModelFactory implements ViewModelProvider.Factory {
    private EONET client = new EONET();

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(CategoryViewModel.class))
            return (T) new CategoryViewModel(client, RxBus.getInstance());
        else if(modelClass.isAssignableFrom(EventViewModel.class))
            return (T) new EventViewModel(client, RxBus.getInstance());
        else
            throw new IllegalArgumentException("Unknown ViewModel Class");
    }
}
