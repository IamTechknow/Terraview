package com.iamtechknow.terraview.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.iamtechknow.terraview.model.CatMeasureJoin;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.MeasureLayerJoin;
import com.iamtechknow.terraview.model.Measurement;
import com.iamtechknow.terraview.model.SearchQuery;

@Database(entities = {Layer.class, Measurement.class, Category.class, SearchQuery.class, CatMeasureJoin.class, MeasureLayerJoin.class}, version = 1, exportSchema = false)
public abstract class TVDatabase extends RoomDatabase {
    private static TVDatabase INSTANCE;

    public static TVDatabase getInstance(Context c) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(c.getApplicationContext(),
                    TVDatabase.class, "data.sqlite")
                    .build();
        }
        return INSTANCE;
    }

    public abstract TVDao getTVDao();
    public abstract JoinDAO getJoinDAO();
}
