package com.iamtechknow.terraview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.data.LocalDataSource;
import com.iamtechknow.terraview.data.RemoteDataSource;

/**
 * Enables injection of production implementations for
 * {@link DataSource} at compile time.
 */
public class Injection {
    public static DataSource provideRemoteSource(@NonNull Context context) {
        return new RemoteDataSource(context);
    }

    public static DataSource provideLocalSource(@NonNull LoaderManager manager, @NonNull Context c) {
        return new LocalDataSource(manager, c);
    }
}
