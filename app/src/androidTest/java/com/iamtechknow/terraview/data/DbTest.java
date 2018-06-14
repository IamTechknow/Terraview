package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

/**
 * Abstract class for Room unit tests. The database used is temporary
 * and does not rely on any other existing databases
 */
public abstract class DbTest {
    TVDatabase db;

    @Before
    public void initDb() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                TVDatabase.class).build();
    }

    @After
    public void closeDb() {
        db.close();
    }
}
