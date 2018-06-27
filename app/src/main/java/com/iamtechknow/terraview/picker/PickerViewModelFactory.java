package com.iamtechknow.terraview.picker;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import com.iamtechknow.terraview.api.MetadataAPI;
import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.model.Layer;

import java.util.ArrayList;

import io.reactivex.subjects.PublishSubject;
import retrofit2.Retrofit;

/**
 * ViewModel Factory that takes in implementations of DataSources and Retrofit to create the ViewModels
 */
public class PickerViewModelFactory implements ViewModelProvider.Factory {
    private static final String BASE_URL = "https://worldview.earthdata.nasa.gov/";

    private DataSource data;
    private boolean forCategoryTab;

    //Layer tab dependencies
    private Retrofit apiBuilder = new Retrofit.Builder().baseUrl(BASE_URL).build();
    private ArrayList<Layer> stack, toDelete;

    public PickerViewModelFactory(DataSource data, boolean tab) {
        this.data = data;
        this.forCategoryTab = tab;
    }

    public PickerViewModelFactory(DataSource data, ArrayList<Layer> stack, ArrayList<Layer> toDel) {
        this(data, false);
        this.stack = stack;
        toDelete = toDel;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(LayerViewModel.class))
            return (T) new LayerViewModel(data, RxBus.getInstance(), apiBuilder.create(MetadataAPI.class), new SparseBooleanArray(),
                    stack, toDelete, PublishSubject.create(), PublishSubject.create());
        else if(modelClass.isAssignableFrom(NonLayerViewModel.class))
            return (T) new NonLayerViewModel(data, RxBus.getInstance(), forCategoryTab, PublishSubject.create(), PublishSubject.create());
        else
            throw new IllegalArgumentException("Unknown ViewModel Class");
    }
}
