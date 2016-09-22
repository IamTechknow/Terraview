package com.iamtechknow.worldview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;

import com.iamtechknow.worldview.data.DataSource;
import com.iamtechknow.worldview.data.FakeRemoteDataSource;

/**
 * Enables injection of mock implementations for
 * {@link DataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {
    public static DataSource provideRemoteSource(@NonNull Context context) {
        return new FakeRemoteDataSource();
    }

    public static DataSource provideLocalSource(@NonNull LoaderManager manager, @NonNull Context context) {
        return new FakeRemoteDataSource();
    }
}