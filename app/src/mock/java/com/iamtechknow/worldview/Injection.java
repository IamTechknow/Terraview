package com.iamtechknow.terraview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;

import com.iamtechknow.terraview.data.DataSource;
import com.iamtechknow.terraview.data.FakeRemoteDataSource;

/**
 * Enables injection of mock implementations for
 * {@link DataSource} at compile time. This is useful for testing, since it allows us to use
 * a fake instance of the class to isolate the dependencies and run a test hermetically.
 */
public class Injection {
    public static DataSource provideRemoteSource(@Nullable Context context) {
        return new FakeRemoteDataSource();
    }

    public static DataSource provideLocalSource(@Nullable LoaderManager manager, @Nullable Context context) {
        return new FakeRemoteDataSource();
    }
}