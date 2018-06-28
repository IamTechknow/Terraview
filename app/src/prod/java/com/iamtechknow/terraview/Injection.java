package com.iamtechknow.terraview;

import android.content.Context;
import android.support.annotation.NonNull;

import com.iamtechknow.terraview.data.*;

/**
 * Enables injection of production implementations for
 * {@link DataSource} at compile time.
 */
public class Injection {
    private static final String PREFS_FILE = "settings";

    public static DataSource provideRemoteSource(@NonNull Context c) {
        return new RemoteDataSource(TVDatabase.getInstance(c), c.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE));
    }

    public static DataSource provideLocalSource(@NonNull Context c) {
        return new LocalDataSource(TVDatabase.getInstance(c));
    }
}
