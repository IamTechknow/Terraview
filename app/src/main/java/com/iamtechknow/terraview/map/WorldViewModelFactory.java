package com.iamtechknow.terraview.map;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import io.reactivex.subjects.PublishSubject;

public class WorldViewModelFactory implements ViewModelProvider.Factory {
    private MapInteractor map;

    public WorldViewModelFactory(MapInteractor map) {
        this.map = map;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(WorldViewModel.class))
            return (T) new WorldViewModel(map, PublishSubject.create());
        else
            throw new IllegalArgumentException("Unknown ViewModel Class");
    }
}
