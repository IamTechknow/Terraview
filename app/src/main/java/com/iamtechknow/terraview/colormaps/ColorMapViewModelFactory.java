package com.iamtechknow.terraview.colormaps;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.iamtechknow.terraview.api.ColorMapAPI;

import io.reactivex.subjects.PublishSubject;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Factory that provides dependency injection for the ColorMap ViewModel.
 */
public class ColorMapViewModelFactory implements ViewModelProvider.Factory {
    private static final String BASE_URL = "https://gibs.earthdata.nasa.gov";

    private ColorMapAPI client;

    public ColorMapViewModelFactory() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        client = retrofit.create(ColorMapAPI.class);
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(ColorMapViewModel.class))
            return (T) new ColorMapViewModel(client, PublishSubject.create());
        else
            throw new IllegalArgumentException("Unknown ViewModel Class");
    }
}
