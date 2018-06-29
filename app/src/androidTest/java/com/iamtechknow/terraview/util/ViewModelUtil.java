package com.iamtechknow.terraview.util;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

/**
 * Creates a one-off factory for the given view model instance
 */
public class ViewModelUtil {
    public static <T extends ViewModel> ViewModelProvider.Factory createFor(T model) {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(model.getClass()))
                    return (T) model;

                throw new IllegalArgumentException("Unknown ViewModel Class");
            }
        };
    }
}
